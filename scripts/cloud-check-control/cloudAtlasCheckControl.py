#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
execute atlas-checks on an EC2 instance
"""
import argparse
import logging
import os
import sys
import time

import boto3
import paramiko
import scp
from botocore.exceptions import ClientError
from paramiko.auth_handler import AuthenticationException


VERSION = "1.0.0"
AWS_REGION = 'us-west-1'


def setup_logging(default_level=logging.INFO):
    """
    Setup logging configuration
    """
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s",
        level=default_level,
        datefmt="%Y-%m-%d %H:%M:%S",
    )
    return logging.getLogger("CloudAtlasChecksControl")


def finish(error_message=None, status=0):
    """exit the process

    Method to exit the Python script. It will log the given message and then exit().

    :param error_message: Error message to log upon exiting the process
    :param status: return code to exit the process with
    """
    if error_message:
        logger.error(error_message)
    else:
        logger.info("Done")
    exit(status)


class CloudAtlasChecksControl:
    """Main Class to control atlas checks spark job on EC2"""

    def __init__(
        self,
        timeoutMinutes=6000,
        key=None,
        instanceId="",
        processes=32,
        memory=256,
        formats="flags",
        countries="",
        s3InFolder=None,
        s3fsMount=False,
        s3OutFolder=None,
        terminate=False,
        templateName="atlas_checks-ec2-template",
        atlasConfig="https://raw.githubusercontent.com/osmlab/atlas-checks/dev/config/configuration.json",
        checks="",
        mrkey="",
        mrProject="",
        mrURL="https://maproulette.org:443",
        jar="atlas-checks/build/libs/atlas-checks-*-SNAPSHOT-shadow.jar",
        awsRegion=AWS_REGION,
    ):
        self.timeoutMinutes = timeoutMinutes
        self.key = key
        self.instanceId = instanceId
        self.s3InFolder = s3InFolder
        self.s3fsMount = s3fsMount
        self.processes = processes
        self.memory = memory
        self.formats = formats
        self.countries = countries
        self.s3OutFolder = s3OutFolder
        self.terminate = terminate
        self.templateName = templateName
        self.homeDir = "/home/ubuntu/"
        self.atlasCheckDir = os.path.join(self.homeDir, "atlas-checks/")
        self.atlasOutDir = os.path.join(self.homeDir, "output/")
        self.atlasInDir = os.path.join(self.homeDir, "input/")
        self.atlasLogDir = os.path.join(self.homeDir, "log/")
        self.atlasCheckLogName = "atlasCheck.log"
        self.atlasCheckLog = os.path.join(self.atlasLogDir, self.atlasCheckLogName)
        self.atlasCheckMRPushLogName = "mrPush.log"
        self.atlasCheckMRPushLog = os.path.join(
            self.atlasLogDir, self.atlasCheckMRPushLogName
        )
        self.atlasConfig = atlasConfig
        self.checks = checks
        self.mrkey = mrkey
        self.mrProject = mrProject
        self.mrURL = mrURL
        self.jar = jar

        self.instanceName = "AtlasChecks"
        self.localJar = '/tmp/atlas-checks.jar'
        self.localConfig = '/tmp/configuration.json'

        self.sshClient = None
        self.scpClient = None
        self.ec2 = boto3.client(
            'ec2',
            region_name = awsRegion,
        )
        self.ssmClient = boto3.client(
            'ssm',
            region_name = awsRegion,
        )


    def setup_config(self, file_preface="file://"):
        if self.atlasConfig.find("s3:") >= 0:
            if self.ssh_cmd(
                "aws s3 cp {} {}".format(self.atlasConfig, self.localConfig)
            ):
                finish("Failed to copy config S3://{}".format(self.atlasConfig), -1)
            return file_preface + self.localConfig
        elif self.atlasConfig.find("http:") >= 0:
            return self.atlasConfig
        else:
            # if configuration.json is a file then copy it to the EC2 instance
            self.put_files(self.atlasConfig, self.localConfig)
            return file_preface + self.localConfig

    def setup_jar(self):
        if self.jar.find("s3:") >= 0:
            if self.ssh_cmd(
                "aws s3 cp {} {}".format(self.jar, self.localJar)
            ):
                finish("Failed to copy jar S3://{}".format(self.jar), -1)
            return self.localJar
        else:
            # if configuration.json is a file then copy it to the EC2 instance
            self.put_files(self.jar, self.localJar)
            return self.localJar

    def atlasCheck(self):
        """Submit an spark job to perform atlas checks on an EC2 instance.

        If the CloudAtlasChecksControl includes an instance ID then atlas checks will
        be executed on that instance. If no instance ID is defined then it will
        create a new instance.

        Dependencies:
          - self.instanceId - indicates a running instance or "" to create one
          - self.S3Atlas - indicates the S3 bucket and path that contains atlas files
        """
        if self.instanceId == "":
            self.create_instance()
            self.get_instance_info()

        if not self.is_process_running("SparkSubmit"):
            cmd = "mkdir -p {} {}".format(self.atlasLogDir, self.atlasOutDir)
            if self.ssh_cmd(cmd):
                finish("Unable to create directory {}".format(cmd), -1)

            # remove the success or failure files from any last run.
            if self.ssh_cmd("rm -f {}/_*".format(self.atlasOutDir)):
                finish("Unable to clean up old status files", -1)

            # sync the country folders to the local directory
            for c in list(self.countries.split(",")):
                logger.info("syncing {}".format(c))
                if self.ssh_cmd(
                    "aws s3 sync --only-show-errors {0}{1} {2}{1}".format(
                        self.s3InFolder, c, self.atlasInDir
                    )
                ):
                    finish(
                        "Failed to sync {}/{}".format(self.s3InFolder, c), -1
                    )
            if self.ssh_cmd(
                "aws s3 cp {}sharding.txt {}sharding.txt".format(
                    self.s3InFolder, self.atlasInDir
                )
            ):
                finish("Failed to copy sharding.txt", -1)

            if self.info is not None:
                cmd = ("echo '{{\n{},\n\"cmd\":\"{}\"\n}}' > {}INFO "
                .format(self.info, " ".join(sys.argv), self.atlasOutDir))
            else:
                cmd = ("echo '{{\n\"cmd\":\"{}\"\n}}' > {}INFO "
                .format(" ".join(sys.argv), self.atlasOutDir))
            if self.ssh_cmd(cmd):
                finish("Unable to write info file", -1)

            atlasConfig = self.setup_config()
            jarFile = self.setup_jar()

            cmd = (
                "/opt/spark/bin/spark-submit"
                + " --class=org.openstreetmap.atlas.checks.distributed.ShardedIntegrityChecksSparkJob"
                + " --master=local[{}]".format(self.processes)
                + " --conf='spark.driver.memory={}g'".format(self.memory)
                + " --conf='spark.rdd.compress=true'"
                + " {}".format(jarFile)
                + " -maxPoolMinutes=2880"
                + " -inputFolder='{}'".format(self.atlasInDir)
                + " -output='{}'".format(self.atlasOutDir)
                + " -outputFormats='{}'".format(self.formats)
                + " -countries='{}'".format(self.countries)
                + " -configFiles='{}'".format(atlasConfig)
                + " > {} 2>&1 &".format(self.atlasCheckLog)
            )

            logger.info("Submitting spark job: {}".format(cmd))
            if self.ssh_cmd(cmd):
                finish("Unable to execute spark job", -1)
            # make sure spark job has started before checking for completion
            time.sleep(5)
        else:
            logger.info("Detected a running atlas check spark job.")

        logger.info("About to wait for remote script to complete. If "
                    "disconnected before the script completes then execute the "
                    "following command to continue waiting:\n {} --id={}"
                    .format(" ".join(sys.argv), self.instanceId))
        # wait for script to complete
        if self.wait_for_process_to_complete():
            finish(
                "Timeout waiting for script to complete. TODO - instructions to reconnect.",
                -1,
            )

        self.sync()

    def sync(self):
        """Sync an existing instance containing already generated atlas output with s3

        Dependencies:
          - self.instanceId - indicates a running instance or "" to create one
          - self.s3OutFolder - the S3 bucket and folder path to push the output
          - self.terminate - indicates if the EC2 instance should be terminated
        """
        if self.s3OutFolder is None:
            logger.warning(
                "No S3 output folder specified, skipping s3 sync. Use -o 's3folder/path' to sync to s3"
            )
            return
        logger.info(
            "Syncing EC2 instance atlas-checks output with S3 bucket {}.".format(
                self.s3OutFolder
            )
        )

        # push output to s3
        cmd = "aws s3 sync --only-show-errors --exclude *.crc {} {} ".format(
            self.atlasOutDir, self.s3OutFolder
        )
        if self.ssh_cmd(cmd):
            finish("Unable to sync with S3", -1)
        # terminate instance
        if self.terminate:
            self.terminate_instance()

    def challenge(self):
        if self.instanceId == "":
            self.create_instance()
            self.get_instance_info()
        logger.info("Creating map roulette challenge.")

        # sync the country folders to the local directory
        for c in list(self.countries.split(",")):
            logger.info(
                "syncing {}/flag/{} to {}flag/{}".format(
                    self.s3InFolder, c, self.atlasOutDir, c
                )
            )
            if self.ssh_cmd(
                "aws s3 sync --only-show-errors {0}/flag/{1} {2}flag/{1}".format(
                    self.s3InFolder, c, self.atlasOutDir
                )
            ):
                finish("Failed to sync {}/{}".format(self.s3InFolder, c), -1)

        atlasConfig = self.setup_config(file_preface="")
        jarFile = self.setup_jar()

        cmd = (
            "java -cp {}".format(jarFile)
            + " org.openstreetmap.atlas.checks.maproulette.MapRouletteUploadCommand"
            + " -maproulette='{}:{}:{}'".format(self.mrURL, self.mrProject, self.mrkey)
            + " -logfiles='{}flag'".format(self.atlasOutDir)
            + " -outputPath='{}'".format(self.atlasOutDir)
            + " -config='{}'".format(atlasConfig)
            + " -checkinComment='#AtlasChecks'"
            + " -countries='{}'".format(self.countries)
            + " -checks='{}'".format(self.checks)
            + " -includeFixSuggestions=true"
            + " > {} 2>&1".format(self.atlasCheckLog)
        )

        logger.info("Starting mr upload: {}".format(cmd))
        if self.ssh_cmd(cmd, verbose=True):
            finish("Unable to execute spark job", -1)

    def clean(self):
        """Clean a running Instance of all produced folders and files

        This readies the instance for a clean atlas check run or terminates an EC2
        instance completely.

        Dependencies:
          - self.instanceId - indicates a running instance or "" to create one
          - self.terminate - indicates if the EC2 instance should be terminated
        """
        if self.terminate:
            logger.info("Terminating EC2 instance.")
            self.terminate_instance()
        else:
            logger.info("Cleaning up EC2 instance.")
            cmd = "rm -rf {}/* {}/* ".format(self.atlasOutDir, self.atlasLogDir)
            if self.ssh_cmd(cmd):
                finish("Unable to clean", -1)

    def create_instance(self):
        """Create Instance from atlas_checks-ec2-template template

        Dependencies:
          - self.templateId
          - self.instanceName
        :return:
        """
        logger.info("Creating EC2 instance from {} template.".format(self.templateName))
        try:
            logger.info("Create instance...")
            response = self.ec2.run_instances(
                LaunchTemplate={"LaunchTemplateName": self.templateName},
                TagSpecifications=[
                    {
                        "ResourceType": "instance",
                        "Tags": [{"Key": "Name", "Value": self.instanceName}],
                    }
                ],
                MaxCount=1,
                MinCount=1,
                KeyName=self.key,
            )
            self.instanceId = response["Instances"][0]["InstanceId"]
            logger.info("Instance {} was created".format(self.instanceId))
        except ClientError as e:
            finish(e, -1)

    def terminate_instance(self):
        """Terminate Instance

        Dependencies:
          - self.templateId
        """
        logger.info("Terminating EC2 instance {}".format(self.instanceId))
        try:
            response = self.ec2.terminate_instances(InstanceIds=[self.instanceId])
            logger.info("Instance {} was terminated".format(self.instanceId))
        except ClientError as e:
            finish(e, -1)

    def ssh_connect(self):
        """Connect to an EC2 instance"""
        for _timeout in range(16):
            try:
                keyFile = "{}/.ssh/{}.pem".format(os.environ.get("HOME"), self.key)
                key = paramiko.RSAKey.from_private_key_file(keyFile)
                self.sshClient = paramiko.SSHClient()
                self.sshClient.set_missing_host_key_policy(paramiko.AutoAddPolicy())
                logger.debug(
                    "Connecting to {} ... ".format(self.instance["PublicDnsName"])
                )
                self.sshClient.connect(
                    self.instance["PublicDnsName"], username="ubuntu", pkey=key
                )
                logger.info(
                    "Connected to {} ... ".format(self.instance["PublicDnsName"])
                )
                self.scpClient = scp.SCPClient(self.sshClient.get_transport())
                break
            except AuthenticationException as error:
                logger.error(
                    "Authentication failed: did you remember to create an SSH key? {error}"
                )
                raise error
            except paramiko.ssh_exception.NoValidConnectionsError:
                time.sleep(15)
                continue

    def put_files(self, localFiles, remoteDirectory):
        """Put files from local system onto running EC2 instance"""
        if self.scpClient is None:
            self.ssh_connect()
        try:
            self.scpClient.put(localFiles, remoteDirectory)
        except scp.IOException as error:
            logger.error("Unable to copy files. {error}")
            raise error
        logger.debug("Files: " + localFiles + " uploaded to: " + remoteDirectory)

    def get_files(self, remoteFiles, localDirectory):
        """Get files from running ec2 instance to local system"""
        if self.scpClient is None:
            self.ssh_connect()
        try:
            self.scpClient.get(remoteFiles, localDirectory)
        except scp.SCPException as error:
            logger.error("Unable to copy files. {error}")
            raise error
        logger.debug("Files: " + remoteFiles + " downloaded to: " + localDirectory)

    def ssh_cmd(self, cmd, quiet=False, verbose=False):
        """Issue an ssh command on the remote EC2 instance

        :param cmd: the command string to execute on the remote system
        :param quiet: If true, don't display errors on failures
        :returns: Returns the status of the completed ssh command.
        """
        if self.key is not None:
            if self.sshClient is None:
                self.ssh_connect()
            logger.debug("Issuing remote command: {} ... ".format(cmd))
            ssh_stdin, ssh_stdout, ssh_stderr = self.sshClient.exec_command(cmd)
            if ssh_stdout.channel.recv_exit_status() and not quiet:
                logger.error(" Remote command stderr:")
                logger.error("\t".join(map(str, ssh_stderr.readlines())))
            if verbose:
                logger.info(" Remote command stdout:")
                logger.info("\t".join(map(str, ssh_stdout.readlines())))
            return ssh_stdout.channel.recv_exit_status()

        # if key was not specified then try to use ssm
        logger.debug("Issuing remote command: {} ... ".format(cmd))
        while True:
            try:
                response = self.ssmClient.send_command(
                    InstanceIds=[self.instanceId],
                    DocumentName='AWS-RunShellScript',
                    Parameters={'commands': [cmd]}
                )
                break
            except ClientError as e:
                logger.debug(f'{e}')
                time.sleep(5)

        time.sleep(1)
        command_id = response['Command']['CommandId']
        for _timeout in range(self.timeoutMinutes * 60):
            feedback = self.ssmClient.get_command_invocation(CommandId=command_id, InstanceId=self.instanceId)
            if feedback['StatusDetails'] != 'InProgress':
                break
            time.sleep(1)
        if feedback['StatusDetails'] != 'Success':
            if not quiet:
                logger.error("feedback: " + feedback['StatusDetails'])
                logger.error(" Remote command stderr:")
                logger.error(feedback['StandardErrorContent'])
            return -1
        if verbose:
            logger.info(" Remote command stdout:")
            logger.info(feedback['StandardOutputContent'])
        return 0


    def wait_for_process_to_complete(self):
        """Wait for process to complete

        Will block execution while waiting for the completion of the spark
        submit on the EC2 instance. Upon completion of the script it will look
        at the log file produced to see if it completed successfully. If the
        Atlas Checks spark job failed then this function will exit.

        :returns: 0 - if Atlas check spark job completed successfully
        :returns: 1 - if Atlas check spark job timed out
        """
        logger.info("Waiting for Spark Submit process to complete...")
        # wait for up to TIMEOUT minutes for the VM to be up and ready
        for _timeout in range(self.timeoutMinutes):
            if not self.is_process_running("SparkSubmit"):
                logger.info("Atlas Check spark job has completed.")
                if self.ssh_cmd(
                    "grep 'Success!' {}/_*".format(self.atlasOutDir), quiet=True
                ):
                    logger.error("Atlas Check spark job Failed.")
                    logger.error(
                        "---tail of Atlas Checks Spark job log output ({})--- \n".format(
                            self.atlasCheckLogName
                            + " ".join(
                                map(
                                    str,
                                    open(self.atlasCheckLogName, "r").readlines()[-50:],
                                )
                            )
                        )
                    )
                    finish(status=-1)
                return 0
            time.sleep(60)
        return 1

    def is_process_running(self, process):
        """Indicate if process is actively running

        Uses pgrep on the EC2 instance to detect if the process is
        actively running.

        :returns: 0 - if process is NOT running
        :returns: 1 - if process is running
        """
        if self.ssh_cmd("pgrep -P1 -f {}".format(process), quiet=True):
            return 0
        logger.debug("{} is still running ... ".format(process))
        return 1

    def start_ec2(self):
        """Start EC2 Instance."""
        logger.info("Starting the EC2 instance.")

        try:
            logger.info("Start instance")
            response = self.ec2.start_instances(InstanceIds=[self.instanceId])
            logger.debug(response)
        except ClientError as e:
            logger.error(e)

    def stop_ec2(self):
        """Stop EC2 Instance."""
        logger.info("Stopping the EC2 instance.")

        try:
            response = self.ec2.stop_instances(InstanceIds=[self.instanceId])
            logger.debug(response)
        except ClientError as e:
            logger.error(e)

    def get_instance_info(self):
        """Get the info for an EC2 instance.

        Given an EC2 instance ID this function will retrieve the instance info
        for the instance and save it in self.instance.
        """
        logger.info("Getting EC2 Instance {} Info...".format(self.instanceId))
        # wait for up to TIMEOUT seconds for the VM to be up and ready
        for _timeout in range(10):
            response = self.ec2.describe_instances(InstanceIds=[self.instanceId])
            if not response["Reservations"]:
                finish("Instance {} not found".format(self.instanceId), -1)
            if (
                response["Reservations"][0]["Instances"][0].get("PublicIpAddress")
                is None
            ):
                logger.info(
                    "Waiting for EC2 instance {} to boot...".format(self.instanceId)
                )
                time.sleep(6)
                continue
            self.instance = response["Reservations"][0]["Instances"][0]
            logger.info(
                "EC2 instance: {} booted with name: {}".format(
                    self.instanceId, self.instance["PublicDnsName"]
                )
            )
            break
        for _timeout in range(100):
            if self.ssh_cmd("systemctl is-system-running", quiet=True):
                logger.debug(
                    "Waiting for systemd on EC2 instance to complete initialization..."
                )
                time.sleep(6)
                continue
            return
        finish("Timeout while waiting for EC2 instance to be ready", -1)


def parse_args():
    """Parse user parameters

    :returns: args
    """
    parser = argparse.ArgumentParser(
        description="This script automates the use of EC2 instance to execute "
        "an atlas-checks spark job. It is meant to be executed on a laptop with "
        "access to the EC2 instance"
    )
    parser.add_argument(
        '--zone',
        default=AWS_REGION,
        type=str,
        help=f"The AWS region to use. e.g. {AWS_REGION}",
    )
    parser.add_argument(
        "--name",
        help="Set EC2 instance name.",
    )
    parser.add_argument(
        "--template",
        help="Set EC2 template name to create instance from.",
    )
    parser.add_argument(
        "--minutes",
        type=int,
        help="Set process timeout to number of minutes.",
    )
    parser.add_argument(
        "--version", help="Display the current version", action="store_true"
    )
    parser.add_argument(
        "--terminate",
        default=False,
        help="Terminate EC2 instance after successful execution",
        action="store_true",
    )
    subparsers = parser.add_subparsers(
        title="commands",
        description="One of the following commands must be specified when executed. "
        "To see more information about each command and the parameters that "
        "are used for each command then specify the command and "
        "the --help parameter.",
    )

    parser_check = subparsers.add_parser(
        "check",
        help="Execute Atlas Checks and, if '--output' is set, then push atlas check results to S3 folder",
    )
    parser_check.add_argument(
        "--id", help="ID - Indicates the ID of an existing EC2 instance to use"
    )
    parser_check.add_argument(
        "--key",
        required=True,
        help="KEY - Instance key name to use to login to instance. This key "
        "is expected to be the same name as the key as defined by AWS and the "
        "corresponding pem file must be located in your local '~/.ssh/' "
        "directory and should be a pem file. See the following URL for "
        "instructions on creating a key: "
        "https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html. "
        "(e.g. `--key=aws-key`)",
    )
    parser_check.add_argument(
        "--output",
        help="Out - The S3 Output directory. (e.g. '--out=s3://atlas-bucket/atlas-checks/output')",
    )
    parser_check.add_argument(
        "--mount",
        default=False,
        help="Flag to indicate if s3fs should be used to mount input directory. (Default: False)",
    )
    parser_check.add_argument(
        "--input",
        required=True,
        help="IN - The S3 Input directory that contains atlas file directories and sharding.txt. "
        "(e.g. s3://bucket/path/to/atlas/file/dir/)",
    )
    parser_check.add_argument(
        "--countries",
        required=True,
        help="COUNTRIES - A comma separated list of ISO3 codes. (e.g. --countries=GBR)",
    )
    parser_check.add_argument(
        "--memory",
        type=int,
        help="MEMORY - Gigs of memory for spark job.",
    )
    parser_check.add_argument(
        "--formats",
        help="FORMATS - Output format",
    )
    parser_check.add_argument(
        "--config",
        required=True,
        help="CONFIG - s3://path/to/configuration.json, http://path/to/configuration.json, "
        " or /local/path/to/configuration.json to use as configuration.json for atlas-checks ",
    )
    parser_check.add_argument(
        "--processes",
        type=int,
        help="PROCESSES - Number of parallel jobs to start.",
    )
    parser_check.add_argument(
        "--jar",
        required=True,
        help="JAR - s3://path/to/atlas_checks.jar or /local/path/to/atlas_checks.jar to execute",
    )
    parser_check.add_argument(
        "--info",
        help="INFO - Json string to add to the 'INFO' file in the output folder "
        "(e.g. --tag='{\"version\":\"1.6.3\"}')",
    )
    parser_check.set_defaults(func=CloudAtlasChecksControl.atlasCheck)

    parser_sync = subparsers.add_parser(
        "sync", help="Sync Atlas Check output files from instance to S3 folder"
    )
    parser_sync.add_argument(
        "--id",
        required=True,
        help="ID - Indicates the ID of an existing EC2 instance to use",
    )
    parser_sync.add_argument(
        "--key",
        required=True,
        help="KEY - Instance key name to use to login to instance. This key "
        "is expected to be the same name as the key as defined by AWS and the "
        "corresponding pem file must be located in your local '~/.ssh/' "
        "directory and should be a pem file. See the following URL for "
        "instructions on creating a key: "
        "https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html. "
        "(e.g. `--key=aws-key`)",
    )
    parser_sync.add_argument(
        "--output", required=True, help="Out - The S3 Output directory"
    )
    parser_sync.set_defaults(func=CloudAtlasChecksControl.sync)

    parser_mr = subparsers.add_parser(
        "challenge", help="Create a Map Roulette Challenge"
    )
    parser_mr.add_argument(
        "--id",
        help="ID - Indicates the ID of an existing EC2 instance to use",
    )
    parser_mr.add_argument(
        "--project",
        required=True,
        help="PROJECT - Indicates the name to use to create the map roulette project",
    )
    parser_mr.add_argument(
        "--mrkey",
        required=True,
        help="MRKEY - The api|key to use to connect to Map Roulette",
    )
    parser_mr.add_argument(
        "--countries",
        required=True,
        help="COUNTRIES - A comma separated list of ISO3 codes. (e.g. --countries=GBR)",
    )
    parser_mr.add_argument(
        "--checks",
        required=True,
        help="CHECKS - A comma separated list of checks names to include in project. "
        "(e.g. --checks='EdgeCrossingEdgeCheck,SinkIslandCheck')",
    )
    parser_mr.add_argument(
        "--key",
        required=True,
        help="KEY - Instance key name to use to login to instance. This key "
        "is expected to be the same name as the key as defined by AWS and the "
        "corresponding pem file must be located in your local '~/.ssh/' "
        "directory and should be a pem file. See the following URL for "
        "instructions on creating a key: "
        "https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html. "
        "(e.g. `--key=aws-key`)",
    )
    parser_mr.add_argument(
        "--config",
        required=True,
        help="CONFIG - Path within the S2 Input bucket or a URL to a json file to "
        "use as configuration.json for atlas-checks (Default: Latest from atlas-config repo)",
    )
    parser_mr.add_argument(
        "--input",
        required=True,
        help="INPUT - The s3 Atlas Files Output directory to use as input for challenge. "
        "(e.g. '--input=s3://atlas-bucket/atlas-checks/output')",
    )
    parser_mr.add_argument(
        "--jar",
        help="JAR - The full path to the jar file to execute.",
    )
    parser_mr.set_defaults(func=CloudAtlasChecksControl.challenge)

    parser_clean = subparsers.add_parser("clean", help="Clean up instance")
    parser_clean.add_argument(
        "--id",
        required=True,
        help="ID - Indicates the ID of an existing EC2 instance to use",
    )
    parser_clean.add_argument(
        "--key",
        required=True,
        help="KEY - Instance key name to use to login to instance. This key "
        "is expected to be the same name as the key as defined by AWS and the "
        "corresponding pem file must be located in your local '~/.ssh/' "
        "directory and should be a pem file. See the following URL for "
        "instructions on creating a key: "
        "https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html. "
        "(e.g. `--key=aws-key`)",
    )
    parser_clean.set_defaults(func=CloudAtlasChecksControl.clean)

    args = parser.parse_args()
    return args


def evaluate(args, cloudctl):
    """Evaluate the given arguments.

    :param args: The user's input.
    :param cloudctl: An instance of CloudAtlasChecksControl to use.
    """
    if args.version is True:
        logger.critical("This is version {0}.".format(VERSION))
        finish()
    if args.name is not None:
        cloudctl.instanceName = args.name
    if args.template is not None:
        cloudctl.templateName = args.templateName
    if args.minutes is not None:
        cloudctl.timeoutMinutes = args.minutes
    if hasattr(args, "input") and args.input is not None:
        cloudctl.s3InFolder = args.input
    if hasattr(args, "mount") and args.mount is not None:
        cloudctl.s3fsMount = args.mount
    if hasattr(args, "processes") and args.processes is not None:
        cloudctl.processes = args.processes
    if hasattr(args, "key") and args.key is not None:
        cloudctl.key = args.key
    if hasattr(args, "output") and args.output is not None:
        cloudctl.s3OutFolder = args.output
    if hasattr(args, "countries") and args.countries is not None:
        cloudctl.countries = args.countries
    if hasattr(args, "formats") and args.formats is not None:
        cloudctl.formats = args.formats
    if hasattr(args, "memory") and args.memory is not None:
        cloudctl.memory = args.memory
    if hasattr(args, "config") and args.config is not None:
        cloudctl.atlasConfig = args.config
    if hasattr(args, "checks") and args.checks is not None:
        cloudctl.checks = args.checks
    if hasattr(args, "mrkey") and args.mrkey is not None:
        cloudctl.mrkey = args.mrkey
    if hasattr(args, "project") and args.project is not None:
        cloudctl.mrProject = args.project
    if hasattr(args, "jar") and args.jar is not None:
        cloudctl.jar = args.jar
    if hasattr(args, "info") and args.jar is not None:
        cloudctl.info = args.info
    if hasattr(args, "id") and args.id is not None:
        cloudctl.instanceId = args.id
        cloudctl.get_instance_info()

    if hasattr(args, "func") and args.func is not None:
        args.func(cloudctl)
    else:
        finish("A command must be specified. Try '-h' for help.")


logger = setup_logging()

if __name__ == "__main__":
    args = parse_args()
    cloudctl = CloudAtlasChecksControl(
        terminate=args.terminate,
        awsRegion=args.zone,
    )
    evaluate(args, cloudctl)
    finish()

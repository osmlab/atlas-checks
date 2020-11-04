# cloudAtlasCheckControl - EC2 Atlas Checks Controller

## Prerequisites

### AWS EC2 and S3

To execute the entire Atlas Check process the user will need access to an AWS account with EC2 and S3 resources. Please visit the [AWS website](https://aws.amazon.com/) to create and manage your account. This script makes use of [AWS EC2](https://aws.amazon.com/ec2/) resources to execute the Atlas Check Spark job and [AWS S3](https://aws.amazon.com/s3) object store to save the Atlas Checks results. To communicate with the AWS console and control the resources the scripts needs access the [AWS CLI](https://aws.amazon.com/cli/). To execute the AWS CLI you will need an "Access Key" and "Secret Access Key". These you will need to get from your AWS administrator. You will also need to set the default region. (e.g. "us-west-1").

- [Install](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
  and then
  [configure](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html)
  AWS CLI

### AWS Key Pair

To be able to execute commands on the EC2 instance that is created the scripts need to be able to ssh to the EC2 instance. To allow the script to ssh to the EC2 instance you need to create an AWS key pair.

- [Create an AWS key pair](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html#having-ec2-create-your-key-pair)

Make sure that your "my-key-pair.pem" file that is produced during this process is placed in your ~/.ssh/ directory and that the permissions are set correctly as the instructions indicate. Once a key pair has been created you may also need to adjust your [AWS security Groups](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-security-groups.html) to allow ssh access from your local server. Please see your administrator for information on whether this is required for your setup.

### AWS EC2 Template

This script takes advantage of the ability for AWS to start EC2 instances from templates that have been created from an image of another EC2 image. This scrip assumes that you have created a template from an image of an EC2 instance that you will use to start new instances. Information of [Launch Templates](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-launch-templates.html?icmpid=docs_ec2_console) is available from AWS, but for the purposes of this script and document we will assume that you have installed the python environment on an EC2 instance, created an image from that and then created a launch template from that image to use in the Atlas Checks process.

### Python3

Make sure python3 is installed. Instructions below for Mac. Also make sure that you source your .zshrc or restart your shell after you perform these steps.

```
brew install pyenv
brew install python
pyenv install 3.8.5
pyenv global 3.8.5
echo -e 'if command -v pyenv 1>/dev/null 2>&1; then\n  eval "$(pyenv init -)"\nfi' >> ~/.zshrc
```

### Python libraries

Install python libraries necessary to execute the application.

- boto3 - the python libraries to control EC2 instances
- paramiko - ssh agent so the script can ssh to the EC2 instance.

```
sudo pip install boto3 paramiko
```

## Running the cloudAtlasCheckControl.py script

There are three major commands you can use with the cloudAtlasCheckControl.py script. Each one has its own help. The main help display shows the flags and parameters that work with all the commands. The following parameters apply to all commands and must be used on the command line before the command.

- `-h, --help` - Show help message and exit
- `-n NAME, --name NAME` - If creating an EC2 instance, this NAME will be used to override the default EC2 instance name: 'AtlasChecks'. The script doesn't use the EC2 instance name so this can be set to any value that the user would like to use.
- `-t TEMPLATE, --template TEMPLATE` - This parameter sets EC2 template name that will be used to create the EC2 instance from. If used, this parameter will override the default: 'atlas_checks-ec2-template'. At this time a template MUST be specified to operate properly.
- `-m MINUTES, --minutes MINUTES` - This parameter will set the timeout, in minutes, that the script will use when waiting for the Atlas Check spark job to complete. The default is 6000 minutes.
- `-v, --version` - Display the current version of the script.
- `-T, --terminate` - This flag indicates that the user would like to terminate the EC2 instance after successful operation. If this flag is not specified then the script will leave any EC2 instance used or created running upon completion of the script. There are a few different scenarios where the script will not terminate even if termination is requested. The script will NOT terminate an EC2 instance if an error is encountered when processing any command. The script will also not terminate an EC2 instance if the check command is performed but the sync command is skipped (see --out parameter).

The following parameters are used by one or more of the commands. These parameters may not apply to all command so see the command help file for which of these parameters are accepted, required, or optional for each command.

- `-k KEY, --key KEY` - This parameter is the AWS key pair name created above. This parameter specifies the name of the key as specified in the AWS console. The similarly named pem file must be located in the user's ~/.ssh/ directory. See the following URL for instructions on creating a key: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html. (e.g. `--key=aws-key`)
- `-i ID, --id ID` - This parameter specifies the ID of an existing VM instance to use. If this parameter is specified then the command that is being executed will NOT create a new EC2 instance and will, instead, attempt to connect to an EC2 instance with the given ID. Please note that this is the ID of the instance and not the name or description of the EC2 instance. The script will indicate the ID of the EC2 instance used in the log whether an EC2 instance is created or a running EC2 instance is used.
- `-o OUT, --output OUT` - The S3 Output directory to push output results upon successful completion of Atlas Check spark job processing. If this parameter is not specified when executing Atlas Checks then the output of the checks will not be pushed to an external object store and the EC2 instance will not be terminated even if -T is used. (e.g. '--out=atlas-bucket/Atlas_Checks')
- `-i IN, --input IN` - The S3 Input directory where atlas files are located to execute checks on. This S3 bucket will be mounted to the EC2 instance for readonly processing of the atlas files.
- `-p PROCESSES, --processes PROCESSES` - The number of parallel osmium processes to start. Note that when processing a large PBF file each osmium process will use a great deal of memory so small numbers of parallel processes is suggested. (Default: 32)
- `-j CONFIG, --config CONFIG` - The json file to use as an [Atlas configuration file](https://github.com/osmlab/atlas-checks/blob/dev/docs/configuration.md) to control the Atlas Checks process. This can either be a fully qualified URL or a full path to a json file in the S3 Input bucket. (e.g my-s3-bucket/Atlas_Checks/configurations/special_config.json) (Default: https://raw.githubusercontent.com/osmlab/atlas-checks/dev/config/configuration.json)
- `-f FORMATS --formats FORMATS` - A comma separated list of formats to use to determine the output format for Atlas Checks. (Default: 'flags')
- `-m MEMORY,--memory MEMORY` - The Maximum amount of memory in GB for the Spark job to use. (Default: 256GB)
- `-c COUNTRIES, --countries COUNTRIES` - A comma separated list of ISO3 country codes to perform the checks on.

### check

The check command is used to perform the Atlas Checks on an EC2 instance. If the --id parameter is specified for this command then the script will use an already running EC2 instance, otherwise the script will create a new instance from a launch template. Once connected to the EC2 instance the Atlas Checks spark job will be started as specified by the parameters and then monitor the EC2 instance for completion of the spark job. If the spark job completes successfully then the script will push results to an S3 bucket if the --out parameter is specified.

Required Parameters: `-k` or `--key`, `-i` or `--input`

Example: Create an EC2 Instance, Execute all the atlas checks, push flag output results to S3 bucket, and terminate the EC2 instance.

```
./cloudAtlasCheckControl.py --terminate check --key=my-key --input=my-s3-bucket/Atlas_Generator/Atlas_Files --out=my-s3-bucket/Atlas_Checks/results/
```

Example: The same as above except that this time only perform test on the USA and use 50 processes in parallel. Also, use the special_config.json file to indicate different settings for this Atlas Checks execution. Generate geojson and flags out output and push them to the S3 bucket. Terminate instance when complete.

```
./cloudAtlasCheckControl.py --terminate check --key=my-key --input=my-s3-bucket/Atlas_Generator/Atlas_Files --out=my-s3-bucket/Atlas_Checks/results/ --countries=USA --processes=50 --formats=flags,geojson --config=my-s3-bucket/Atlas_Checks/configurations/special_config.json
```

### sync

The sync command can be used to connect to an instance that is running and sync the resulting Atlas Checks output files from a previous run to S3. This command is generally used after a successful Atlas Checks execution when the `out` parameter was not provided during the check command execution. It may also be used to re-sync Atlas Checks result files from a running instance to the S3 bucket.

Required Parameters: `-i` or `--id`, `-k` or `--key`, `-o` or `--out`

Example: Push Atlas Checks results that were produced on a running instance to the S3 bucket indicated.

```
./cloudAtlasCheckControl.py sync --key=my-key --output=mmy-s3-bucket/Atlas_Checks/results/ --id=i-0d48466b0e91ef786
```

### clean

Clean can be used to clean up a running instance to prep it for a fresh Atlas Checks run. It can also be used to terminate a running instance without doing anything else by specifying the global `--terminate` flag.

Required Parameters: `-i` or `--id`, `-k` or `--key`

Example: Clean up a running EC2 instance to prep for a new Atlas Check execution.

```
./cloudAtlasCheckControl.py clean --key=my-key --id=i-0d48466b0e91ef786
```

Example: Terminate a running EC2 instance.

```
./cloudAtlasCheckControl.py --terminate clean --key=my-key --id=i-0d48466b0e91ef786
```

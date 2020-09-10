""" Purpose: Upload flags from one or more log files to MapRoulette
    Usage: python maproulette_upload_command.py \
                        --input_folder <folder containing the input files>  \
                        --check_config <the file containing the checks configurable parameters> \
                        --project_id <the ID of the project to post the new challenges and tasks to>    \
                        --countries <an optional comma-separated list of ISO3 country codes to filter flags by> \
                        --checks <an optional comma-separated list of check names to filter flags by>

    Author: matt.manley@critigen.com
    Date: 09/09/2020
"""


import click
from maproulette import Configuration, Challenge, Task
from utilities import *
from task_deserializer import construct_task
from challenge_deserializer import get_check_name, construct_challenge


@click.command()
@click.option('--input_folder', required=True, type=str)
@click.option('--mr_hostname', required=True, type=str)
@click.option('--mr_api_key', required=True, type=str)
@click.option('--mr_cert_file_path', required=False, default=None, type=str)
@click.option('--mr_key_file_path', default=None, type=str)
@click.option('--mr_verify', is_flag=True)
@click.option('--checks_config', required=True, type=str)
@click.option('--project_id', required=True, type=int)
@click.option('--countries', default=None, type=str)
@click.option('--checks', default=None, type=str)
def main(input_folder, mr_hostname, mr_api_key, mr_cert_file_path, mr_key_file_path, mr_verify, checks_config,
         project_id, countries, checks):
    """Post flags from one or more log files to MapRoulette

    :param input_folder: the input directory that contains the log files to be uploaded to MapRoulette
    :param mr_hostname: the hostname of the MapRoulette instance being addressed
    :param mr_api_key: the user-specific API key for the MapRoulette instance being addressed
    :param mr_cert_file_path: optional parameter to specify the path to the client side certificate file
    :param mr_key_file_path: optional parameter to specify the path to the client key file
    :param mr_verify: optional parameter to specify whether to verify SSL certificates for HTTPS requests. Default is
        False
    :param checks_config: the file containing the checks configurable parameters
    :param project_id: the ID of the project to post the new challenges and tasks to
    :param countries: an optional comma-separated list of ISO3 country codes to filter flags by
    :param checks: an optional comma-separated list of check names to filter flags by
    :return: None
    """
    # Access the checks config settings
    check_config_values = read_config(checks_config)

    # If the use of certs is enabled, use them to create the MapRoulette Configuration object
    if mr_cert_file_path and mr_key_file_path:
        certs = (mr_cert_file_path, mr_key_file_path)
    else:
        certs = None

    # Create the Configuration object
    maproulette_configuration = Configuration(
        hostname=mr_hostname,
        api_key=mr_api_key,
        certs=certs,
        verify=mr_verify
    )

    input_directory = os.fsencode(input_folder)
    project_id = int(project_id)
    challenge_api = Challenge(maproulette_configuration)
    task_api = Task(maproulette_configuration)

    # Iterate over valid files in the input directory
    for file in os.listdir(input_directory):
        filepath = get_filepath(input_directory, file)
        # Filter out any files that do not have valid extensions
        if os.fsdecode(file).endswith((ValidFileTypes.COMPRESSED_LOG.value, ValidFileTypes.LOG.value)):
            # Read the log or compressed log
            data = read_log(filepath)
            # If the user specified to filter on specified checks, filter the log file to just those outputs
            if checks:
                check_list = checks.split(',')
                # Filter the list of logs to just those with valid check names
                data = [d for d in data if next(iterate_json(d, LogAttributes.GENERATOR.value))
                        in check_list]
            # Get a list of unique checks to create challenges for
            filtered_checks = {get_check_name(d) for d in data}
            # Iterate over each check
            for check in filtered_checks:
                # Create a ChallengeModel for each check using parameters from the checks config
                challenge_model = construct_challenge(
                    config_object=check_config_values,
                    check_name=check,
                    project_id=project_id
                )
                # Post the challenge
                response = challenge_api.create_challenge(challenge_model.to_dict())
                # Store the challenge ID
                challenge_id = response['data']['id']
                # Determine what tasks should belong to this challenge
                flags = [d for d in data if get_check_name(d) in [check]]
                tasks = []
                # Iterate over each flag and create a TaskModel each
                for flag in flags:
                    task_model = construct_task(flag, challenge_id).to_dict()
                    tasks.append(task_model)
                # Post the tasks to the challenge
                print(task_api.create_tasks(data=tasks))


if __name__ == "__main__":
    main()

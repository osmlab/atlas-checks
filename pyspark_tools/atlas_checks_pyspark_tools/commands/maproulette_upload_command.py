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

import urllib3
from maproulette import Configuration, Project, Challenge, Task, ProjectModel
from maproulette.api.errors import NotFoundError
from atlas_checks_pyspark_tools.maproulette_utilities.utilities import *
from atlas_checks_pyspark_tools.maproulette_utilities.task_deserializer import construct_task
from atlas_checks_pyspark_tools.maproulette_utilities.challenge_deserializer import get_check_name, construct_challenge

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)


def run(input_folder, hostname, api_key, cert_fp, key_fp, verify, checks_config,
        project_name, countries, checks):

    # Access the checks config settings
    check_config_values = read_config(checks_config)

    # If the use of certs is enabled, use them to create the MapRoulette Configuration object
    if cert_fp and key_fp:
        certs = (cert_fp, key_fp)
    else:
        certs = None

    # Create the Configuration object
    maproulette_configuration = Configuration(
        hostname=hostname,
        api_key=api_key,
        certs=certs,
        verify=verify
    )

    input_directory = os.fsencode(input_folder)
    project_api = Project(maproulette_configuration)
    try:
        project_id = project_api.get_project_by_name(project_name=project_name)['data']['id']
    except NotFoundError:
        project_model = ProjectModel(name=project_name, description='')
        response = project_api.create_project(project_model)
        project_id = response['data']['id']

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

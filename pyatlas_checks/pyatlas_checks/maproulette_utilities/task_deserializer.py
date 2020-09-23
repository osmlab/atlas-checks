from maproulette import TaskModel
from pyatlas_checks.maproulette_utilities.utilities import *


def task_deserializer(data):
    """Parses a log file to obtain various parameters

    :param data: the log data to parse
    :return: a dict containing task parameters
    """
    features = data.get(LogAttributes.FEATURES.value)
    properties = data.get(LogAttributes.PROPERTIES.value)
    challenge_name = properties.get(LogAttributes.GENERATOR.value)
    instructions = properties.get(LogAttributes.INSTRUCTIONS.value)
    task_id = format_identifiers(properties.get(LogAttributes.IDENTIFIERS.value))

    return {
        'features': features,
        'properties': properties,
        'challenge_name': challenge_name,
        'instructions': instructions,
        'task_id': task_id
    }


def construct_task(flag, challenge_id):
    """Creates a TaskModel object for a given flag in a log file

    :param flag: the flag to convert to a task
    :param challenge_id: the ID of the challenge to set as the parent parameter
    :return: a TaskModel object
    """
    task_params = task_deserializer(data=flag)
    return TaskModel(
        name=task_params.get('task_id'),
        parent=challenge_id,
        geometries=flag,
        instruction=task_params.get('instructions')
    )


def format_identifiers(identifiers):
    """Properly formats an array of identifiers from a log as a comma-separated string

    :param identifiers: the arry of identifiers from a log
    :return: a comma separated string of identifiers
    """
    if isinstance(identifiers, list):
        return ','.join(map(str, identifiers))

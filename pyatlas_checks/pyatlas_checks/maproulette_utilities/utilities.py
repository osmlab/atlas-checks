import gzip
import json
import os
from enum import Enum


def get_filepath(directory, file):
    """Returns the absolute path of the specified file

    :param directory: the directory that the file is contained within
    :param file: the file name
    :return: the absolute path of the file
    """
    return os.path.abspath(os.path.join(directory, file))


def read_log(file):
    """Read log file depending on the format

    :param file: the log file to read
    :return: a list of dictionaries that represent the log file
    """
    json_list = []
    if os.fsdecode(file).endswith(ValidFileTypes.COMPRESSED_LOG.value):
        with gzip.open(file, 'r') as f:
            for line in f:
                json_list.append(json.loads(line))
    else:
        with open(file, 'r') as f:
            for line in f:
                json_list.append(json.loads(line))
    return json_list


def read_config(config_path):
    """Parses a JSON config file

    :return: a dictionary representing the JSON config file
    """
    return json.load(open(config_path, "r"))


def iterate_json(json_object, target_key):
    """Iterate recursively through dictionaries and lists to find the specified key and yield its associated value

    :param json_object: A dictionary object that represents a feature from the log
    :param target_key: A key to search through the nested dictionary
    :return: A generator iterator object containing all of the specified key strings in the json_object
    """
    if isinstance(json_object, dict):
        for key, value in json_object.items():
            if key == target_key:
                yield value
            else:
                yield from iterate_json(value, target_key)
    elif isinstance(json_object, list):
        for item in json_object:
            yield from iterate_json(item, target_key)


class ExtendedEnum(Enum):
    """An extended enumeration that allows for iterating over the enumeration objects"""
    def _generate_next_value_(name, start, count, last_values):
        return name.lower()

    @classmethod
    def list(cls):
        return [i.value for i in cls]


class ValidFileTypes(ExtendedEnum):
    """An enumeration of valid file types"""
    LOG = ".log"
    COMPRESSED_LOG = ".log.gz"


class LogAttributes(ExtendedEnum):
    """An enumeration of log attributes"""
    PROPERTIES = "properties"
    GENERATOR = "generator"
    INSTRUCTIONS = "instructions"
    ID = "id"
    IDENTIFIERS = "identifiers"
    FEATURES = "features"
    GEOMETRY = "geometry"
    COORDINATES = "coordinates"
    ISO_COUNTRY_CODE = "iso_country_code"


class CheckConfigParameters(ExtendedEnum):
    """An enumeration of check config parameters"""
    NAME = "name"
    DESCRIPTION = "description"
    BLURB = "blurb"
    INSTRUCTION = "instruction"
    DIFFICULTY = "difficulty"
    DEFAULT_PRIORITY = "defaultPriority"
    HIGH_PRIORITY_RULE = "highPriorityRule"
    MEDIUM_PRIORITY_RULE = "mediumPriorityRule"
    LOW_PRIORITY_RULE = "lowPriorityRule"
    KEYWORDS = "tags"
    CHECK_IN_COMMENT = "checkinComment"
    DEFAULT_EMPTY_STRING = ""
    DEFAULT_NONE_TYPE = None


class ValidDifficultyValues(ExtendedEnum):
    """An enumeration of valid difficulty setting values"""
    EASY = 1
    NORMAL = 2
    EXPERT = 3


class ValidDefaultPriorityValues(ExtendedEnum):
    """An enumeration of valid default priority setting values"""
    LOW = 2
    MEDIUM = 1
    HIGH = 0

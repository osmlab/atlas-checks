"""
Purpose: The puspose of this script is to convert Atlas Checks line delimited json output files into check separated .log files.
Inputs: Path to Atlas Checks flag folder containing .log files
Outputs: Path to save the check separated .log files
Original Author: Daniel Baah
Updates: Micah Nacht
"""


import json
import argparse
import os
import collections
import gzip


def get_log_files(logs_path):
    """
    Fetch all line-delimited feature from directory of .log files
    Parse each feature and create a dictionary of checks & their features
    :param logs_path: path to directory of log files
    :return: checks dictionary. {checkName: [features] }
    """
    check_names_dict = collections.defaultdict(list)
    for file in os.listdir(logs_path):
        name, extension = os.path.splitext(file)
        if extension == ".log" or (extension == ".gz" and name.endswith(".log")):
            if extension == ".log":
                with open(os.path.join(logs_path, file), "r") as line_delimited_geojson:
                    lines = line_delimited_geojson.readlines()
            elif extension == ".gz":
                with gzip.open(os.path.join(logs_path, file), "rt") as line_delimited_geojson:
                    lines = line_delimited_geojson.readlines()
            for geojson in lines:
                parsed_json = json.loads(geojson)
                check_names_dict[get_check_name(parsed_json)].append(parsed_json)

    return check_names_dict


def get_check_name(geojson):
    """
    Returns the checkName from a FeatureCollection
    :param geojson:
    :return string:
    """
    return geojson.get("properties").get("generator")


def get_check_data(check_name, line_delimited_geojson):
    """
    For a given check, return a filtered list of FeatureCollections
    :param check_name:
    :param line_delimited_geojson:
    :return: filtered list of FeatureCollections
    """
    return list(filter(lambda geojson: get_check_name(geojson) == check_name, line_delimited_geojson))


def write_split_checks(checks_dict, output_directory):
    """
    Write a new, line delimited .log file to disk
    :param checks_dict:
    :param output_directory:
    :return:
    """
    for check_name in checks_dict.keys():
        flag_count = len(checks_dict[check_name])
        location = os.path.join(output_directory, "{}-{}.log".format(check_name, flag_count))
        with open(location, 'w') as f:
            for geojson in checks_dict[check_name]:
                f.write("%s\n" % json.dumps(geojson))
        print("Writing - {} to disk".format(location))


def main():
    """
    Grab arguments from command line
    Create dictionary of checks
    Write new file to disk
    """
    parser = argparse.ArgumentParser()
    parser.add_argument("logs", help="path to log directory")
    parser.add_argument("--output", help="optional path to save log files", default="./output")

    args = parser.parse_args()

    checks_dict = get_log_files(args.logs)
    write_split_checks(checks_dict, args.output)


if __name__ == "__main__":
    main()

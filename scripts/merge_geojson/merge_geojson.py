#!/usr/bin/env python
"""
Merge geojson files of the same type in the same directory, optionally to a
newline delimited geojson file compliant with RFC 7464 for MapRoulette.
"""
import argparse
import geojson
import typing
import glob
import gzip
import re
import os
import json
import logging

ASCII_RECORD_SEPARATOR = b"\x1E"
ASCII_LINE_SEPARATOR = b"\x0A"


def get_geojson_files(country_path: str, flag: str) -> typing.List[str]:
    """
    Get geojson files in a path for a flag
    """
    files = glob.glob(
        os.path.join(country_path, "**", "*" + flag + "*"), recursive=True
    )
    return [g for g in files if re.search(r".*\.geojson(|\.gz)$", g)]


def fix_suggestions_to_cooperativeWork(
    feature: geojson.Feature,
) -> geojson.FeatureCollection:
    """
    This is only useful with RFC7464 compliant geojson. This converts
    fix_suggestions to cooperativeWork tasks. It currently only covers tag
    fixes and not geometry fixes. It also does not support added or removed
    features at this time.
    """
    properties = feature["properties"]
    # The id MUST be in the form of <elementType>/<numericId>, such as node/42
    # Checked id order is id, @id, osmid, osm_id, and name (in order)
    # Currently assuming that there is only one osmIdentifier per feature
    osm_id = properties["feature_properties"][0]["osmIdentifier"]
    if feature["geometry"]["type"] in ["Position", "Point"]:
        feature["id"] = "node/" + osm_id
    elif feature["geometry"]["type"] in [
        "LineString",
        "Polygon",
    ]:  # TODO check polygon
        feature["id"] = "way/" + osm_id
    elif feature["geometry"]["type"] in [
        "MultiPolygon",
        "MultiPoint",
        "MultiLineString",
    ]:
        feature["id"] = "relation/" + osm_id
    # version=2 is required, type=2 is for osc base64 encoded (i.e. geometry
    # changes). type=1 is for tag fixes
    cooperativeWork = {"meta": {"version": 2, "type": 1}}
    operations = []
    for osm_object in properties["fix_suggestions"]:
        ignore_keys = [
            "last_edit_version",
            "last_edit_user_name",
            "last_edit_user_id",
            "last_edit_time",
            "last_edit_changeset",
        ]
        fix_suggestions = properties["fix_suggestions"][osm_object]
        # Cooperative challenges supports setTags and unsetTags
        if fix_suggestions["type"] in ["UPDATE"]:
            add_tags = {}
            del_tags = []
            for descriptor in fix_suggestions["descriptors"]:
                if (
                    descriptor["name"] == "TAG"
                    and descriptor["key"] not in ignore_keys
                ):
                    if descriptor["type"] in ["ADD", "UPDATE"]:
                        add_tags[descriptor["key"]] = descriptor["value"]
                    elif descriptor["type"] in ["REMOVE"]:
                        del_tags.append(descriptor["key"])
                elif descriptor["name"] not in ["TAG"]:
                    logging.error(
                        f'Fixes for {descriptor["name"]} are not yet supported'
                    )
                    logging.debug(str(descriptor))
            if len(add_tags) > 0:
                operations.append({"operation": "setTags", "data": add_tags})
            if len(del_tags) > 0:
                operations.append({"operation": "unsetTags", "data": del_tags})
        else:
            # We currently don't support "ADD" or "REMOVE" for objects
            logging.error(f'Unknown type {fix_suggestions["type"]}')
            logging.debug(str(fix_suggestions))
    cooperativeWork["operations"] = [
        {
            "operationType": "modifyElement",
            "data": {"id": feature["id"], "operations": operations},
        }
    ]
    feature_collection = geojson.FeatureCollection([feature])
    if len(operations) > 0:
        # cooperativeWork goes in the main FeatureCollection body
        feature_collection["cooperativeWork"] = cooperativeWork
        # properties["cooperativeWork"] = cooperativeWork
        del properties["fix_suggestions"]
    return feature_collection


def write_feature_rfc7464(file_name: str, feature: geojson.Feature):
    """
    Create an RFC7464 compliant file for use with MapRoulette
    """
    fc = (
        fix_suggestions_to_cooperativeWork(feature)
        if "fix_suggestions" in feature["properties"]
        else geojson.FeatureCollection([feature])
    )
    with open(file_name, "ab") as fh:
        # Line-by-line uses the 1E byte record separator
        fh.write(ASCII_RECORD_SEPARATOR)

    with open(file_name, "a") as fh:
        string = geojson.dumps(fc)
        fh.write(string)

    with open(file_name, "ab") as fh:
        # Specific line separator (byte used instead of specific character,
        # just in case)
        fh.write(ASCII_LINE_SEPARATOR)


def read_file(file_name: str) -> geojson.FeatureCollection:
    file_type = "gz" if file_name.endswith(".gz") else "geojson"
    try:
        if file_type == "geojson":
            with open(file_name, "r") as fh:
                first = str.encode(fh.read(1))
                if first == ASCII_RECORD_SEPARATOR:
                    return None
                fh.seek(0)
                return geojson.load(fh)
        elif file_type == "gz":
            with gzip.open(file_name, "rt") as fh:
                return geojson.load(fh)
    except json.decoder.JSONDecodeError as e:
        logging.error(f"{file_name} had a json parse error")
        raise e

    raise ValueError(f"{file_name} is not a recognized type")


def line_by_line(files: typing.List[str], check: str):
    write_name = check + ".line.geojson"
    if len(files) > 0:
        write_name = os.path.join(os.path.dirname(files[0]), write_name)
    if os.path.isfile(write_name):
        os.remove(write_name)
    for f in files:
        fc = read_file(f)
        if fc is None:
            continue
        if isinstance(fc, geojson.FeatureCollection):
            for feature in fc["features"]:
                write_feature_rfc7464(write_name, feature)
        else:
            logging.error(f"Bad file? {f}")


def concat_files(files: typing.List[str], check: str):
    write_name = check + ".concat.geojson"
    if len(files) > 0:
        write_name = os.path.join(os.path.dirname(files[0]), write_name)
    featurecollection = None
    for f in files:
        fc = read_file(f)
        if fc is None:
            continue
        if featurecollection is None:
            featurecollection = fc
        else:
            featurecollection["features"] = (
                featurecollection["features"] + fc["features"]
            )
    with open(write_name, "w") as fp:
        geojson.dump(featurecollection, fp)


def main():
    parser = argparse.ArgumentParser(
        description="Create a line-by-line geojson file"
    )
    parser.add_argument("files", nargs="+")
    parser.add_argument("-c", "--checks", nargs="?")
    parser.add_argument("--line-by-line", action="store_true")
    args = parser.parse_args()
    for f in args.files:
        logging.info(f)
        if not os.path.isdir(f):
            continue
        files = get_geojson_files(f, args.checks)
        if args.line_by_line:
            line_by_line(files, args.checks)
        else:
            concat_files(files, args.checks)


if __name__ == "__main__":
    main()

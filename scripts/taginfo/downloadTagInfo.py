#!/usr/bin/env python3
"""
This downloads the TagInfo database (taginfo-db.db.bz2) located on
https://taginfo.openstreetmap.org/download, and cleans it up a bit for size
purposes.
"""
import os
import typing
import requests
import argparse
import bz2
import sqlite3
from tqdm import tqdm
import pandas


def download_file(name: str, directory: str):
    with requests.get(name, stream=True) as response:
        response.raise_for_status()
        size = int(response.headers.get("content-length", 0))
        block_size = 4096
        progress = tqdm(
            total=size,
            unit="iB",
            unit_scale=True,
            desc="Downloading TagInfo database",
        )
        with open(
            os.path.join(directory, os.path.basename(name)), "wb"
        ) as file_handle:
            for content in response.iter_content(chunk_size=block_size):
                file_handle.write(content)
                progress.update(len(content))
        progress.close()


def cleanup(
    database: str, min_count: int = 100, tags_to_remove: typing.List[str] = []
):
    """
    Remove unnecessary data
    """
    with sqlite3.connect(database) as conn:
        for table in [
            "key_characters",
            "key_combinations",
            "key_distributions",
            "prevalent_roles",
            "prevalent_values",
            "relation_roles",
            "relation_types",
            "similar_keys",
            "similar_keys_common_rare",
            "tag_combinations",
            "tag_distributions",
        ]:
            conn.execute("DROP TABLE IF EXISTS {}".format(table))
        for table in ["keys", "tags"]:
            conn.execute(
                "DELETE FROM {} WHERE count_all < {}".format(table, min_count)
            )
            # Remove "known good" keys that inflate the database, or keys that
            # are from imports and are largely unique. Also, some keys that can
            # be validated from the surrounding relations are dropped.
            for key in tags_to_remove:
                conn.execute(
                    "DELETE FROM {} WHERE key = '{}'".format(table, key)
                )
        conn.commit()
        conn.execute("VACUUM")


def convert_to_parquet(database: str, save_directory: str = "parquet"):
    """
    Convert an sqlite db to parquet files
    """
    with sqlite3.connect(database) as conn:
        path = os.path.join(save_directory, "taginfo")
        if not os.path.exists(path):
            os.makedirs(path)
        for table in conn.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        ).fetchall():
            table_name = table[0]
            data_frame = pandas.read_sql(f"SELECT * FROM {table_name}", conn)
            filename = os.path.join(path, table_name)
            if os.path.isfile(filename):
                os.remove(filename)
            data_frame.to_parquet(filename)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-d", "--directory", help="The directory to save the file to"
    )
    parser.add_argument("-u", "--url", help="The url for the TagInfo database")
    parser.add_argument(
        "--minimum_count",
        help="The minimum count to keep for tags",
        default=100,
    )
    parser.add_argument(
        "--parquet", action="store_true", help="Save as parquet db files"
    )
    parser.add_argument(
        "--tags-to-remove",
        nargs="*",
        help="The tags to remove",
        default=[
            "name",
            "addr:housenumber",
            "addr:city",
            "addr:place",
            "addr:suburb",
            "tiger:zip_left",
            "tiger:name_base",
            "tiger:zip_right",
            "tiger:county",
            "tiger:upload_uuid",
        ],
    )
    args = parser.parse_args()
    db = "taginfo-db.db"
    url = (
        args.url
        if args.url
        else "https://taginfo.openstreetmap.org/download/taginfo-db.db.bz2"
    )
    directory = args.directory if args.directory else "./"
    if not os.path.isfile(os.path.join(directory, db + ".bz2")):
        download_file(url, directory)
    if not os.path.isfile(os.path.join(directory, db)):
        block_size = 4096
        with open(
            os.path.join(directory, db), "wb"
        ) as taginfo_db, bz2.BZ2File(
            os.path.join(directory, db + ".bz2"), "rb"
        ) as compressed:
            for data in tqdm(
                iter(lambda: compressed.read(100 * block_size), b""),
                desc="Decompressing TagInfo database",
            ):
                taginfo_db.write(data)

    # Remove unnecessary data (for file size -- can be important with spark
    # transferring files around)
    cleanup(
        db, min_count=args.minimum_count, tags_to_remove=args.tags_to_remove
    )

    # For more indexes, see
    # https://github.com/taginfo/taginfo/blob/master/sources/db/add_extra_indexes.sql
    # The remaining indexes are for tag key counts (at this time).
    with sqlite3.connect(db) as conn:
        conn.execute(
            "CREATE INDEX IF NOT EXISTS {0}_idx ON {1} ({2})".format(
                "tags_key_value", "tags", "key, value"
            )
        )
        conn.execute("ANALYZE tags_key_value_idx")
        conn.execute("PRAGMA optimize")

    if args.parquet:
        convert_to_parquet(db)


if __name__ == "__main__":
    main()

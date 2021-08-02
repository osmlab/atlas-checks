#!/usr/bin/env python
"""
Scrape the OSM Wiki Data base for wiki data items to be used with a fallback
tag checker.
"""
import argparse
import json
import logging
import re
import requests
import sqlite3
import time
import pandas
import os

from cachecontrol import CacheControl
from cachecontrol.caches.file_cache import FileCache
from tqdm import tqdm

user_agent = (
    "atlas osm wiki db download/0.1 (https://github.com/osmlab/atlas-checks)"
)


CHANGED_ENTRIES = "changed.log"
"""
The logfile to write what has changed for WikiData items since the last run
"""


class WikiData:
    """
    A base class for wiki data
    """

    def __init__(self):
        # 503/200, see https://www.mediawiki.org/wiki/Manual:Maxlag_parameter
        self.maxlag = 1
        base_url = "https://wiki.openstreetmap.org"
        self.baseurl = f"{base_url}/w/api.php?maxlag={self.maxlag}&format=json"


class Properties(WikiData):
    """
    A class for wiki data property items
    """

    def __init__(self, item: int):
        super().__init__()
        self.id = item
        self.url = self.baseurl + "&action=wbgetentities&ids=P" + str(self.id)


class Item(WikiData):
    """
    A class for wiki data items
    """

    def __init__(self, item: int):
        super().__init__()
        self.id = item
        self.url = self.baseurl + "&action=wbgetentities&ids=Q" + str(self.id)


class DataBase:
    def __init__(self, database: str):
        self.db = database

    def add_table(self, table: str):
        """
        Add a table to a database
        """
        with sqlite3.connect(self.db) as conn:
            conn.execute(
                "CREATE TABLE IF NOT EXISTS {0} (id STRING PRIMARY KEY)".format(
                    table
                )
            )
            conn.commit()

    def add_column_to_table(self, table: str, column: str, column_def: str):
        """
        Add a column to a table
        """
        with sqlite3.connect(self.db) as conn:
            conn.execute(
                "ALTER TABLE {0} ADD {1} {2}".format(table, column, column_def)
            )
            conn.commit()

    def does_primary_key_exist(self, table, id):
        """
        Check if a primary key exists
        """
        with sqlite3.connect(self.db) as conn:
            result = conn.execute(
                "SELECT * FROM {0} WHERE id = ?".format(table), (id,)
            ).fetchone()
            return result is not None

    def does_column_exist(self, table, header):
        """
        Check if a column exists in a table, baseds off of the header
        """
        with sqlite3.connect(self.db) as conn:
            results = conn.execute(
                "PRAGMA table_info({0})".format(table)
            ).fetchall()
            # tuple(cid, name, type, notnull, default value, primary key)
            headers = [result[1] for result in results]
            return header in headers

    def add_row_to_table(self, table: str, row: dict):
        """
        Add a row to a table
        """
        with sqlite3.connect(self.db) as conn:
            match = re.compile("P[0-9]+")
            headers = []
            values = []
            for key in row:
                if match.match(key) or "id" == key:
                    headers.append(key)
                    values.append(row[key])
            # valuesQ MUST come before headers
            valuesQ = ", ".join(["?" for _ in headers])
            headers = ", ".join(headers)
            values = [self.convert(value) for value in values]
            rtuple = tuple(values)
            current = conn.execute(
                f"SELECT * FROM {table} WHERE id=?", (row["id"],)
            ).fetchone()
            if current:
                conn.execute(f"DELETE FROM {table} WHERE id=?", (row["id"],))
            try:
                conn.execute(
                    "INSERT INTO {0} ({1}) VALUES ({2})".format(
                        table, headers, valuesQ
                    ),
                    rtuple,
                )
            except sqlite3.IntegrityError as exception:
                logging.error(
                    f"INSERT INTO {table} ({headers}) VALUES ({rtuple})"
                )
                if "P19" in row:
                    print(row["P19"])
                    logging.error(
                        "Duplicates: "
                        + str(
                            conn.execute(
                                f"SELECT id FROM {table} WHERE P19=?",
                                (row["P19"],),
                            ).fetchall()
                        )
                    )
                if "P16" in row:
                    logging.error(
                        "Duplicates: "
                        + str(
                            conn.execute(
                                f"SELECT id FROM {table} WHERE P16=?",
                                (row["P16"],),
                            )
                        )
                    )
                raise exception
            if current:
                cursor = conn.execute(
                    f"SELECT * FROM {table} WHERE id=?", (row["id"],)
                )
                names = [desc[0] for desc in cursor.description]
                new = cursor.fetchone()
                logging.info(new)
                entries = len(new)
                entry = 0
                while entry < entries:
                    if new[entry] != current[entry]:
                        message = " ".join(
                            [
                                row["id"],
                                "changed",
                                str(names[entry]),
                                str(current[entry]),
                                "to",
                                str(new[entry]),
                                os.linesep,
                            ]
                        )
                        logging.error(message)
                        with open(CHANGED_ENTRIES, "a") as log:
                            log.write(message)
                    entry = entry + 1
            conn.commit()

    def create_index(self, table: str, column: str, unique: bool = False):
        """
        Create an index to speed queries up
        """
        statement = "CREATE "
        if unique:
            statement += "UNIQUE "
        statement += "INDEX IF NOT EXISTS {0}_{1}_idx ON {0} ({1})"
        with sqlite3.connect(self.db) as conn:
            conn.execute(statement.format(table, column))

    def convert_to_parquet(self, save_directory: str = "parquet"):
        """
        Convert an sqlite db to parquet files
        """
        with sqlite3.connect(self.db) as conn:
            path = os.path.join(save_directory, self.db.rstrip(".db"))
            if not os.path.exists(path):
                os.makedirs(path)
            for table in conn.execute(
                "SELECT name FROM sqlite_master WHERE type='table'"
            ).fetchall():
                table_name = table[0]
                data_frame = pandas.read_sql(
                    f"SELECT * FROM {table_name}", conn
                )
                filename = os.path.join(path, table_name)
                if os.path.isfile(filename):
                    os.remove(filename)
                data_frame.to_parquet(filename)

    @staticmethod
    def convert(obj):
        """
        Convert an object to a standard instance (e.g., number or string)
        """
        if isinstance(obj, dict) or isinstance(obj, list):
            return json.dumps(obj)
        if isinstance(obj, int) or isinstance(obj, float):
            return obj
        if isinstance(obj, str):
            return obj
        return str(obj)


def get_wiki_response(
    url: str, session: requests.sessions.Session = requests.Session()
) -> requests.Response:
    """
    Get a wiki response for a URL in a (relatively) safe manner (for bots)
    """
    try:
        response = session.get(url)
        while (
            response.status_code == 503 or response.status_code == 200
        ) and "Retry-After" in response.headers:
            timeout = response.headers["Retry-After"]
            logging.info(f"Sleeping for {timeout}")
            time.sleep(response.headers["Retry-After"])
            response = session.get(url)
        time.sleep(1)
        response.raise_for_status()
        return response
    except requests.ConnectionError as e:
        logging.exception(e)
        if e.response:
            logging.error(e.response.text())
        time.sleep(10)
        return get_wiki_response(url, session=session)


def populate_special():
    special = {}
    special["Q2"] = "tag"
    special["Q7"] = "key"
    special["Q9"] = "element"
    special["Q10"] = "OSM concept"
    special["Q11"] = "status"
    special["Q12"] = "group"
    special["Q13"] = "de facto"
    special["Q14"] = "in use"
    special["Q15"] = "approved"
    special["Q16"] = "rejected"
    special["Q17"] = "voting"
    special["Q18"] = "draft"
    special["Q19"] = "abandoned"
    special["Q20"] = "proposed"
    special["Q2761"] = "sandbox"
    special["Q5060"] = "obsolete"
    special["Q5061"] = "deprecated"
    special["Q7550"] = "discardable"
    special["Q21146"] = "imported"
    special["Q8001"] = "is prohibited"
    special["Q8000"] = "is allowed"

    return special


def parse_data_value(datavalue, special: dict = populate_special()):
    if "mainsnak" in datavalue:
        return parse_data_value(datavalue["mainsnak"], special=special)
    if "datavalue" in datavalue:
        return parse_data_value(datavalue["datavalue"])
    if "type" in datavalue:
        datatype = datavalue["type"]
        if datatype == "wikibase-entityid":
            tid = datavalue["value"]["id"]
            if tid in special:
                return special[tid]
            else:
                return tid
        if datatype == "string":
            return str(datavalue["value"])
        if datatype == "monolingualtext":
            return str(datavalue["value"]["text"])
    logging.error(f"Unsupported data value: {datavalue}")


def parse_claim(claim: list, special: dict = populate_special()):
    """
    Parse a claim from a WikiData dict
    >>> import json
    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P2","hash":"9c1b9f9b61faedefa272a9c8c980faba6cefe7d5","datavalue":{"value":{"entity-type":"item","numeric-id":7,"id":"Q7"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$D0688651-E4D8-41AF-B1C8-69C4FD0A9654","rank":"normal"}]')  # noqa: E501
    >>> parse_claim(json_data)
    'key'

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P16","hash":"9eb4b6015212b132f8ed28074659f06b58cad1d2","datavalue":{"value":"ref:EU:ENTSOE_EIC","type":"string"},"datatype":"string"},"type":"statement","id":"Q4108$31C4E97A-C439-4ECB-803C-AA73B02C9EC9","rank":"normal"}]')  # noqa: E501
    >>> parse_claim(json_data)
    'ref:EU:ENTSOE_EIC'

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P33","hash":"787ef76c487fb28eedcc10c388fb4245e2b92e0f","datavalue":{"value":{"entity-type":"item","numeric-id":8000,"id":"Q8000"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$8B848D75-5817-40EB-9A48-EF2A2D350C3C","rank":"preferred"}]')  # noqa: E501
    >>> parse_claim(json_data)
    'is allowed'

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P34","hash":"0020131857e3f842017ae00fb42c27556e40ba4d","datavalue":{"value":{"entity-type":"item","numeric-id":8000,"id":"Q8000"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$62AD3599-0926-42E0-8FB8-45E20105BCB4","rank":"preferred"}]')  # noqa: E501
    >>> parse_claim(json_data)
    'is allowed'

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P35","hash":"8d54b8f7391e3f783fe3d76d15d9036b90305842","datavalue":{"value":{"entity-type":"item","numeric-id":8000,"id":"Q8000"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$0F868DF0-E602-4545-AAF4-6A6A7749FC51","rank":"preferred"}]')  # noqa: E501
    >>> parse_claim(json_data)
    'is allowed'

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P36","hash":"724c9a8f85dfb6c9de78ecb25a7504e02b40a8c7","datavalue":{"value":{"entity-type":"item","numeric-id":8000,"id":"Q8000"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$364FDFEB-46D4-4FF2-8973-EFC08450CFBB","rank":"preferred"}]')  # noqa: E501
    >>> parse_claim(json_data)
    'is allowed'

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P25","hash":"2cb6aaaf2e30dcb19fc1ac1a5f671d26516cdc8f","datavalue":{"value":{"entity-type":"item","numeric-id":4680,"id":"Q4680"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$27A9F1C7-65E0-4495-B08F-E27530B145B7","rank":"preferred"}]')  # noqa: E501
    >>> parse_claim(json_data)
    'Q4680'

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P46","hash":"c93b534c6eef1b3953670dfe6e014675e880d62f","datavalue":{"value":{"entity-type":"item","numeric-id":4766,"id":"Q4766"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$470D4B95-6738-4F3E-896A-B8EBE1F56248","rank":"normal"},{"mainsnak":{"snaktype":"value","property":"P46","hash":"e03b53a3a83e1e611c7e6e0f86cf0b30bd645c37","datavalue":{"value":{"entity-type":"item","numeric-id":4954,"id":"Q4954"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$177046FD-4E6F-4074-B7C4-0E05F95B7CC8","rank":"normal"},{"mainsnak":{"snaktype":"value","property":"P46","hash":"db5bdfb387965b655d9019a16cbe4a49b93e9df9","datavalue":{"value":{"entity-type":"item","numeric-id":4990,"id":"Q4990"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$B0C90B34-1741-4933-9F0E-FBB9D5CC9AE4","rank":"normal"},{"mainsnak":{"snaktype":"value","property":"P46","hash":"479e97d7efe88373b437fa5097a7bc2af2def907","datavalue":{"value":{"entity-type":"item","numeric-id":5254,"id":"Q5254"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$18F12696-579F-4688-9939-31837D3ADAA0","rank":"normal"},{"mainsnak":{"snaktype":"value","property":"P46","hash":"1f834ec797f2c558120484498b794382fba0ae0c","datavalue":{"value":{"entity-type":"item","numeric-id":5792,"id":"Q5792"},"type":"wikibase-entityid"},"datatype":"wikibase-item"},"type":"statement","id":"Q4108$17789653-0C21-4B38-BCF7-1080F22D2429","rank":"normal"}]')  # noqa: E501
    >>> parse_claim(json_data)
    ['Q4766', 'Q4954', 'Q4990', 'Q5254', 'Q5792']

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P31","hash":"7bb636ede2668f353c188e243443eb44b582bf46","datavalue":{"value":{"text":"Key:ref:EU:ENTSOE EIC","language":"en"},"type":"monolingualtext"},"datatype":"monolingualtext"},"type":"statement","id":"Q4108$96D9A775-073F-49A4-83F1-82AB3E3217B7","rank":"normal"}]')  # noqa: E501
    >>> parse_claim(json_data)
    'Key:ref:EU:ENTSOE EIC'

    >>> json_data = json.loads('[{"mainsnak":{"snaktype":"value","property":"P13","hash":"1352cfc4840bb96337bfc822ceb0255686e3e9e7","datavalue":{"value":"[1-6][0-9][ATVWXYZ][0-9A-Z-]{12}[0-9A-Z](;[1-6][0-9][ATVWXYZ][0-9A-Z-]{12}[0-9A-Z])*","type":"string"},"datatype":"external-id"},"type":"statement","id":"Q4108$a03623d5-4420-9b6c-9787-69a8bbb2c605","rank":"normal"}]')  # noqa: E501
    >>> parse_claim(json_data)
    '[1-6][0-9][ATVWXYZ][0-9A-Z-]{12}[0-9A-Z](;[1-6][0-9][ATVWXYZ][0-9A-Z-]{12}[0-9A-Z])*'

    """
    rrlist = []
    for item in claim:
        if (
            "mainsnak" in item
            and "snaktype" in item["mainsnak"]
            and item["mainsnak"]["snaktype"] == "value"
            and "datavalue" in item["mainsnak"]
        ):
            data = item["mainsnak"]["datavalue"]
            item = parse_data_value(data)
            if "qualifiers" in item:
                item_information = {}
                for qualifier in item["qualifiers"]:
                    qlist = []
                    item_information[qualifier] = qlist
                    for key in item["qualifiers"][qualifier]:
                        if "datavalue" in key:
                            data_value = parse_data_value(key)
                            if data_value:
                                qlist.append(data_value)
                item = {item: item_information}

            if item is not None:
                rrlist.append(item)

    if len(rrlist) == 0:
        return None
    return rrlist if len(rrlist) > 1 else rrlist[0]


def main():
    """
    Main loop (iterates through Properties and Items until a set number have
    failed, then stops)
    """
    special = populate_special()
    filecache = FileCache(".web_cache", forever=True)
    session = CacheControl(requests.Session(), filecache)
    session.headers.update({"User-Agent": user_agent})
    db = DataBase("wikidata.db")
    table = "wiki_data"
    db.add_table(table)
    skipped = 0
    index = 1
    if os.path.isfile(CHANGED_ENTRIES):
        os.remove(CHANGED_ENTRIES)
    progress = tqdm(desc="Downloading wiki property entries")
    while skipped < 5:
        progress.update(1)
        prop = Properties(index)
        header = "P" + str(prop.id)
        if header not in special and not db.does_column_exist(table, header):
            response = get_wiki_response(prop.url, session=session)
            try:
                response.raise_for_status()
                json = response.json()
                if (
                    "entities" in json
                    and header in json["entities"]
                    and "missing" not in json["entities"][header]
                ):
                    skipped = 0
                    db.add_column_to_table(table, header, "BLOB")
                else:
                    logging.info(f"{header} not in wiki data")
                    skipped = skipped + 1
                    logging.debug(f"{skipped}")
            except sqlite3.OperationalError as e:
                if (
                    len(e.args) == 0
                    or "duplicate column name" not in e.args[0]
                ):
                    raise e
        index = index + 1
    progress.close()

    # P16 is the permanent key id
    db.create_index("wiki_data", "P16", unique=True)
    # P19 is the permanent tag id
    db.create_index("wiki_data", "P19", unique=True)
    index = 1
    skipped = 0
    entries_to_update = []
    with tqdm(desc="Adding wiki data entries") as progress:
        while skipped < 50:
            progress.update(1)
            data = Item(index)
            header = "Q" + str(data.id)
            if header not in special and not db.does_primary_key_exist(
                table, header
            ):
                if update(data, session, db, table, special):
                    skipped = 0
                else:
                    tqdm.write(f"Failed to get {data.url}")
                    skipped = skipped + 1
            elif db.does_primary_key_exist(table, header):
                entries_to_update.append(data)
                skipped = 0
            index = index + 1
    # Try to stay under 2000 characters
    # https://wiki.openstreetmap.org/w/api.php?action=wbgetentities&ids= is ~65 characters
    # 10_000 == 5 additional characters, so we could do 387 at a time. Rounding down to 300.
    with tqdm(
        total=len(entries_to_update), desc="Updating wiki data entries"
    ) as progress:
        for items in chunk(entries_to_update, 300):
            item = Item([t_item.id for t_item in items])
            update(item, session, db, table, special)
            progress.update(len(items))


def chunk(items, max_length):
    """Yield max_length successive items"""
    for index in range(0, len(items), max_length):
        yield items[index : index + max_length]


def update(data: Item, session, db, table, special):
    header = "Q" + str(data.id)
    logging.info(f"Getting {header}")
    response = get_wiki_response(data.url, session=session)
    json = response.json()
    if (
        "entities" in json
        and header in json["entities"]
        and "missing" not in json["entities"][header]
        # Skip anything that redirects.
        # The redirect will be indexed eventually.
        and "redirects" not in json["entities"][header]
    ):
        for entry in json["entities"]:
            rdata = json["entities"][entry]
            put_data = {"id": header}
            if "claims" in rdata:
                for claim in rdata["claims"]:
                    put_data[claim] = parse_claim(
                        rdata["claims"][claim], special=special
                    )
            db.add_row_to_table(table, put_data)
        return True
    logging.debug("Skipped {header}")
    return False


def args():
    """
    Parse arguments
    """
    args = argparse.ArgumentParser()
    args.add_argument("--test", action="store_true", help="Run tests")
    return args.parse_args()


if __name__ == "__main__":
    args = args()
    if args.test:
        import doctest

        doctest.testmod()
    else:
        main()

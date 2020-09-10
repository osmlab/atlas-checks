#!/usr/bin/env python3
"""
Get SRTM (HGT) files from the Nasa SRTM mission via USGS in a server-friendly
way.

There is a pause of 10 seconds between downloads, and if a file is already
downloaded, it is not redownloaded. This also looks at timestamps, although
it is unlikely that the SRTM files will be significantly updated.
"""
from defusedxml import ElementTree as ET
from tqdm import tqdm
import argparse
import dateutil.parser
import os
import requests
import time
import typing

url = "https://dds.cr.usgs.gov/srtm/version2_1/SRTM3/"
def download_file(url: str, output_dir: str="elevation"):
    name = url.split("/")[-1]
    with requests.get(url, stream=True) as response:
        response.raise_for_status()
        with open(os.path.join(output_dir, name), "wb") as fh:
            for content in tqdm(response.iter_content(chunk_size=4096)):
                fh.write(content)

def file_needs_download(url: str, output_dir: str="elevation") -> bool:
    name = url.split("/")[-1]
    filepath = os.path.join(output_dir, name)
    if os.path.isfile(filepath):
        head = requests.head(url)
        head.raise_for_status()
        last_modified = dateutil.parser.parse(head.headers.get('Last-Modified')).timestamp()
        content_length = int(head.headers.get('Content-Length'))

        creation = os.path.getctime(filepath)
        modification = os.path.getmtime(filepath)

        return modification < last_modified or creation < last_modified or content_length != os.path.getsize(filepath)
    return True

def get_sub_url(url: str) -> typing.List[str]:
    response = requests.get(url)
    sub_url = []
    for line in response.iter_lines():
        try:
            tree = ET.fromstring(line)
            for child in tree.findall("a"):
                sub_url.append(child.attrib.get("href"))
        except ET.ParseError as e:
            pass
    return sub_url

def download_url(url: str, output_dir: str="elevation") -> None:
    for sub_url in tqdm(get_sub_url(url)):
        for sub_sub_url in tqdm(get_sub_url(url + sub_url)):
            if file_needs_download(url + sub_url + sub_sub_url, output_dir = output_dir):
                tqdm.write("Downloading " + url + sub_url + sub_sub_url)
                download_file(url + sub_url + sub_sub_url, output_dir = output_dir)
                time.sleep(10)
            else:
                tqdm.write(url + sub_url + sub_sub_url + " not downloaded")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Download SRTM files")
    parser.add_argument("directory", nargs='?', default="elevation", help="The directory to save files to")
    args = parser.parse_args()
    download_url(url, output_dir = args.directory)

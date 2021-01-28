#!/usr/bin/env python

import codecs
import os
from setuptools import setup, find_packages


with open("README.md", "r") as readme:
    long_description = readme.read()


def read(rel_path):
    here = os.path.abspath(os.path.dirname(__file__))
    with codecs.open(os.path.join(here, rel_path), 'r') as fp:
        return fp.read()


def get_version(rel_path):
    for line in read(rel_path).splitlines():
        if line.startswith('__version__'):
            delim = '"' if '"' in line else "'"
            return line.split(delim)[1]
    else:
        raise RuntimeError("Unable to find version string.")


name = "pyatlas_checks"
version = get_version(f"{name}/__init__.py")
exclude_dirs = ["tests", "venv"]
packs = find_packages(exclude=exclude_dirs)

setup(name=name,
      version=version,
      description="A Python CLI for Atlas-Checks tools",
      long_description_content_type="text/markdown",
      long_description=long_description,
      url="https://github.com/osmlab/atlas-checks/pyatlas_checks",
      packages=packs,
      include_package_data=True,
      python_requires=">=3.6",
      setup_requires=["pytest-runner"],
      tests_require=["pytest"],
      install_requires=["Click", "pyspark"],
      entry_points='''
            [console_scripts]
            pyatlas-checks=pyatlas_checks.cli:cli
      '''
      )

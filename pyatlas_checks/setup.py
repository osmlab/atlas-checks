#!/usr/bin/env python

import os
from setuptools import setup, find_packages

here = os.path.abspath(os.path.dirname(__file__))
README = open(os.path.join(here, 'README.md')).read()
exclude_dirs = ['tests', 'venv']

packs = find_packages(exclude=exclude_dirs)

setup(name='pyatlas_checks',
      version="1.0",
      packages=find_packages(),
      include_package_data=True,
      install_requires=["Click"],
      entry_points='''
            [console_scripts]
            pyatlas-checks=pyatlas_checks.cli:cli
      '''
      )

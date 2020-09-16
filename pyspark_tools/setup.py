#!/usr/bin/env python

import os
from setuptools import setup, find_packages

here = os.path.abspath(os.path.dirname(__file__))
README = open(os.path.join(here, 'README.md')).read()
exclude_dirs = ['tests', 'venv']

packs = find_packages(exclude=exclude_dirs)

setup(name='pyspark_tools',
      version="1.0",
      py_modules=['cli'],
      install_requires=["Click"],
      entry_points='''
            [console_scripts]
            pyspark=cli:cli
      '''
      )

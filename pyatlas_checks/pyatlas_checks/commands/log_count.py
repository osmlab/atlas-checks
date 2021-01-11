from pyatlas_checks.commands.base_command import BaseCommand
from pyspark.sql import SparkSession


class LogCount(BaseCommand):
    def __init__(self, input_log):
        super().__init__()
        self.input_log = input_log

    def run(self):
        spark = SparkSession.builder.appName("LogCount").getOrCreate()
        return spark.read.json(self.input_log).count()

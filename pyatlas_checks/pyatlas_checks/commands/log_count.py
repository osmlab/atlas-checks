from pyspark.sql import SparkSession


def run(input_log):
    spark = SparkSession.builder.appName("LogCount").getOrCreate()
    return spark.read.json(input_log).count()

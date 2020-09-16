from pyspark.sql import SparkSession, udf, types
import json


class LogDiff:
    def __init__(self, baseline, current, output):
        self.baseline = baseline
        self.current = current
        self.output = output
        self.get_check_name = udf.UserDefinedFunction(
            lambda json_text: json.loads(json_text)["properties"]["generator"], types.StringType())
        self.get_identifiers = udf.UserDefinedFunction(
            lambda json_text: json.loads(json_text)["properties"]["identifiers"], types.ArrayType(types.StringType()))
        self.set_diff_type_addition = udf.UserDefinedFunction(lambda: "addition", types.StringType())
        self.set_diff_type_subtraction = udf.UserDefinedFunction(lambda: "subtraction", types.StringType())

    def read_run(self, spark, path):
        return spark.read.text(path)\
            .withColumn("check", self.get_check_name("value"))\
            .withColumn("identifiers", self.get_identifiers("value"))

    def run(self):
        spark = SparkSession.builder.appName("LogCount").getOrCreate()

        baseline = self.read_run(spark, self.baseline)
        current = self.read_run(spark, self.current)

        join_condition = [current.check == baseline.check, current.identifiers == baseline.identifiers]

        current.join(baseline, join_condition, "left_outer")\
            .filter(baseline.value.isNull())\
            .select(current.check, current.identifiers, current.value)\
            .withColumn("diff_type", self.set_diff_type_addition())\
            .union(
                baseline.join(current, join_condition, "left_outer")
                .filter(current.value.isNull())
                .select(baseline.check, baseline.identifiers, baseline.value)
                .withColumn("diff_type", self.set_diff_type_subtraction())
            )\
            .select("check", "diff_type", "value").write.partitionBy("check", "diff_type").text(self.output)

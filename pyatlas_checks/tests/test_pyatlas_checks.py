import unittest
from click.testing import CliRunner
from pyatlas_checks.cli import cli


class TestLogCounter(unittest.TestCase):
    runner = CliRunner()

    def test_log_counter(self):
        runner = self.runner
        resource_path = 'tests/data/test.log'
        log_count = "Total log count: 2527"
        result = runner.invoke(cli=cli, args=['log-counter', resource_path])
        self.assertEqual(0, result.exit_code)
        self.assertIn(log_count, result.output)

    def test_log_counter_no_path_provided(self):
        runner = self.runner
        result = runner.invoke(cli=cli, args=['log-counter'])
        self.assertEqual(2, result.exit_code)

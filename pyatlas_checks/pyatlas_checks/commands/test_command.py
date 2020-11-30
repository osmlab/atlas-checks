from pyatlas_checks.commands.base_command import BaseCommand


class TestCommand(BaseCommand):
    def __init__(self, test_string):
        super().__init__()
        self.test_string = test_string

    def run(self):
        self.logger.warn(self.test_string)

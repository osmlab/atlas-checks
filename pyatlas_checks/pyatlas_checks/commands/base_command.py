import logging


class BaseCommand:
    def __init__(self):
        self.logger = logging.getLogger()

    def run(self):
        pass

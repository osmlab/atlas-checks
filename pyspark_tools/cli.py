import click
from atlas_checks_pyspark_tools.commands import maproulette_upload_command
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)


class Config(object):

    def __init__(self):
        self.verbose = False


pass_config = click.make_pass_decorator(Config, ensure=True)


@click.group()
@click.option('--verbose', is_flag=True)
@pass_config
def cli(config, verbose):
    config.verbose = verbose


@cli.command()
@click.option('--input_folder', required=True, type=str,
              help='The input directory that contains the log files to be uploaded to MapRoulette')
@click.option('--hostname', required=True, type=str,
              help='The hostname of the MapRoulette instance being addressed')
@click.option('--api_key', envvar="API_KEY", type=str,
              help='The user-specific API key for the MapRoulette instance being addressed')
@click.option('--cert_fp', required=False, default=None, type=str,
              help='The path to the client side certificate file')
@click.option('--key_fp', default=None, type=str,
              help='The path to the client key file')
@click.option('--verify', default=False, is_flag=True,
              help='Flag indicating whether or not to verify SSL certificates for HTTPS requests')
@click.option('--checks_config', required=True, type=str,
              help='The file containing the configurable Atlas-Checks parameters')
@click.option('--project_name', required=True, type=str,
              help='The name of the project to post the new challenges and tasks to')
@click.option('--countries', default=None, type=str,
              help='A comma-separated list of ISO3 country codes to filter flags by')
@click.option('--checks', default=None, type=str,
              help='A comma-separated list of check names to filter flags by')
@pass_config
def mr_upload(config, input_folder, hostname, api_key, cert_fp, key_fp, verify, checks_config, project_name, countries,
              checks):
    """Post flags from one or more log files to MapRoulette
    """
    maproulette_upload_command.run(
        input_folder=input_folder,
        hostname=hostname,
        api_key=api_key,
        cert_fp=cert_fp,
        key_fp=key_fp,
        verify=verify,
        checks_config=checks_config,
        project_name=project_name,
        countries=countries,
        checks=checks
    )

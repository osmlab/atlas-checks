import click
from pyatlas_checks.commands import log_count
from pyatlas_checks.commands import log_diff
from pyatlas_checks.commands import maproulette_upload_command


@click.group()
@click.option('--verbose', is_flag=True)
@click.pass_context
def cli(ctx, verbose):
    ctx.obj = {
        'verbose': verbose
    }


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
@click.pass_context
def mr_upload(ctx, input_folder, hostname, api_key, cert_fp, key_fp, verify, checks_config, project_name, countries,
              checks):
    """Post flags from one or more log files to MapRoulette
    """
    verbose = ctx.obj['verbose']
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


@cli.command()
@click.argument('baseline')
@click.argument('current')
@click.argument('output')
@click.pass_context
def log_difference(ctx, baseline, current, output):
    """Find the difference between two log files
    """
    verbose = ctx.obj['verbose']
    runner = log_diff.LogDiff(
        baseline=baseline,
        current=current,
        output=output
    )
    runner.run()


@cli.command()
@click.argument('input_log')
@click.pass_context
def log_counter(ctx, input_log):
    verbose = ctx.obj['verbose']
    click.echo(f"Total log count: {log_count.run(input_log)}")

import click
from pyatlas_checks.commands.log_count import LogCount


@click.group()
@click.option('--verbose', is_flag=True)
@click.pass_context
def cli(ctx, verbose):
    ctx.obj = {
        'verbose': verbose
    }


@cli.command()
@click.argument('input_log')
@click.pass_context
def log_counter(ctx, input_log):
    verbose = ctx.obj['verbose']
    if verbose:
        click.echo(f"Verbose mode enabled.")
    command = LogCount(input_log)
    click.echo(f"Total log count: {command.run()}")


if __name__ == '__main__':
    cli()

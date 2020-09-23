import click
from pyatlas_checks.commands.test_command import TestCommand


@click.group()
@click.option('--verbose', is_flag=True)
@click.pass_context
def cli(ctx, verbose):
    ctx.obj = {
        'verbose': verbose
    }


@cli.command()
@click.argument('test_string')
@click.pass_context
def test_command(ctx, test_string):
    verbose = ctx.obj['verbose']
    command = TestCommand(test_string)
    command.run()

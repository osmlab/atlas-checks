package org.openstreetmap.atlas.checks.commands;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.openstreetmap.atlas.utilities.runtime.FlexibleCommand;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Shell for running atlas-checks commands. Run this command with no arguments to learn more about it.
 *
 * @author bbreithaupt
 */
public class AtlasChecksCommand extends FlexibleCommand
{
    public static void main(final String... args)
    {
        final AtlasChecksCommand command = new AtlasChecksCommand(args);
        try
        {
            command.runWithoutQuitting(args);
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
            command.printUsageAndExit(1);
        }
    }

    public AtlasChecksCommand(final String... args)
    {
        super(args);
    }

    @Override
    protected Stream<Class<? extends FlexibleSubCommand>> getSupportedCommands()
    {
        final List<Class<? extends FlexibleSubCommand>> returnValue = new ArrayList<>();
        new FastClasspathScanner(
                AtlasChecksCommand.class.getPackage().getName())
                .matchClassesImplementing(FlexibleSubCommand.class, returnValue::add).scan();
        return returnValue.stream();
    }
}


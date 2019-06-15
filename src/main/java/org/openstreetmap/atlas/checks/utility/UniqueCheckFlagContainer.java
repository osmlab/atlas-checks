package org.openstreetmap.atlas.checks.utility;

import org.openstreetmap.atlas.checks.flag.CheckFlag;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class UniqueCheckFlagContainer implements Serializable
{

    public static UniqueCheckFlagContainer combine(final UniqueCheckFlagContainer container1, final UniqueCheckFlagContainer container2)
    {
        container2.uniqueFlags.entrySet().forEach(entry ->
        {
            container1.addAll(entry.getKey(), entry.getValue().values());
        });
        return container1;
    }


    private final ConcurrentHashMap<String, ConcurrentHashMap<String, CheckFlag>> uniqueFlags;

    public UniqueCheckFlagContainer()
    {
        this.uniqueFlags = new ConcurrentHashMap<>();
    }

    private UniqueCheckFlagContainer(final ConcurrentHashMap<String, ConcurrentHashMap<String, CheckFlag>> flags)
    {
        this.uniqueFlags = flags;
    }

    public void add(String flagSource, final CheckFlag flag)
    {
        this.uniqueFlags.putIfAbsent(flagSource, new ConcurrentHashMap<>());
        this.uniqueFlags.get(flagSource).putIfAbsent(flag.getIdentifier(), flag);
    }

    public void addAll(String flagSource, final Iterable<CheckFlag> flags)
    {
        flags.forEach(flag -> this.add(flagSource, flag));

    }

    private Stream<CheckFlag> stream()
    {
        return this.uniqueFlags.values().stream().map(ConcurrentHashMap::values).flatMap(
                Collection::stream);
    }
}

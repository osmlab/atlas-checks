package org.openstreetmap.atlas.checks.utility;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;

/**
 * A container that will deduplicate check flags based on source and unique IDs
 *
 * @author jklamer
 * @author bbreithaupt
 */
public class UniqueCheckFlagContainer implements Serializable
{

    private final ConcurrentHashMap<String, ConcurrentHashMap<Set<String>, CheckFlag>> uniqueFlags;

    /**
     * Combines to containers. This deduplicates {@link CheckFlag}s by overwiting ones with matching
     * sources and IDs.
     *
     * @param container1
     *            {@link UniqueCheckFlagContainer}
     * @param container2
     *            {@link UniqueCheckFlagContainer}
     * @return merged {@link UniqueCheckFlagContainer}
     */
    public static UniqueCheckFlagContainer combine(final UniqueCheckFlagContainer container1,
            final UniqueCheckFlagContainer container2)
    {
        container2.uniqueFlags.entrySet()
                .forEach(entry -> container1.addAll(entry.getKey(), entry.getValue().values()));
        return container1;
    }

    public UniqueCheckFlagContainer()
    {
        this.uniqueFlags = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("s1144")
    // Ignore unused constructor warning, this is used for deserialization
    private UniqueCheckFlagContainer(
            final ConcurrentHashMap<String, ConcurrentHashMap<Set<String>, CheckFlag>> flags)
    {
        this.uniqueFlags = flags;
    }

    /**
     * Add a {@link CheckFlag} to the container based on its source.
     *
     * @param flagSource
     *            {@link String} source (check that generated the flag)
     * @param flag
     *            {@link CheckFlag}
     */
    public void add(final String flagSource, final CheckFlag flag)
    {
        this.uniqueFlags.putIfAbsent(flagSource, new ConcurrentHashMap<>());
        final Set<String> uniqueObjectIdentifiers = flag.getUniqueIdentifiers();
        this.uniqueFlags.get(flagSource)
                .putIfAbsent(uniqueObjectIdentifiers.isEmpty()
                        ? Collections.singleton(flag.getIdentifier())
                        : uniqueObjectIdentifiers, flag);
    }

    /**
     * Batch add {@link CheckFlag} from a single source.
     *
     * @param flagSource
     *            {@link String} source (check that generated the flags)
     * @param flags
     *            {@link Iterable} of {@link CheckFlag}s
     */
    public void addAll(final String flagSource, final Iterable<CheckFlag> flags)
    {
        flags.forEach(flag -> this.add(flagSource, flag));

    }

    /**
     * Convert the {@link CheckFlag}s into a {@link Stream} of {@link CheckFlagEvent}s.
     *
     * @return a {@link Stream} of {@link CheckFlagEvent}s
     */
    public Stream<CheckFlagEvent> reconstructEvents()
    {
        return this.uniqueFlags.keySet().stream()
                .flatMap(checkName -> this.uniqueFlags.get(checkName).values().stream()
                        .map(checkFlag -> new CheckFlagEvent(checkName, checkFlag)));
    }

    /**
     * Get the contents of the container as a stream.
     *
     * @return a {@link Stream} of {@link CheckFlag}s
     */
    public Stream<CheckFlag> stream()
    {
        return this.uniqueFlags.values().stream().map(ConcurrentHashMap::values)
                .flatMap(Collection::stream);
    }
}

package org.openstreetmap.atlas.checks.utility;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * This is a basic edge walker that is implemented using a BFS. It gathers a {@link Set} of
 * {@link Edge}s based on a collector and decider.
 *
 * @author bbreithaupt
 */
public class BfsEdgeWalker
{
    private final BiFunction<Edge, Set<Edge>, Set<Edge>> collector;
    private final Predicate<Edge> decider;

    /**
     * A class constructor that sets both the collector and decider.
     *
     * @param collector
     *            a {@link BiFunction} that takes an {@link Edge} and a {@link Set} of previously
     *            queued {@link Edge}s, and returns a {@link Set} of {@link Edge}s to be enqueued
     * @param decider
     *            a {@link Predicate} that takes an {@link Edge} and return true if the edge should
     *            be added to the collection of {@link Edge}s that will be returned byu the walker
     */
    public BfsEdgeWalker(final BiFunction<Edge, Set<Edge>, Set<Edge>> collector,
            final Predicate<Edge> decider)
    {
        this.collector = collector;
        this.decider = decider;
    }

    /**
     * A class constructor that sets the collector and uses a default decider that returns true for
     * all
     *
     * @param collector
     *            a {@link BiFunction} that takes an {@link Edge} and a {@link Set} of previously
     *            queued {@link Edge}s, and returns a {@link Set} of {@link Edge}s to be enqueued
     */
    public BfsEdgeWalker(final BiFunction<Edge, Set<Edge>, Set<Edge>> collector)
    {
        this(collector, edge -> true);
    }

    /**
     * Given a starting edge, collects a {@link Set} of {@link Edge}s based on the collector and
     * decider.
     *
     * @param startEdge
     *            the {@link Edge} to start walking from
     * @return a {@link Set} of collected {@link Edge}s
     */
    public Set<Edge> collect(final Edge startEdge)
    {
        final Set<Edge> queued = new HashSet<>();
        final Set<Edge> collection = new HashSet<>();
        final ArrayDeque<Edge> queue = new ArrayDeque<>();

        queue.add(startEdge);
        queued.add(startEdge);

        while (!queue.isEmpty())
        {
            final Edge currentEdge = queue.poll();

            if (this.decider.test(currentEdge))
            {
                collection.add(currentEdge);
                final Set<Edge> toQueue = this.collector.apply(currentEdge, queued);
                queue.addAll(toQueue);
                queued.addAll(toQueue);
            }
        }

        return collection;
    }
}

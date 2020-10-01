package org.openstreetmap.atlas.checks.base;

import java.io.Serializable;
import java.util.Optional;

import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;

/**
 * The check interface has one method that will execute the check against an atlas entity and return
 * an Optional {@link CheckFlag}. If the optional is empty then the check didn't find anything to
 * flag.
 *
 * @author cuthbertm
 */
public interface Check extends Serializable
{
    /**
     * The check for the atlas object
     *
     * @param object
     *            The {@link AtlasObject} to check
     * @return an {@link Optional} {@link CheckFlag}, {@link Optional#EMPTY} if check did not reveal
     *         any errors
     */
    Optional<CheckFlag> check(AtlasObject object);

    /**
     * Cleanup check to remove any remaining artifacts from execution
     */
    void clear();

    /**
     * The finder returned will be used to collect the {@link ComplexEntity}s this check will be
     * applied to.
     *
     * @param <T>
     *            The type of {@link ComplexEntity} to be returned
     * @return An {@link Optional} {@link Finder}
     */
    default <T extends ComplexEntity> Optional<Finder<T>> finder()
    {
        return Optional.empty();
    }

    /**
     * Compute and return all the {@link CheckFlag}s from this check, given {@link AtlasEntity}s and
     * {@link Relation}s. {@link ComplexEntity}s can be added as well, using the appropriate
     * {@link Finder}.
     *
     * @param atlas
     *            the {@link Atlas} to check
     * @return all the {@link CheckFlag}s from this check
     */
    Iterable<CheckFlag> flags(Atlas atlas);

    /**
     * Gets a challenge object for the specific check
     *
     * @return a {@link Challenge}
     */
    Challenge getChallenge();

    /**
     * Gets the name of this check
     *
     * @return a {@code String} name
     */
    String getCheckName();

    /**
     * Helper for debugging. Implement in check to log info after check is run.
     */
    void logStatus();

    /**
     * Checks to see whether the check is valid for the given country.
     *
     * @param country
     *            country to check
     * @return {@code true} if the check is applicable to the given country
     */
    boolean validCheckForCountry(String country);

    /**
     * Checks to see whether the supplied object class type is valid for this particular check
     *
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return true if it is
     */
    boolean validCheckForObject(AtlasObject object);

}

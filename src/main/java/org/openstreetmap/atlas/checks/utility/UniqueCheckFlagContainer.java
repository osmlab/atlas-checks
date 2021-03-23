package org.openstreetmap.atlas.checks.utility;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;

/**
 * A container used to deduplicate check flags based on checkName and unique IDs
 *
 * @author jklamer
 * @author bbreithaupt
 */
public class UniqueCheckFlagContainer implements Serializable
{

    private String checkName;
    private Set<String> uniqueIdentifiers;
    private CheckFlag checkFlag;

    /**
     * @param checkFlagEvent
     *            {@link CheckFlagEvent}
     */
    public UniqueCheckFlagContainer(final CheckFlagEvent checkFlagEvent)
    {
        this(checkFlagEvent.getCheckName(), checkFlagEvent.getCheckFlag().getUniqueIdentifiers(),
                checkFlagEvent.getCheckFlag().makeComplete());
    }

    /**
     * @param checkName
     *            {@link String} check name
     * @param uniqueIdentifiers
     *            {@link Set} of {@link String}s from {@link CheckFlag#getUniqueIdentifiers()}
     * @param checkFlag
     *            {@link CheckFlag}
     */
    public UniqueCheckFlagContainer(final String checkName, final Set<String> uniqueIdentifiers,
            final CheckFlag checkFlag)
    {
        this.checkName = checkName;
        this.uniqueIdentifiers = uniqueIdentifiers;
        this.checkFlag = checkFlag.makeComplete();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || getClass() != other.getClass())
        {
            return false;
        }
        final UniqueCheckFlagContainer that = (UniqueCheckFlagContainer) other;
        return Objects.equals(this.checkName, that.checkName)
                && Objects.equals(this.uniqueIdentifiers, that.uniqueIdentifiers);
    }

    public CheckFlag getCheckFlag()
    {
        return this.checkFlag;
    }

    public String getCheckName()
    {
        return this.checkName;
    }

    public CheckFlagEvent getEvent()
    {
        return new CheckFlagEvent(this.checkName, this.checkFlag);
    }

    public Set<String> getUniqueIdentifiers()
    {
        return this.uniqueIdentifiers;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.checkName, this.uniqueIdentifiers);
    }
}

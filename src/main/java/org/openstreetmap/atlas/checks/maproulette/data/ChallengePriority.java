package org.openstreetmap.atlas.checks.maproulette.data;

/**
 * @author cuthbertm
 */
public enum ChallengePriority
{
    HIGH(0),
    MEDIUM(1),
    LOW(2);

    private final int value;

    public static ChallengePriority fromValue(final int value)
    {
        for (final ChallengePriority challengePriority : ChallengePriority.values())
        {
            if (challengePriority.intValue() == value)
            {
                return challengePriority;
            }
        }
        return ChallengePriority.MEDIUM;
    }

    ChallengePriority(final int value)
    {
        this.value = value;
    }

    public int intValue()
    {
        return this.value;
    }
}

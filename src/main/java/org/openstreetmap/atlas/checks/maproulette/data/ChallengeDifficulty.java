package org.openstreetmap.atlas.checks.maproulette.data;

/**
 * @author cuthbertm
 */
public enum ChallengeDifficulty
{
    EASY(1),
    NORMAL(2),
    EXPERT(3);

    private final int value;

    public static ChallengeDifficulty fromValue(final int value)
    {
        for (final ChallengeDifficulty challengeDifficulty : ChallengeDifficulty.values())
        {
            if (challengeDifficulty.intValue() == value)
            {
                return challengeDifficulty;
            }
        }
        return null;
    }

    ChallengeDifficulty(final int value)
    {
        this.value = value;
    }

    public int intValue()
    {
        return this.value;
    }
}

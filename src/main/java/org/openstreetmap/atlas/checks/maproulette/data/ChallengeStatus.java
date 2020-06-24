package org.openstreetmap.atlas.checks.maproulette.data;

/**
 * @author danielbaah
 */
public enum ChallengeStatus
{
    NA(0),
    BUILDING(1),
    FAILED(2),
    READY(3),
    PARTIALLY_LOADED(4),
    FINISHED(5),
    DELETING_TASKS(6);

    private final int value;

    public static ChallengeStatus fromValue(final int value)
    {
        for (final ChallengeStatus challengeStatus : ChallengeStatus.values())
        {
            if (challengeStatus.intValue() == value)
            {
                return challengeStatus;
            }
        }
        return ChallengeStatus.NA;
    }

    ChallengeStatus(final int value)
    {
        this.value = value;
    }

    public int intValue()
    {
        return this.value;
    }
}

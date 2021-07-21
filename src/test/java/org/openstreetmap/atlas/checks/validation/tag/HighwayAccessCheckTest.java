package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link HighwayAccessCheck}
 *
 * @author v-naydinyan
 */
public class HighwayAccessCheckTest {
    @Rule
    public HighwayAccessCheckTestRule setup = new HighwayAccessCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final HighwayAccessCheck check = new HighwayAccessCheck(ConfigurationResolver.emptyConfiguration());

    @Test
    public void falsePositiveAccessTagIsPermissiveHighwayTagIsWrong()
    {
        this.verifier.actual(this.setup.falsePositiveAccessTagIsPermissiveHighwayTagIsWrong(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveAccessTagIsWrongHighwayTagIsCorrect()
    {
        this.verifier.actual(this.setup.falsePositiveAccessTagIsWrongHighwayTagIsCorrect(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveAccessTagIsWrongHighwayTagIsWrong()
    {
        this.verifier.actual(this.setup.falsePositiveAccessTagIsWrongHighwayTagIsWrong(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveAccessTagIsYesHighwayTagIsWrong()
    {
        this.verifier.actual(this.setup.falsePositiveAccessTagIsYesHighwayTagIsWrong(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsBridleway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsBridleway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsBusguideway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsBusguideway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsBusway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsBusway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsCycleway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsCycleway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsFootway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsFootway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsMotorway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsMotorway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsPath()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsPath(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsPedestrian()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsPedestrian(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsRaceway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsRaceway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsSteps()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsSteps(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsTrack()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsTrack(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsPermissiveHighwayTagIsTrunk()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsPermissiveHighwayTagIsTrunk(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsBridleway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsBridleway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsBusguideway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsBusguideway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsBusway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsBusway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsCycleway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsCycleway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsFootway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsFootway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsMotorway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsMotorway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsPath()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsPath(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsPedestrian()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsPedestrian(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsRaceway()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsRaceway(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsSteps()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsSteps(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsTrack()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsTrack(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAccessTagIsYesHighwayTagIsTrunk()
    {
        this.verifier.actual(this.setup.truePositiveAccessTagIsYesHighwayTagIsTrunk(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}

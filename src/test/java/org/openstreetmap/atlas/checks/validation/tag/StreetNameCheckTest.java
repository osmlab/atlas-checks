package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link StreetNameCheck}
 *
 * @author v-naydinyan
 */
public class StreetNameCheckTest
{
    @Rule
    public StreetNameCheckTestRule setup = new StreetNameCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    // FIXME
    private final StreetNameCheck check = new StreetNameCheck(
            ConfigurationResolver.emptyConfiguration());

    @Test
    public void falsePositiveAutNodeInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveAutNodeInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveAutRelationDeprecatedTag()
    {
        this.verifier.actual(this.setup.falsePositiveAutRelationDeprecatedTag(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveAutRelationInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveAutRelationInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveAutWayInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveAutWayInvalidTag(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveCheNodeInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveCheNodeInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveCheRelationDeprecatedTag()
    {
        this.verifier.actual(this.setup.falsePositiveCheRelationDeprecatedTag(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveCheRelationInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveCheRelationInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveCheWayInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveCheWayInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveDeuNodeInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveDeuNodeInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveDeuRelationDeprecatedTag()
    {
        this.verifier.actual(this.setup.falsePositiveDeuRelationDeprecatedTag(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveDeuRelationInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveDeuRelationInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveDeuWayInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveDeuWayInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveLieNodeInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveLieNodeInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveLieRelationDeprecatedTag()
    {
        this.verifier.actual(this.setup.falsePositiveLieRelationDeprecatedTag(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveLieRelationInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveLieRelationInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveLieWayInvalidValue()
    {
        this.verifier.actual(this.setup.falsePositiveLieWayInvalidValue(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void truePositiveAutNodeInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveAutNodeInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAutRelationInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveAutRelationInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveAutWayInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveAutWayInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveCheNodeInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveCheNodeInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveCheRelationInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveCheRelationInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveCheWayInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveCheWayInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveDeuNodeInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveDeuNodeInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveDeuRelationDeprecatedTag()
    {
        this.verifier.actual(this.setup.truePositiveDeuRelationDeprecatedTag(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveDeuRelationInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveDeuRelationInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveDeuWayInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveDeuWayInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveLieNodeInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveLieNodeInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveLieRelationInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveLieRelationInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveLieWayInvalidValue()
    {
        this.verifier.actual(this.setup.truePositiveLieWayInvalidValue(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}

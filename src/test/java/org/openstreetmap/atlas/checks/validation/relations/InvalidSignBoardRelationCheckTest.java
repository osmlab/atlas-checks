package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for InvalidSignBoardRelationCheck.
 *
 * @author micah-nacht
 */
public class InvalidSignBoardRelationCheckTest
{

    private static final InvalidSignBoardRelationCheck check = new InvalidSignBoardRelationCheck(
            ConfigurationResolver.emptyConfiguration());
    @Rule
    public InvalidSignBoardRelationCheckTestRule setup = new InvalidSignBoardRelationCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void disconnectedFromTest()
    {
        this.verifier.actual(this.setup.getDisconnectedFrom(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertTrue(
                flag.getInstructions().contains("the role=from members must be connected.")));
    }

    @Test
    public void fromMeetsReverseToTest()
    {
        this.verifier.actual(this.setup.getFromMeetsReverseTo(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void fromNotEdgeTest()
    {
        this.verifier.actual(this.setup.getFromNotEdge(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("the role=from member must be a way")));
    }

    @Test
    public void multipleFromsTest()
    {
        this.verifier.actual(this.setup.getMultipleFroms(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void noDestinationTest()
    {
        this.verifier.actual(this.setup.getNoDestination(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("must have a destination= tag")));
    }

    @Test
    public void noFromOrToTest()
    {
        this.verifier.actual(this.setup.getNoFromOrTo(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("must have a role=from member")
                        && flag.getInstructions().contains("must have a role=to member")));
    }

    @Test
    public void noFromTest()
    {
        this.verifier.actual(this.setup.getNoFrom(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("must have a role=from member")));
    }

    @Test
    public void noToTest()
    {
        this.verifier.actual(this.setup.getNoTo(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("must have a role=to member")));
    }

    @Test
    public void reverseFromMeetsReverseTo()
    {
        this.verifier.actual(this.setup.getReverseFromMeetsReverseTo(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void reverseFromMeetsToTest()
    {
        this.verifier.actual(this.setup.getReverseFromMeetsTo(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void toFromNoMeetingTest()
    {
        this.verifier.actual(this.setup.getToFromNoMeeting(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains("the role=from member must meet the role=to member")));
    }

    @Test
    public void toNotEdgeTest()
    {
        this.verifier.actual(this.setup.getToNotEdge(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("the role=to member must be a way")));
    }

    @Test
    public void twoToTest()
    {
        this.verifier.actual(this.setup.getTwoTo(), check);
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.stream().anyMatch(
                flag -> flag.getInstructions().contains("must have exactly one role=to member"))));
    }

    @Test
    public void validTest()
    {
        this.verifier.actual(this.setup.getValidSignBoard(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void waySectionedFromTest()
    {
        this.verifier.actual(this.setup.getWaySectionedFrom(), check);
        this.verifier.verifyEmpty();
    }
}

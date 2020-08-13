package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Unit tests for {@link AtGradeSignPostCheck}
 *
 * @author sayas01
 */
public class AtGradeSignPostCheckTest
{
    @Rule
    public AtGradeSignPostCheckTestRule setup = new AtGradeSignPostCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final Configuration inlineConfiguration = ConfigurationResolver
            .inlineConfiguration("{  \"AtGradeSignPostCheck\": {\n" + "    \"enabled\": true,\n"
                    + "    \"highway.filter\": \"highway->trunk,primary,secondary\",\n"
                    + "    \"connected.highway.types\": {\n"
                    + "      \"primary\": [\"trunk\",\"primary\",\"secondary\"],\n"
                    + "      \"trunk\": [\"primary\"],\n" + "      \"secondary\": [\"primary\"]\n"
                    + "    }\n" + "  }}");

    @Test
    public void testIncompleteDestinationSignRelation()
    {
        this.verifier.actual(this.setup.getIncompleteDestinationSignRelationAtlas(),
                new AtGradeSignPostCheck(this.inlineConfiguration));
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains("could also form destination sign "
                    + "relations with this node. Create new destination sign relation with these "
                    + "edges and the node"));
        });
    }

    @Test
    public void testLinkRoadConnectedAtGradeJunctions()
    {
        this.verifier.actual(this.setup.getLinkRoadConnectedAtGradeJunctionAtlas(),
                new AtGradeSignPostCheck(this.inlineConfiguration));
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(
                    "is the most logical route between the OSM ways connected to this node, but is either not part of a destination sign relation or is missing a destination sign tag."));
        });

    }

    @Test
    public void testMissingDestinationSignRelation()
    {
        this.verifier.actual(this.setup.getMissingDestinationSignRelationAtlas(),
                new AtGradeSignPostCheck(this.inlineConfiguration));
        // this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(4, flag.getFlaggedObjects().size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains("is not part of a destination sign relation"));
        });
    }

    @Test
    public void testMissingDestinationSignTag()
    {
        this.verifier.actual(this.setup.getMissingDestinationSignTagAtlas(),
                new AtGradeSignPostCheck(this.inlineConfiguration));
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains("missing \"destination\" tags"));
        });
    }

    @Test
    public void testRoundaboutConnectorDestinationSignTag()
    {
        this.verifier.actual(this.setup.getRoundaboutConnectorMissingDestinationSignTagAtlas(),
                new AtGradeSignPostCheck(this.inlineConfiguration));
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(8, flag.getFlaggedObjects().size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains("is part of a roundabout and forms an "
                            + "at-grade junction. It is part of destination sign relation(s). "
                            + "Either the existing relations are missing destination sign tag or "
                            + "following connected edges"));
        });
    }

    @Test
    public void testRoundaboutIntersectionDestinationSignRelation()
    {
        this.verifier.actual(this.setup.getRoundaboutIntersectionMissingDestinationSignRelation(),
                new AtGradeSignPostCheck(this.inlineConfiguration));
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(8, flag.getFlaggedObjects().size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains("is part of a roundabout and "
                    + "forms an at-grade junction with connected edges. Add destination sign "
                    + "relations with the node as \"intersection\" member and following connected "
                    + "edges"));
        });
    }
}

package org.openstreetmap.atlas.checks.validation.tag;

import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.tags.AddressStreetTag;

/**
 * Tests for {@link AbbreviatedAddressStreetCheck}.
 *
 * @author vlemberg
 */

public class AbbreviatedAddressStreetCheckTest
{
    @Rule
    public AbbreviatedAddressStreetCheckTestRule setup = new AbbreviatedAddressStreetCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private static final String INSTRUCTION_FORMAT = "1. OSM entity {0,number,#} has address {1} with abbreviated road type \"{2}\". According to conventions, it should be changed to \"{3}\".";

    @Test
    public void testFalsePositiveCase1()
    {
        this.verifier.actual(this.setup.getFalsePositiveCase1(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));

        this.verifier.verify(flag ->
        {
            Assert.assertEquals(MessageFormat.format(INSTRUCTION_FORMAT, 1000, "Rochester St. W.",
                    "St.", "Street"), flag.getInstructions());
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            final FeatureChange fixSuggestion = flag.getFixSuggestions().iterator().next();
            Assert.assertEquals(1, fixSuggestion.getTags().size());
            Assert.assertEquals("Rochester Street W.",
                    fixSuggestion.getTags().get(AddressStreetTag.KEY));
        });
    }

    @Test
    public void testFalsePositiveCase2()
    {
        this.verifier.actual(this.setup.getFalsePositiveCase2(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(MessageFormat.format(INSTRUCTION_FORMAT, 1000,
                    "Lower Honoapiilani Rd.", "Rd.", "Road"), flag.getInstructions());
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            final FeatureChange fixSuggestion = flag.getFixSuggestions().iterator().next();
            Assert.assertEquals(1, fixSuggestion.getTags().size());
            Assert.assertEquals("Lower Honoapiilani Road",
                    fixSuggestion.getTags().get(AddressStreetTag.KEY));
        });
    }

    @Test
    public void testFalsePositiveCase3()
    {
        this.verifier.actual(this.setup.getFalsePositiveCase3(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(MessageFormat.format(INSTRUCTION_FORMAT, 1000,
                    "N. Harbor Village W. Dr.", "Dr.", "Drive"), flag.getInstructions());
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            final FeatureChange fixSuggestion = flag.getFixSuggestions().iterator().next();
            Assert.assertEquals(1, fixSuggestion.getTags().size());
            Assert.assertEquals("N. Harbor Village W. Drive",
                    fixSuggestion.getTags().get(AddressStreetTag.KEY));
        });
    }

    @Test
    public void testFalsePositiveCase4()
    {
        this.verifier.actual(this.setup.getFalsePositiveCase4(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(MessageFormat.format(INSTRUCTION_FORMAT, 1000, "Fox Run Pkwy.",
                    "Pkwy.", "Parkway"), flag.getInstructions());
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            final FeatureChange fixSuggestion = flag.getFixSuggestions().iterator().next();
            Assert.assertEquals(1, fixSuggestion.getTags().size());
            Assert.assertEquals("Fox Run Parkway",
                    fixSuggestion.getTags().get(AddressStreetTag.KEY));
        });
    }

    @Test
    public void testInvalidRoadTypeNumericStreet()
    {
        this.verifier.actual(this.setup.getInvalidRoadTypeNumericStreet(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(
                    MessageFormat.format(INSTRUCTION_FORMAT, 1000, "1St St", "St", "Street"),
                    flag.getInstructions());
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            final FeatureChange fixSuggestion = flag.getFixSuggestions().iterator().next();
            Assert.assertEquals(1, fixSuggestion.getTags().size());
            Assert.assertEquals("1St Street", fixSuggestion.getTags().get(AddressStreetTag.KEY));
        });
    }

    @Test
    public void testInvalidRoadTypePoint()
    {
        this.verifier.actual(this.setup.getInvalidRoadTypePoint(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidRoadTypePoint2()
    {
        this.verifier.actual(this.setup.getInvalidRoadTypePoint2(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidRoadTypeSuffix()
    {
        // check when road type is followed by directional suffix e.g. N, S, E, W, NE, SE, SW, NW
        this.verifier.actual(this.setup.getInvalidRoadTypeSuffix(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidRoadTypeWay()
    {
        this.verifier.actual(this.setup.getInvalidRoadTypeWay(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testValidRoadType()
    {
        this.verifier.actual(this.setup.getValidRoadType(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}

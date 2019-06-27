package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link RoadNameSpellingConsistencyCheck} unit tests
 *
 * @author seancoulter
 */
public class RoadNameSpellingConsistencyCheckTest
{

    @Rule
    public RoadNameSpellingConsistencyCheckTestRule setup = new RoadNameSpellingConsistencyCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void oneSegmentInconsistentSpelling()
    {
        this.verifier.actual(this.setup.getOneSegmentInconsistentSpelling(),
                new RoadNameSpellingConsistencyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void moreThanOneSegmentInconsistentSpelling()
    {
        this.verifier.actual(this.setup.getMoreThanOneSegmentInconsistentSpelling(),
                new RoadNameSpellingConsistencyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void allSegmentsInconsistentSpelling()
    {
        this.verifier.actual(this.setup.getAllSegmentsInconsistentSpelling(),
                new RoadNameSpellingConsistencyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void oneSegmentInconsistentSpellingAccent()
    {
        this.verifier.actual(this.setup.getOneSegmentInconsistentSpellingAccent(),
                new RoadNameSpellingConsistencyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void noSegmentsInconsistentSpelling()
    {
        this.verifier.actual(this.setup.getNoSegmentsInconsistentSpelling(),
                new RoadNameSpellingConsistencyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

}

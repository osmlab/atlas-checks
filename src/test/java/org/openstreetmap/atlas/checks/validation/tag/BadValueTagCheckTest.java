package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author mm-ciub on 31/07/2020.
 */
public class BadValueTagCheckTest
{
    @Rule
    public BadValueTagCheckTestRule setup = new BadValueTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void illegalSourceEdge()
    {
        this.verifier.actual(this.setup.getIllegalSourceLinkEdge(),
                new BadValueTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void illegalSourceNode()
    {
        this.verifier.actual(this.setup.getIllegalSourceLinkNode(),
                new BadValueTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }
}

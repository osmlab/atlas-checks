package org.openstreetmap.atlas.checks.validation.tag;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link StreetNameIntegersOnlyCheck}
 *
 * @author bbreithaupt
 */

public class StreetNameIntegersOnlyCheckTest
{
    @Rule
    public StreetNameIntegersOnlyCheckTestRule setup = new StreetNameIntegersOnlyCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
}
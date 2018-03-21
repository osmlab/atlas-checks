package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Rule;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link AddressPointMatchCheck}
 *
 * @author savannahostrowski
 */
public class AddressPointMatchCheckTest
{
    @Rule
    public AddressPointMatchCheckTestRule setup = new AddressPointMatchCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

}

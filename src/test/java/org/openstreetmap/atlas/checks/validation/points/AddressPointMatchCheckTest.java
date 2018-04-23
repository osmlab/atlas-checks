package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link AddressPointMatchCheck}
 *
 * @author savannahostrowski
 */
public class AddressPointMatchCheckTest
{

    private static final String JONES_STREET_NAME = "Jones";
    private static final String JOHN_STREET_NAME = "John";
    @Rule
    public AddressPointMatchCheckTestRule setup = new AddressPointMatchCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    // Has street name, number and is part of associated street relation
    @Test
    public void pointWithStreetNameStreetNumberAndAssociatedStreet()
    {
        this.verifier.actual(this.setup.pointWithStreetNameStreetNumberAndAssociatedStreet(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    // Has no street name, has street number and has associated street relation
    @Test
    public void pointWithNoStreetNameStreetNumberAndAssociatedStreet()
    {
        this.verifier.actual(this.setup.pointWithNoStreetNameStreetNumberAndAssociatedStreet(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    // Has no street name, has street number, and has no associated street relation
    @Test
    public void pointWithNoStreetNameStreetNumberAndNoAssociatedStreet()
    {
        this.verifier.actual(this.setup.pointWithNoStreetNameStreetNumberAndNoAssociatedStreet(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void pointWithStreetNameStreetNumber()
    {
        this.verifier.actual(this.setup.pointWithStreetNameStreetNumber(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void pointWithStreetNameNoStreetNumberNoCandidates()
    {
        this.verifier.actual(this.setup.pointWithStreetNameNoStreetNumberNoCandidates(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void pointWithStreetNameNoStreetNumberPointCandidatesNoDuplicates()
    {
        this.verifier.actual(
                this.setup.pointWithStreetNameNoStreetNumberPointCandidatesNoDuplicates(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void pointWithStreetNameNoStreetNumberEdgeCandidatesNoDuplicates()
    {
        this.verifier.actual(
                this.setup.pointWithStreetNameNoStreetNumberEdgeCandidatesNoDuplicates(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void pointWithStreetNameNoStreetNumberPointCandidatesDuplicateNames()
    {
        this.verifier.actual(
                this.setup.pointWithStreetNameNoStreetNumberPointCandidatesDuplicateNames(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier
                .verify(flag -> Assert.assertTrue(flag.getInstructions().contains(JONES_STREET_NAME)
                        && flag.getInstructions().contains(JOHN_STREET_NAME)));
    }

    @Test
    public void pointWithStreetNameNoStreetNumberEdgeCandidatesDuplicateNames()
    {
        this.verifier.actual(
                this.setup.pointWithStreetNameNoStreetNumberEdgeCandidatesDuplicateNames(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(
                flag -> Assert.assertTrue(flag.getInstructions().contains(JONES_STREET_NAME)));
    }

    @Test
    public void pointWithEmptyStreetNameNoStreetNumberNoCandidates()
    {
        this.verifier.actual(this.setup.pointWithEmptyStreetNameNoStreetNumberNoCandidates(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void pointWithEmptyStreetNamePointCandidatesNoDuplicates()
    {
        this.verifier.actual(this.setup.pointWithEmptyStreetNamePointCandidatesNoDuplicates(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void pointWithEmptyStreetNameEdgeCandidatesNoDuplicates()
    {
        this.verifier.actual(this.setup.pointWithEmptyStreetNameEdgeCandidatesNoDuplicates(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void pointWithEmptyStreetNamePointCandidatesDuplicateNames()
    {
        this.verifier.actual(this.setup.pointWithEmptyStreetNamePointCandidatesDuplicateNames(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void pointWithEmptyStreetNameEdgeCandidatesDuplicateNames()
    {
        this.verifier.actual(this.setup.pointWithEmptyStreetNameEdgeCandidatesDuplicateNames(),
                new AddressPointMatchCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}

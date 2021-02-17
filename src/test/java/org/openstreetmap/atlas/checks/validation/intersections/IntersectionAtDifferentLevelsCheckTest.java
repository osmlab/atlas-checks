package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * {@link IntersectionAtDifferentLevelsCheck} unit test
 *
 * @author vladlemberg
 */
public class IntersectionAtDifferentLevelsCheckTest
{
    @Rule
    public IntersectionAtDifferentLevelsCheckTestRule setup = new IntersectionAtDifferentLevelsCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"IntersectionAtDifferentLevelsCheck\":{\"great.separation.filter\":\"bridge->yes|tunnel->yes|embankment->yes|cutting->yes|ford->yes\"}}");

    private final Configuration inlineConfigurationEmptyFilter = ConfigurationResolver
            .inlineConfiguration(
                    "{\"IntersectionAtDifferentLevelsCheck\":{\"great.separation.filter\":\"\"}}");

    @Test
    public void invalidLevel()
    {
        this.verifier.actual(this.setup.getInvalidLevels(),
                new IntersectionAtDifferentLevelsCheck(this.inlineConfigurationEmptyFilter));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidLevelArea()
    {
        this.verifier.actual(this.setup.getInvalidLevelsArea(),
                new IntersectionAtDifferentLevelsCheck(this.inlineConfigurationEmptyFilter));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void invalidLevelPedestrianCrossing()
    {
        this.verifier.actual(this.setup.getInvalidLevelsCrossing(),
                new IntersectionAtDifferentLevelsCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void invalidLevelRailwayCrossing()
    {
        this.verifier.actual(this.setup.getInvalidLevelsRailwayCrossing(),
                new IntersectionAtDifferentLevelsCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void invalidLevelWithFilter()
    {
        this.verifier.actual(this.setup.getInvalidLevelsFilter(),
                new IntersectionAtDifferentLevelsCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidLevelWithFilterIgnore()
    {
        this.verifier.actual(this.setup.getInvalidLevels(),
                new IntersectionAtDifferentLevelsCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(0);
    }
}

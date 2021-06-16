package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * {@link IntersectionAtDifferentLayersCheck} unit test
 *
 * @author vladlemberg
 */
public class IntersectionAtDifferentLayersCheckTest
{
    @Rule
    public IntersectionAtDifferentLayersCheckTestRule setup = new IntersectionAtDifferentLayersCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"IntersectionAtDifferentLayersCheck\":{\"great.separation.filter\":\"\","
                    + "\"indoor.mapping.filter\": \"indoor->*|highway->corridor,steps|level->*\"}}");

    private final Configuration inlineConfigurationGreatSeparationFilter = ConfigurationResolver
            .inlineConfiguration(
                    "{\"IntersectionAtDifferentLayersCheck\":{\"great.separation.filter\":\"bridge->yes|tunnel->yes|embankment->yes|cutting->yes|ford->yes\","
                            + "\"indoor.mapping.filter\": \"indoor->*|highway->corridor,steps|level->*\"}}");

    @Test
    public void invalidLayer()
    {
        this.verifier.actual(this.setup.invalidLayers(),
                new IntersectionAtDifferentLayersCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidLayerArea()
    {
        this.verifier.actual(this.setup.invalidLayersArea(),
                new IntersectionAtDifferentLayersCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void invalidLayerPedestrianCrossing()
    {
        this.verifier.actual(this.setup.invalidLayersCrossing(),
                new IntersectionAtDifferentLayersCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void invalidLayerRailwayCrossing()
    {
        this.verifier.actual(this.setup.invalidLayersRailwayCrossing(),
                new IntersectionAtDifferentLayersCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void invalidLayersGreatSeparationFilter()
    {
        this.verifier.actual(this.setup.invalidLayersGreatSeparationFilter(),
                new IntersectionAtDifferentLayersCheck(
                        this.inlineConfigurationGreatSeparationFilter));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidLayersGreatSeparationFilterNegative()
    {
        this.verifier.actual(this.setup.invalidLayers(), new IntersectionAtDifferentLayersCheck(
                this.inlineConfigurationGreatSeparationFilter));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void invalidLayersIndoorMappingFilter()
    {
        this.verifier.actual(this.setup.invalidLayersIndoorMapping(),
                new IntersectionAtDifferentLayersCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(0);
    }

}

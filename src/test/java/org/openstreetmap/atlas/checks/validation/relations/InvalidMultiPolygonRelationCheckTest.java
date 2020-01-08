package org.openstreetmap.atlas.checks.validation.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Test for {@link InvalidMultiPolygonRelationCheck}
 *
 * @author jklamer
 */
public class InvalidMultiPolygonRelationCheckTest
{

    private static final InvalidMultiPolygonRelationCheck referenceDefaultLocaleCheck = new InvalidMultiPolygonRelationCheck(
            ConfigurationResolver.emptyConfiguration());
    @Rule
    public final InvalidMultiPolygonRelationCheckTestRule setup = new InvalidMultiPolygonRelationCheckTestRule();

    @Rule
    public final ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void invalidMemberType()
    {
        this.verifyCheck(this.setup.getInvalidMemberType(),
                ConfigurationResolver.emptyConfiguration(), 1,
                Arrays.asList(referenceDefaultLocaleCheck.getLocalizedInstruction(
                        InvalidMultiPolygonRelationCheck.INVALID_OSM_TYPE_INSTRUCTION_FORMAT_INDEX,
                        1L, 39033L, 1L, Arrays.asList(Tuple.createTuple("node", 39007L)))));
    }

    @Test
    public void testNoOuter()
    {
        this.verifyCheck(this.setup.getNoOuterRelation(),
                ConfigurationResolver.emptyConfiguration(), 1,
                Arrays.asList(referenceDefaultLocaleCheck.getLocalizedInstruction(
                        InvalidMultiPolygonRelationCheck.MISSING_OUTER_INSTRUCTION_FORMAT_INDEX,
                        39569L)));
    }

    @Test
    public void testOneMember()
    {
        this.verifyCheck(this.setup.getOneMemberRelation(),
                ConfigurationResolver.emptyConfiguration(), 1,
                Arrays.asList(referenceDefaultLocaleCheck.getLocalizedInstruction(
                        InvalidMultiPolygonRelationCheck.SINGLE_MEMBER_RELATION_INSTRUCTION_FORMAT_INDEX,
                        39570L)));
    }

    @Test
    public void testOpenPolygon()
    {
        this.verifier.actual(this.setup.getAtlas(), referenceDefaultLocaleCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            final List<Location> openLocations = new ArrayList<>();
            openLocations.add(Location.forString(InvalidMultiPolygonRelationCheckTestRule.ONE));
            openLocations.add(Location.forString(InvalidMultiPolygonRelationCheckTestRule.THREE));
            final Relation relation = this.setup.getAtlas().relation(Long.valueOf(
                    InvalidMultiPolygonRelationCheckTestRule.RELATION_ID_OPEN_MULTIPOLYGON));
            final Set<Long> memberIds = relation.members().stream()
                    .map(member -> member.getEntity().getOsmIdentifier())
                    .collect(Collectors.toSet());
            Assert.assertTrue(flag.getInstructions()
                    .contains(referenceDefaultLocaleCheck.getLocalizedInstruction(
                            InvalidMultiPolygonRelationCheck.CLOSED_LOOP_INSTRUCTION_FORMAT_INDEX,
                            relation.getOsmIdentifier(), memberIds, openLocations)));

        });
    }

    @Test
    public void testOpenRelation()
    {
        this.verifyCheck(this.setup.getOpenRelation(), ConfigurationResolver.emptyConfiguration(),
                1,
                Arrays.asList(referenceDefaultLocaleCheck.getLocalizedInstruction(
                        InvalidMultiPolygonRelationCheck.CLOSED_LOOP_INSTRUCTION_FORMAT_INDEX,
                        39569L, Stream.of(39766L, 39565L).collect(Collectors.toSet()),
                        Arrays.asList(Location.forWkt("POINT (103.9145902 1.4119302)"),
                                Location.forWkt("POINT (103.9256395 1.4483904)")))));
    }

    // Missing outer role for two members means the relation is also not closed.
    @Test
    public void testTwoMemberNoRole()
    {
        this.verifyCheck(this.setup.getValidRelationTwoNoRole(),
                ConfigurationResolver.emptyConfiguration(), 1,
                Arrays.asList(referenceDefaultLocaleCheck.getLocalizedInstruction(
                        InvalidMultiPolygonRelationCheck.INVALID_ROLE_INSTRUCTION_FORMAT_INDEX, 1L,
                        39152L, 1L, Arrays.asList(39056L)),
                        referenceDefaultLocaleCheck.getLocalizedInstruction(
                                InvalidMultiPolygonRelationCheck.CLOSED_LOOP_INSTRUCTION_FORMAT_INDEX,
                                39152L, Stream.of(39056L, 39040L).collect(Collectors.toSet()),
                                Arrays.asList(Location.forWkt("POINT (103.9215138 1.4328881)"),
                                        Location.forWkt("POINT (103.9211095 1.4134905)")))));
    }

    @Test
    public void testValidRelation()
    {
        this.verifyCheck(this.setup.getValidRelation(), ConfigurationResolver.emptyConfiguration(),
                0, Collections.emptyList());
    }

    @Test
    public void testValidRelationWithWaySection()
    {
        this.verifyCheck(this.setup.getValidRelationWithWaySectioning(),
                ConfigurationResolver.emptyConfiguration(), 0, Collections.emptyList());
    }

    private void verifyCheck(final Atlas atlas, final Configuration configuration,
            final int numberFlags, final List<String> instructions)
    {
        final InvalidMultiPolygonRelationCheck check = new InvalidMultiPolygonRelationCheck(
                configuration);
        this.verifier.actual(atlas, check);
        this.verifier.verifyExpectedSize(numberFlags);
        this.verifier.verify(checkFlag ->
        {
            System.out.print(checkFlag.getInstructions());
            instructions.forEach(instruction -> Assert
                    .assertTrue(checkFlag.getInstructions().contains(instruction)));
        });

    }
}

package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link DuplicateRelationCheck}
 *
 * @author Xiaohong Tang
 */
public class DuplicateRelationCheckTest
{
    private static final DuplicateRelationCheck check = new DuplicateRelationCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public final DuplicateRelationCheckTestRule setup = new DuplicateRelationCheckTestRule();

    @Rule
    public final ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testDifferentMembersRelations()
    {
        this.verifier.actual(this.setup.getDifferentMembersRelations(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testDifferentOSMTagsAndMembersRelations()
    {
        this.verifier.actual(this.setup.getDifferentOSMTagsAndMembersRelations(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testDifferentRolesOnMembersRelations()
    {
        this.verifier.actual(this.setup.getDifferentRolesOnMembersRelations(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testDuplicateRelation()
    {
        this.verifier.actual(this.setup.getDuplicateRelations(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testSameOSMTagsAndMembersRelations()
    {
        this.verifier.actual(this.setup.getSameOSMTagsAndMembersRelations(), check);
        this.verifier.verifyExpectedSize(1);
    }
}

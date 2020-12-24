package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

public class MissingRelationTypeCheckTest
{
    @Rule
    public final MissingRelationTypeCheckTestRule setup = new MissingRelationTypeCheckTestRule();

    @Rule
    public final ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"MissingRelationTypeCheck\":{\"ignore.tags.filter\":\"disused:type->!&disabled:type->!\"}}");

    @Test
    public void missingRelationType()
    {
        this.verifier.actual(this.setup.missingRelationType(),
                new MissingRelationTypeCheck(this.inlineConfiguration));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void missingRelationTypeDisabled()
    {
        this.verifier.actual(this.setup.getMissingRelationTypeDisabled(),
                new MissingRelationTypeCheck(this.inlineConfiguration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void missingRelationTypeDisused()
    {
        this.verifier.actual(this.setup.getMissingRelationTypeDisused(),
                new MissingRelationTypeCheck(this.inlineConfiguration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validRelation()
    {
        this.verifier.actual(this.setup.getValidRelation(),
                new MissingRelationTypeCheck(this.inlineConfiguration));
        this.verifier.verifyEmpty();
    }
}

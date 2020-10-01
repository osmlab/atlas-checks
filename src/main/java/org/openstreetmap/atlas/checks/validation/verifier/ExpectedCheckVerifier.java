package org.openstreetmap.atlas.checks.validation.verifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.rules.Verifier;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;

import com.google.common.collect.Iterables;

/**
 * JUnit verifier for comparing an expected list of CheckFlags with those actually created by the
 * checks under test
 *
 * @author cstaylor
 */
public class ExpectedCheckVerifier extends Verifier
{
    private final List<CheckFlag> expected;

    private final List<CheckFlag> actual;

    /**
     * Default constructor
     */
    public ExpectedCheckVerifier()
    {
        this.expected = new ArrayList<>();
        this.actual = new ArrayList<>();
    }

    /**
     * Adds a test {@link Atlas} and a {@link Check} to verify
     *
     * @param atlas
     *            a test {@link Atlas}
     * @param check
     *            the {@link Check} under test
     */
    public void actual(final Atlas atlas, final BaseCheck<?> check)
    {
        Iterables.addAll(this.actual, check.flags(atlas));
    }

    /**
     * Verifies that the expected {@link CheckFlag}s are returned when the {@link Check} is run over
     * the test {@link Atlas}
     *
     * @param flags
     *            expected {@link CheckFlag}s
     * @return the {@link ExpectedCheckVerifier}
     */
    public ExpectedCheckVerifier expect(final CheckFlag... flags)
    {
        this.expected.addAll(Arrays.asList(flags));
        return this;
    }

    @Override
    protected void verify() throws Throwable
    {
        Assert.assertEquals(this.expected, this.actual);
    }
}

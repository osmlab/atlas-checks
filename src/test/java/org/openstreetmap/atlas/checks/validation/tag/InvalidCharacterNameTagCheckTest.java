package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit test for {@link InvalidCharacterNameTagCheck}
 *
 * @author sayas01
 */
public class InvalidCharacterNameTagCheckTest
{
    @Rule
    public InvalidCharacterNameTagCheckTestRule setup = new InvalidCharacterNameTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final InvalidCharacterNameTagCheck check = new InvalidCharacterNameTagCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"InvalidCharacterNameTagCheck\":{\"valid.object.filter\":\"natural->spring,"
                            + "hot_spring&&name->*||natural->water,lake,pond,reservoir,stream,"
                            + "tidalflat,reedbed,lagoon||water:type->lake||water->lake,pond,oxbow,"
                            + "salt_lake,tidalflat,reedbed,water,Perennial,reservoir,canal,river,"
                            + "lock,moat,riverbank,creek,stream,stream_pool,lagoon||water->"
                            + "drain&&name->*||water->dam,Dam&&natural->water||landuse->pond,"
                            + "reservoir,water||seamark:type->dam&&natural->water||waterway->river,"
                            + "riverbank,brook,ditch,stream,creek,canal,derelict_canal||stream->*||"
                            + "waterway->drain&&name->*||waterway->water,lagoon||wetland->tidalflat,"
                            + "reedbed||natural->lake||||water->lake&&&intermittent->dry||||seasonal->dry||||natural->dry_lake&natural->*&&natural->!dock&&&natural->!water_point&&&natural->!floodway&&&natural->!spillway&&&natural->!wastewater&&&natural->!waterhole||waterway->*&&waterway->!lock_gate&&&waterway->!dock&&&waterway->!water_point&&&waterway->!floodway&&&waterway->!spillway&&&waterway->!wastewater&&&waterway->!waterhole&&&waterway->!culvert&&&waterway->!dam&&&waterway->!waterfall&&&waterway->!fish_pass&&&waterway->!dry_dock&&&waterway->!construction&&&waterway->!boat_lift&&&waterway->!weir&&&waterway->!breakwater&&&waterway->!boatyard||water->*&&water->!lock_gate&&&water->!dock&&&water->!water_point&&&water->!floodway&&&water->!spillway&&&water->!wastewater&&&water->!waterhole&&&water->!pool&&&water->!reflecting_pool&&&water->!swimming_pool&&&water->!salt_pool&&&water->!fountain&&&water->!tank&&&water->!fish_pass||tunnel->*&&tunnel->!culvert||waterway->*&&waterway->!drain&&name->!||water->*&&water->!drain&&name->!||wetland->*&&seasonal->yes&&wetland->!tidalflat&&&wetland->!reedbed||water->*&&seasonal->yes&&water->!tidalflat&&&water->!reedbed||natural->*&&seasonal->yes&&natural->!tidalflat&&&natural->!reedbed\"}}"));

    @Test
    public void testDoubleQuotesInLocalizedNameTag()
    {
        this.verifier.actual(this.setup.getDoubleQuotesInLocalizedNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testDoubleQuotesInNameTag()
    {
        this.verifier.actual(this.setup.getDoubleQuotesInNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidCharacterInArea()
    {
        this.verifier.actual(this.setup.getInvalidCharacterInAreaAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidCharacterInRelation()
    {
        this.verifier.actual(this.setup.getInvalidCharacterInRelationAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testNumbersInLocalizedNameTag()
    {
        this.verifier.actual(this.setup.getNumbersInLocalizedNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testNumbersInNameTag()
    {
        this.verifier.actual(this.setup.getNumbersInNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSmartQuotesInLocalizedNameTag()
    {
        this.verifier.actual(this.setup.getSmartQuotesInLocalizedNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSmartQuotesInNameTag()
    {
        this.verifier.actual(this.setup.getSmartQuotesInNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSpecialCharactersInLocalizedNameTag()
    {
        this.verifier.actual(this.setup.getSpecialCharactersInLocalizedNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSpecialCharactersInNameTag()
    {
        this.verifier.actual(this.setup.getSpecialCharactersInNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}

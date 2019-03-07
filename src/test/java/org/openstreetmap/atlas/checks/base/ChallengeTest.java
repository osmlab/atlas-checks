package org.openstreetmap.atlas.checks.base;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.checks.BaseTestCheck;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengeDifficulty;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengePriority;
import org.openstreetmap.atlas.streaming.resource.ClassResource;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author cuthbertm
 */
public class ChallengeTest
{

    @Test
    public void testChallengeJSON()
    {
        final Configuration configuration = new StandardConfiguration(
                new ClassResource("org/openstreetmap/atlas/checks/base/InlineChallengeTest.json"));
        final BaseTestCheck testCheck = new BaseTestCheck(configuration);
        Assert.assertEquals("description", testCheck.getChallenge().getDescription().toLowerCase());
        Assert.assertEquals("blurb", testCheck.getChallenge().getBlurb().toLowerCase());
        Assert.assertEquals("instruction", testCheck.getChallenge().getInstruction().toLowerCase());
        Assert.assertEquals("#maproulette",
                testCheck.getChallenge().getCheckinComment().toLowerCase());
        Assert.assertEquals(ChallengeDifficulty.EASY, testCheck.getChallenge().getDifficulty());
        Assert.assertEquals(ChallengePriority.LOW, testCheck.getChallenge().getDefaultPriority());

        final JsonObject challengeJSON = testCheck.getChallenge().toJson("TestChallenge");
        final JsonObject highPriority = new Gson().fromJson(
                challengeJSON.get(Challenge.KEY_HIGH_PRIORITY).getAsString(), JsonObject.class);
        final JsonArray rules = highPriority.getAsJsonArray(Challenge.KEY_PRIORITY_RULES);
        Assert.assertEquals(4, rules.size());

        final JsonObject firstRule = rules.get(0).getAsJsonObject();
        Assert.assertEquals(Challenge.VALUE_RULE_ID,
                firstRule.get(Challenge.KEY_RULE_ID).getAsString());
        Assert.assertEquals(Challenge.VALUE_RULE_FIELD,
                firstRule.get(Challenge.KEY_RULE_FIELD).getAsString());
        Assert.assertEquals(Challenge.VALUE_RULE_OPERATOR,
                firstRule.get(Challenge.KEY_RULE_OPERATOR).getAsString());
        Assert.assertEquals(Challenge.VALUE_RULE_TYPE,
                firstRule.get(Challenge.KEY_RULE_TYPE).getAsString());
        Assert.assertEquals("highway.motorway",
                firstRule.get(Challenge.KEY_RULE_VALUE).getAsString());

        Assert.assertEquals("highway.motorway_link",
                rules.get(1).getAsJsonObject().get(Challenge.KEY_RULE_VALUE).getAsString());
        Assert.assertEquals("highway.trunk",
                rules.get(2).getAsJsonObject().get(Challenge.KEY_RULE_VALUE).getAsString());
        Assert.assertEquals("highway.trunk_link",
                rules.get(3).getAsJsonObject().get(Challenge.KEY_RULE_VALUE).getAsString());
    }
}

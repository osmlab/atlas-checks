package org.openstreetmap.atlas.checks.maproulette.data;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.maproulette.serializer.ChallengeDifficultySerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author cuthbertm
 */
public class Challenge implements Serializable
{
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_UPDATE_TASKS = "updateTasks";
    public static final String KEY_NAME = "name";
    public static final String KEY_DEFAULT_PRIORITY = "defaultPriority";
    public static final String KEY_HIGH_PRIORITY = "highPriorityRule";
    public static final String KEY_MEDIUM_PRIORITY = "mediumPriorityRule";
    public static final String KEY_LOW_PRIORITY = "lowPriorityRule";
    public static final String KEY_PRIORITY_RULES = "rules";
    public static final String KEY_RULE_ID = "id";
    public static final String VALUE_RULE_ID = "tag";
    public static final String KEY_RULE_FIELD = "field";
    public static final String VALUE_RULE_FIELD = "tag";
    public static final String KEY_RULE_TYPE = "type";
    public static final String VALUE_RULE_TYPE = "string";
    public static final String KEY_RULE_OPERATOR = "operator";
    public static final String VALUE_RULE_OPERATOR = "equal";
    public static final String KEY_RULE_VALUE = "value";
    public static final String KEY_TAGS = "tags";

    private static final long serialVersionUID = -8034692909431083341L;
    private static final Gson CHALLENGE_GSON = new GsonBuilder().disableHtmlEscaping()
            .registerTypeAdapter(ChallengeDifficulty.class, new ChallengeDifficultySerializer())
            .create();

    @SuppressWarnings("checkstyle:memberName")
    private long id = -1;
    private long parent = -1;
    private final String blurb;
    private final String description;
    private final ChallengeDifficulty difficulty;
    private final String instruction;
    private String name;
    private final String tags;
    private final ChallengePriority defaultPriority;
    private final String highPriorityRule;
    private final String mediumPriorityRule;
    private final String lowPriorityRule;

    public Challenge(final String name, final String description, final String blurb,
            final String instruction, final ChallengeDifficulty difficulty, final String tags)
    {
        this(name, description, blurb, instruction, difficulty, ChallengePriority.NONE, null, null,
                null, tags);
    }

    public Challenge(final String name, final String description, final String blurb,
            final String instruction, final ChallengeDifficulty difficulty,
            final ChallengePriority defaultPriority, final String highPriorityRule,
            final String mediumPriorityRule, final String lowPriorityRule, final String tags)
    {
        this.name = name;
        this.description = description;
        this.blurb = blurb;
        this.instruction = instruction;
        this.difficulty = difficulty;
        this.defaultPriority = defaultPriority;
        this.highPriorityRule = highPriorityRule;
        this.mediumPriorityRule = mediumPriorityRule;
        this.lowPriorityRule = lowPriorityRule;
        this.tags = tags;
    }

    public long getId()
    {
        return this.id;
    }

    public void setId(final long identifier)
    {
        this.id = identifier;
    }

    public long getParentIdentifier()
    {
        return this.parent;
    }

    public void setParentIdentifier(final long identifier)
    {
        this.parent = identifier;
    }

    public String getBlurb()
    {
        return this.blurb;
    }

    public ChallengePriority getDefaultPriority()
    {
        return this.defaultPriority;
    }

    public String getDescription()
    {
        return this.description;
    }

    public ChallengeDifficulty getDifficulty()
    {
        return this.difficulty;
    }

    public String getHighPriorityRule()
    {
        return this.highPriorityRule;
    }

    public String getInstruction()
    {
        return this.instruction;
    }

    public String getLowPriorityRule()
    {
        return this.lowPriorityRule;
    }

    public String getMediumPriorityRule()
    {
        return this.mediumPriorityRule;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getTags()
    {
        return tags;
    }

    public JsonObject toJson(final String challengeName)
    {
        // if the challenge doesn't exist yet then create/update it
        final JsonObject challengeJson = CHALLENGE_GSON.toJsonTree(this).getAsJsonObject();
        challengeJson.add(KEY_ACTIVE, new JsonPrimitive(true));
        challengeJson.add(KEY_UPDATE_TASKS, new JsonPrimitive(true));

        // Do not override the name if it's already set
        if (this.name.isEmpty())
        {
            challengeJson.add(KEY_NAME, new JsonPrimitive(challengeName));
        }

        if (this.defaultPriority != null && this.defaultPriority != ChallengePriority.NONE)
        {
            challengeJson.add(KEY_DEFAULT_PRIORITY,
                    new JsonPrimitive(this.defaultPriority.intValue()));
            challengeJson.addProperty(KEY_HIGH_PRIORITY,
                    this.getPriorityRuleString(this.highPriorityRule));
            challengeJson.add(KEY_MEDIUM_PRIORITY,
                    new JsonPrimitive(this.getPriorityRuleString(this.mediumPriorityRule)));
            challengeJson.add(KEY_LOW_PRIORITY,
                    new JsonPrimitive(this.getPriorityRuleString(this.lowPriorityRule)));
        }
        return challengeJson;
    }

    private String getPriorityRuleString(final String priorityString)
    {
        if (StringUtils.isEmpty(priorityString))
        {
            return CommonConstants.EMPTY_STRING;
        }
        else
        {
            // if we are using the shorthand priority ie. ["highway=primary"] then we need to
            // convert to the json object that MapRoulette requires
            final JsonArray fixedRules = new JsonArray();
            final JsonObject priority = CHALLENGE_GSON.fromJson(priorityString, JsonObject.class);
            final JsonArray rules = priority.getAsJsonArray(KEY_PRIORITY_RULES);
            rules.forEach(rule ->
            {
                if (rule.isJsonObject())
                {
                    fixedRules.add(rule);
                }
                else
                {
                    // if it is not an object then we assume it is a string in the form
                    // "highway=primary"
                    final String[] ruleElements = rule.getAsString().split("=");
                    // if rule elements not = 2 then we assume it is invalid and drop it
                    if (ruleElements.length == 2)
                    {
                        final JsonObject ruleObject = new JsonObject();
                        ruleObject.add(KEY_RULE_ID, new JsonPrimitive(VALUE_RULE_ID));
                        ruleObject.add(KEY_RULE_FIELD, new JsonPrimitive(VALUE_RULE_FIELD));
                        ruleObject.add(KEY_RULE_TYPE, new JsonPrimitive(VALUE_RULE_TYPE));
                        ruleObject.add(KEY_RULE_OPERATOR, new JsonPrimitive(VALUE_RULE_OPERATOR));
                        ruleObject.add(KEY_RULE_VALUE, new JsonPrimitive(
                                String.format("%s.%s", ruleElements[0], ruleElements[1])));
                        fixedRules.add(ruleObject);
                    }
                }
            });
            priority.add(KEY_PRIORITY_RULES, fixedRules);
            return priority.toString();
        }
    }
}

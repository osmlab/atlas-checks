package org.openstreetmap.atlas.checks.maproulette.data;

import java.util.List;

import org.openstreetmap.atlas.streaming.resource.ClassResource;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * A survey is very similar to a challenge with only one minor difference in that the instruction is
 * really a question and it contains answers for that question for the user to respond too. The idea
 * is to collect data rather than to edit data.
 *
 * @author cuthbertm
 */
public class Survey extends Challenge
{
    public static final String KEY_ANSWERS = "answers";
    public static final String KEY_CHALLENGE = "challenge";
    private final List<String> answers;

    /**
     * Creates a Challenge based on a JSON resource file
     *
     * @param checkName
     *            The name of the check that will look for the json challenge info
     * @return The challenge based from the resource json file
     */
    public static Challenge fromResource(final String checkName)
    {
        final ClassResource challenge = new ClassResource(checkName + FileSuffix.JSON.toString());
        return challenge.getJSONResourceObject(Survey.class);
    }

    public Survey(final String name, final String description, final String blurb,
            final String instruction, final ChallengeDifficulty difficulty,
            final List<String> answers, final String tags)
    {
        super(name, description, blurb, instruction, difficulty, tags);
        this.answers = answers;
    }

    public List<String> getAnswers()
    {
        return this.answers;
    }

    @Override
    public JsonObject toJson(final String challengeName)
    {
        final JsonObject challengeJson = super.toJson(challengeName);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.create();
        final JsonObject surveyJson = gson.fromJson("{}", JsonObject.class);
        surveyJson.add(KEY_CHALLENGE, challengeJson);
        surveyJson.add(KEY_ANSWERS, gson.toJsonTree(this.answers));
        return surveyJson;
    }
}

package org.openstreetmap.atlas.checks.maproulette.data.cooperative_challenge;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.TagChangeDescriptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/***
 * This is the cooperative challenge model for Tag changes Reference:
 * https://github.com/osmlab/maproulette3/wiki/Cooperative-Challenges#creating-tag-fix-challenges
 *
 * @author seancoulter
 */
public class TagChangeOperation extends CooperativeChallengeOperation
{

    private static final String UNSET_TAGS = "unsetTags";
    private static final String SET_TAGS = "setTags";

    public TagChangeOperation(final ChangeDescription changeDescription)
    {
        super(changeDescription);
    }

    /**
     * Using fields from the {@link ChangeDescription} associated with this
     * {@link TagChangeOperation}, create the operation it stands for
     *
     * @return this {@link TagChangeOperation}
     */
    @Override
    public TagChangeOperation create()
    {
        final JsonObject json = new JsonObject();
        final JsonObject data = new JsonObject();
        data.add(ID_KEY, new JsonPrimitive(this.getIdentifier()));
        final JsonArray operationsArray = new JsonArray();
        // Convert each change descriptor to a cooperativeWork object for use in a cooperative
        // challenge
        this.getChangeDescriptorList().forEach(changeDescriptor ->
        {
            final JsonObject nestedOperation = new JsonObject();
            final String action = this
                    .convertChangeDescriptorType(changeDescriptor.getChangeDescriptorType());
            nestedOperation.add(OPERATION_KEY, new JsonPrimitive(action));
            // we only check 'unset' which removes a tag and 'set' which adds or updates a tag
            if (UNSET_TAGS.equals(action))
            {
                final JsonArray tagChanges = new JsonArray();
                tagChanges
                        .add(new JsonPrimitive(((TagChangeDescriptor) changeDescriptor).getKey()));
                nestedOperation.add(DATA_KEY, tagChanges);
            }
            else if (SET_TAGS.equals(action))
            {
                final JsonObject tagChanges = new JsonObject();
                tagChanges.add(((TagChangeDescriptor) changeDescriptor).getKey(),
                        new JsonPrimitive(((TagChangeDescriptor) changeDescriptor).getValue()));
                nestedOperation.add(DATA_KEY, tagChanges);
            }
            operationsArray.add(nestedOperation);
        });
        data.add(OPERATIONS_KEY, operationsArray);
        json.add(OPERATION_TYPE_KEY,
                new JsonPrimitive(this.convertAction(this.getOperationType())));
        json.add(DATA_KEY, data);
        this.setJson(json);
        return this;
    }

    /**
     * Converts {@link ChangeDescription} terminology to cooperative challenge terminology
     *
     * @param descriptorType
     *            the {@link ChangeDescriptorType}
     * @return the cooperative challenge translation
     */
    private String convertAction(final ChangeDescriptorType descriptorType)
    {
        if (descriptorType.equals(ChangeDescriptorType.ADD)
                || descriptorType.equals(ChangeDescriptorType.UPDATE))
        {
            return "modifyElement";
        }
        if (descriptorType.equals(ChangeDescriptorType.REMOVE))
        {
            return "removeElement";
        }
        // Unsupported
        return "";
    }

    /**
     * Converts {@link ChangeDescription} terminology to cooperative challenge terminology
     *
     * @param descriptorType
     *            the {@link ChangeDescriptorType}
     * @return the cooperative challenge translation
     */
    private String convertChangeDescriptorType(final ChangeDescriptorType descriptorType)
    {
        if (descriptorType.equals(ChangeDescriptorType.UPDATE))
        {
            return SET_TAGS;
        }
        if (descriptorType.equals(ChangeDescriptorType.REMOVE))
        {
            return UNSET_TAGS;
        }
        // Unsupported
        return "";
    }
}

package org.openstreetmap.atlas.checks.maproulette.data.cooperative_challenge;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

import com.google.gson.JsonObject;

/***
 * This represents an operation that is embedded in a cooperative challenge. Example: Given a
 * FeatureCollection geojson uploaded to MapRoulette:
 * 
 * <pre>
 * {
 *   "type": "FeatureCollection",
 *   "features": [ ... ],            // omitted for readability
 *   "cooperativeWork": {            // special `cooperativeWork` property
 *     "meta": {
 *       "version": 2,               // must be format version `2`
 *       "type": 1                   // `1` for tag fix type
 *     },
 *     "operations": [               // Operations section (see below)
 *       ...
 *     ]
 *   }
 * }
 * </pre>
 * 
 * This class is an abstract representation of the json material to be found under "operations". Its
 * subclasses should handle the various ChangeDescriptor types, including GeometryChangeDescriptor,
 * LongElementChangeDescriptor, RelationMemberChangeDescriptor, TagChangeDescriptor
 *
 * @author seancoulter
 */
public abstract class CooperativeChallengeOperation
{
    private final ChangeDescriptorType operationType;
    private final String identifier;
    private final List<ChangeDescriptor> changeDescriptorList;
    private JsonObject json;
    protected static final String OPERATION_TYPE_KEY = "operationType";
    protected static final String DATA_KEY = "data";
    protected static final String ID_KEY = "id";
    protected static final String OPERATIONS_KEY = "operations";
    protected static final String OPERATION_KEY = "operation";
    private static final String DELIMITER = "/";
    private static final int ATLAS_SECTIONING_IDENTIFIER_LENGTH = 6;

    /**
     * Return the string representation of the OSM data type best representing the itemType
     *
     * @param itemType
     *            Atlas item type
     * @return "relation", "node", or "way"
     */
    private static String extractOSMObjectFromItemType(final ItemType itemType)
    {
        if ("RELATION".equals(itemType.name()))
        {
            return "relation";
        }
        if ("NODE".equals(itemType.name()) || "POINT".equals(itemType.name()))
        {
            return "node";
        }
        return "way";
    }

    protected CooperativeChallengeOperation(final ChangeDescription changeDescription)
    {
        this.operationType = changeDescription.getChangeDescriptorType();
        final String rawId = Long.toString(changeDescription.getIdentifier());
        this.identifier = String.join(DELIMITER,
                extractOSMObjectFromItemType(changeDescription.getItemType()),
                rawId.length() > ATLAS_SECTIONING_IDENTIFIER_LENGTH
                        ? rawId.substring(0, rawId.length() - ATLAS_SECTIONING_IDENTIFIER_LENGTH)
                        : rawId);
        // Filter only for the change descriptors relevant to the instance calling this constructor
        this.changeDescriptorList = changeDescription.getChangeDescriptors().stream()
                .filter(operation -> this.operationFilter().test(operation))
                .collect(Collectors.toList());
    }

    public abstract CooperativeChallengeOperation create();

    public JsonObject getJson()
    {
        return this.json;
    }

    protected List<ChangeDescriptor> getChangeDescriptorList()
    {
        return this.changeDescriptorList;
    }

    protected String getIdentifier()
    {
        return this.identifier;
    }

    protected ChangeDescriptorType getOperationType()
    {
        return this.operationType;
    }

    protected abstract Predicate<ChangeDescriptor> operationFilter();

    protected void setJson(final JsonObject json)
    {
        this.json = json;
    }

}

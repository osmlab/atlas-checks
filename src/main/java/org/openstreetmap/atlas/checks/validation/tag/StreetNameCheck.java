package org.openstreetmap.atlas.checks.validation.tag;

import java.util.*;
import java.util.Arrays;
import java.lang.System;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.EdgeTable;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author v-naydinyan
 */
public class StreetNameCheck extends BaseCheck
{
    private static final String COUNTRY_DEFAULT = "LIE";
    private final String checkCountry;

    private final String germanyISO = "DEU";
    private final String austriaISO = "AUT";
    private final String liechtISO = "LIE";
    private final String switzISO = "CHE";

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public StreetNameCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
//        private static final String FALLBACK_INSTRUCTIONS = "The object with OSM ID {0,number,#} with a street_name {1} contains character {2}";

        this.checkCountry = (String) this.configurationValue(configuration, "country", COUNTRY_DEFAULT);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Checks that the object is in the ISO list and is Node, Edge or Relation
        return (!this.isFlagged(object.getOsmIdentifier()) && (object instanceof
                Node || object instanceof Edge || object instanceof Relation));
    }

    @Override
    /**
     * The function that flags an item. If it is an edge, then it flags the entire way, otherwise flags the object.
     * The function written by Brian Jorgenson in ConstructionCheck.java.
     */
    protected CheckFlag createFlag(final AtlasObject object, final String instruction)
    {
        if (object instanceof Edge)
        {
            return super.createFlag(new OsmWayWalker((Edge) object).collectEdges(), instruction);
        }
        return super.createFlag(object, instruction);
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if ((checkCountry.equalsIgnoreCase(germanyISO) || checkCountry.equalsIgnoreCase(austriaISO)) && flagAutDeu(object)){
            return Optional.of(this.createFlag(object, "flag the germany and austria"));
        }

        if ((checkCountry.equalsIgnoreCase(liechtISO) || checkCountry.equalsIgnoreCase(switzISO)) && flagCheLie(object)){
            return Optional.of(this.createFlag(object, "flag the liecht and switzerladn"));
        }

        return Optional.empty();
    }

//    @Override
//    protected List<String> getFallbackInstructions()
//    {
//        return FALLBACK_INSTRUCTIONS;
//    }
    private boolean flagAutDeu(final AtlasObject object) {
        final Map<String, String> tags = object.getTags();
        String street_tag = tags.get(AddressStreetTag.KEY);
        String name_tag = tags.get(NameTag.KEY);
        String type_tag = tags.get(RelationTypeTag.KEY);
        if (((street_tag != null) && street_tag.toLowerCase().contains("strasse") && !street_tag.toLowerCase().contains("strasser"))
                || ((name_tag != null) && name_tag.toLowerCase().contains("strasse") && !street_tag.toLowerCase().contains("strasser"))){
            return true;
        }

        // Include deprecated tagging

        return false;
    }

    private boolean flagCheLie(final AtlasObject object) {
        final Map<String, String> tags = object.getTags();

        String street_tag = tags.get(AddressStreetTag.KEY);
        String name_tag = tags.get(NameTag.KEY);

        if (((street_tag != null) && street_tag.toLowerCase().contains("stra\u00dfe"))
                || ((name_tag != null) && name_tag.toLowerCase().contains("stra\u00dfe"))){
            return true;
        }
        return false;
    }
}

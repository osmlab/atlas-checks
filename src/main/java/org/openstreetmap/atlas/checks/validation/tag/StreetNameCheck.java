package org.openstreetmap.atlas.checks.validation.tag;

import java.util.*;
import java.util.Arrays;
import java.lang.System;

import javassist.bytecode.stackmap.TypeTag;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
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
    private static final List<String> STREET_NAME_COUNTRIES_DEFAULT = Arrays.asList("DEU", "AUT", "SWE", "LIE");
    private final List<String> checkCountries;

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

        this.checkCountries = (List<String>) this.configurationValue(configuration, "countries", STREET_NAME_COUNTRIES_DEFAULT);
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
        return (this.checkCountries.contains(object.tag(ISOCountryTag.KEY).toUpperCase()) && (object instanceof
                Node || object instanceof Edge || object instanceof Relation));
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

        final Map<String, String> tags = object.getTags();
        // Flagging Germany: flags "ss" and deprecated tagging
        if ((tags.get(ISOCountryTag.KEY).equals("DEU")) || (tags.get(ISOCountryTag.KEY).equals("AUT"))){
            System.out.println("in the german if statement");
            String street_tag = tags.get(AddressStreetTag.KEY);
            String name_tag = tags.get(NameTag.KEY);
            String type_tag = tags.get(RelationTypeTag.KEY);
            if (((object instanceof Node) || (object instanceof Relation)) && (street_tag != null) && street_tag.toLowerCase().contains("strasse") && !street_tag.toLowerCase().contains("strasser")){
                //Flag these items
                System.out.println("DEU or AUT street tags");
            }
            if((object instanceof Edge) && (name_tag != null) && name_tag.toLowerCase().contains("strasse") && !street_tag.toLowerCase().contains("strasser")){
                //Flag these items
                System.out.println("DEU or AUT name tags");
            }
            if((tags.get(ISOCountryTag.KEY) == "DEU") && (type_tag != null) && type_tag.toLowerCase().contains("associatedstreet")){
                //Flag these items
                System.out.println("German depreciated tagging");
            }
        }

        if ((tags.get(ISOCountryTag.KEY).equals("LIE")) || tags.get(ISOCountryTag.KEY).equals("CHE")){
            String street_tag = tags.get(AddressStreetTag.KEY);
            String name_tag = tags.get(NameTag.KEY);
            if (((object instanceof Node) || (object instanceof Relation)) && (street_tag != null) && street_tag.toLowerCase().contains("stra\u00dfe")){
                //Flag these items
                System.out.println("LIE or CHE street tags");
            }
            if((object instanceof Edge) && (name_tag != null) && name_tag.toLowerCase().contains("stra\u00dfe")){
                //Flag these items
                System.out.println(tags);
                System.out.println("LIE or CHE name tags");
            }
        }

        return Optional.empty();
    }

//    @Override
//    protected List<String> getFallbackInstructions()
//    {
//        return FALLBACK_INSTRUCTIONS;
//    }
}

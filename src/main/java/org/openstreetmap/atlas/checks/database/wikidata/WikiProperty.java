// License: GPL. For details, see LICENSE file.
package org.openstreetmap.atlas.checks.database.wikidata;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Known Wiki Properties for OSM Wiki Data
 *
 * @author Taylor Smock
 */
public enum WikiProperty implements WikiItemInterface
{
    ID,
    INSTANCE_OF_P2,
    SUBCLASS_OF_P3,
    @Deprecated
    IMAGE_P4,
    STATUS_P6,
    WIKIDATA_EQUIVALENT_P7,
    FORMATTER_URL_P8,
    KEY_TYPE_P9,
    KEY_FOR_THIS_TAG_P10,
    PROPOSAL_DISCUSSION_P11,
    WIKIDATA_CONCEPT_P12,
    VALUE_VALIDATION_REGEX_P13,
    PERMANENT_KEY_ID_P16,
    REDIRECT_TO_P17,
    DIFFERENT_FROM_P18,
    PERMANENT_TAG_ID_P19,
    PROPERTY_DIFFERENT_FROM_P20,
    RELATION_ROLE_ID_P21,
    REQUIRED_KEY_OR_TAG_P22,
    GROUP_P25,
    LIMITED_TO_LANGUAGE_P26,
    @Deprecated
    EXCLUDING_REGION_QUALIFIER_P27,
    IMAGE_P28,
    MUST_ONLY_BE_USED_IN_REGION_P29,
    NOT_TO_BE_USED_IN_REGION_P30,
    DOCUMENTATION_WIKI_PAGES_P31,
    LANGUAGE_CODE_P32,
    USE_ON_NODES_P33,
    USE_ON_WAYS_P34,
    USE_ON_AREAS_P35,
    USE_ON_RELATIONS_P36,
    USE_ON_CHANGESETS_P37,
    @Deprecated
    RENDERING_IMAGE_P38,
    OSM_CARTO_IMAGE_P39,
    TAG_FOR_THIS_RELATION_TYPE_P40,
    PERMANENT_RELATION_TYPE_ID_P41,
    BELONGS_TO_RELATION_TYPE_P43,
    INCOMPATIBLE_WITH_P44,
    IMPLIES_P45,
    COMBINATION_P46,
    IMAGE_CAPTION_P47,
    LIMITED_TO_REGION_P48,
    GEOGRAPHIC_CODE_P49,
    REDIRECTS_TO_WIKI_PAGE_P50,
    IDENTICAL_TO_P51;

    private final String pid;
    private final String name;

    WikiProperty()
    {
        final String[] enumName = this.name().split("_", -1);
        this.pid = enumName.length >= 2 ? enumName[enumName.length - 1] : "";
        this.name = this.name().replace("_" + pid, "").replace("_", " ");
    }

    /**
     * Get the value from a map where the enum is a key. This is short for
     * {@code map.get(enum.name())}
     *
     * @param <T>
     *            The return type of the map
     * @param map
     *            The map to get the enum from (should not be null)
     * @return The value in the map
     */
    @Nullable
    public <T> T get(@Nonnull final Map<String, T> map)
    {
        final var defaultReturn = map.get(this.getId());
        if (defaultReturn != null)
        {
            return defaultReturn;
        }
        return map.get(this.getDescriptor());
    }

    @Override
    public String getDescriptor()
    {
        return this.name;
    }

    @Override
    public String getId()
    {
        return this.pid;
    }
}

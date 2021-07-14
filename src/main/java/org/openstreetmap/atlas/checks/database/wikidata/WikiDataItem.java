// License: GPL. For details, see LICENSE file.
package org.openstreetmap.atlas.checks.database.wikidata;

import java.text.MessageFormat;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * WikiDataItem special entries
 *
 * @author Taylor Smock
 */
public enum WikiDataItem implements WikiItemInterface
{
    // OSM Concepts
    ELEMENT_Q9,
    KEY_Q7,
    TAG_Q2,
    /** Status of the key/tag */
    STATUS_Q11,
    /** Allowed on nodes/ways/relations/areas */
    STATUS_Q8010,
    // Statuses (STATUS_Q11)
    DE_FACTO_Q13(STATUS_Q11),
    IN_USE_Q14(STATUS_Q11),
    APPROVED_Q15(STATUS_Q11),
    REJECTED_Q16(STATUS_Q11),
    VOTING_Q17(STATUS_Q11),
    DRAFT_Q18(STATUS_Q11),
    ABANDONED_Q19(STATUS_Q11),
    PROPOSED_Q20(STATUS_Q11),
    OBSOLETE_Q5060(STATUS_Q11),
    DEPRECATED_Q5061(STATUS_Q11),
    DISCARDABLE_Q7550(STATUS_Q11),
    IMPORTED_Q21146(STATUS_Q11),
    // Statuses (STATUS_Q8010)
    IS_ALLOWED_Q8000(STATUS_Q8010),
    IS_PROHIBITED_Q8001(STATUS_Q8010),
    // Special
    WELL_KNOWN_VALUES_Q8,
    GROUP_Q12,
    OSM_CONCEPT_Q10,
    SANDBOX_Q2761;

    @Nullable
    private final WikiDataItem parent;
    @Nonnull
    private final String pid;
    @Nonnull
    private final String rName;

    /**
     * Get a well-known wiki data item from a value
     *
     * @param value
     *            The value to look for
     * @return A WikiDataItem or {@code null}
     */
    public static WikiDataItem fromValue(final String value)
    {
        WikiDataItem rItem = Stream.of(WikiDataItem.values())
                .filter(entry -> entry.getId().equalsIgnoreCase(value)).findFirst().orElse(null);
        if (rItem == null)
        {
            rItem = Stream.of(WikiDataItem.values())
                    .filter(p -> p.getDescriptor().equalsIgnoreCase(value)).findFirst()
                    .orElse(null);
        }
        return rItem;
    }

    WikiDataItem()
    {
        this(null);
    }

    WikiDataItem(final WikiDataItem parent)
    {
        this.parent = parent;
        final String[] enumName = this.name().split("_", -1);
        this.pid = enumName[enumName.length - 1];
        this.rName = this.name().replace("_" + pid, "").replace("_", " ");
    }

    @Override
    public String getDescriptor()
    {
        return this.rName;
    }

    @Override
    public String getId()
    {
        return this.pid;
    }

    /**
     * Get the parent WikiDataItem for this, if any
     *
     * @return The parent item or {@code null}
     */
    @Nullable
    public WikiDataItem getParent()
    {
        return this.parent;
    }

    /**
     * Check if the object matches this (essentially "equals")
     *
     * @param other
     *            The object to check
     * @return {@code true} if the object is the same enum OR it is a string that matches the
     *         description (ignoring case).
     */
    public boolean matches(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other instanceof String)
        {
            return this.rName.equalsIgnoreCase((String) other)
                    || this.pid.equalsIgnoreCase((String) other);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("{0}: {1}", this.pid, this.rName);
    }

}

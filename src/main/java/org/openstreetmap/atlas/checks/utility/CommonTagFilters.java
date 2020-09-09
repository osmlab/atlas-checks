package org.openstreetmap.atlas.checks.utility;

/**
 * Hold common tag filters (should be used in more than one check)
 * 
 * @author Taylor Smock
 */
public final class CommonTagFilters
{
    private CommonTagFilters()
    {
        // Hide constructor
    }

    /** Boundary filter for ocean boundaries */
    public static final String DEFAULT_OCEAN_BOUNDARY_TAGS = "natural->coastline";
    /** Tag filter for oceans (without coastline) */
    public static final String DEFAULT_VALID_OCEAN_TAGS = "natural->strait,channel,fjord,sound,bay|"
            + "harbour->*&harbour->!no|estuary->*&estuary->!no|bay->*&bay->!no|place->sea|seamark:type->harbour,harbour_basin,sea_area|water->bay,cove,harbour|waterway->artificial,dock";
}

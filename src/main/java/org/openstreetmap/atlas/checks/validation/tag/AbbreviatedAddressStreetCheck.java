package org.openstreetmap.atlas.checks.validation.tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vladimir Lemberg
 */

public class AbbreviatedAddressStreetCheck extends BaseCheck<String>
{
    private static final String INSTRUCTION_FORMAT = "OSM entity {0,number,#} has address {1} with abbreviated road type \"{2}\". According to conventions, it should be changed to \"{3}\".";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(INSTRUCTION_FORMAT);
    private static final String DEFAULT_ABBREVIATION_RESOURCE = "StreetName.txt";
    private static final Logger logger = LoggerFactory
            .getLogger(AbbreviatedAddressStreetCheck.class);
    private Map<String, List<String>> roadTypeAbbreviationsMap;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public AbbreviatedAddressStreetCheck(final Configuration configuration)
    {
        super(configuration);
        this.roadTypeAbbreviationsMap = this.parseAddressConventionConfig();
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof AtlasEntity
                && !this.isFlagged(this.getUniqueOSMIdentifier(object));
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        this.markAsFlagged(this.getUniqueOSMIdentifier(object));

        final String addressStreet = object.getTag(AddressStreetTag.KEY).orElse(null);
        if (addressStreet != null)
        {
            final String[] splitStreetName = addressStreet.split("\\s+");
            final Pair<String, Integer> roadTypeWithIndex = this
                    .getRoadTypeWithPositionalIndex(splitStreetName);
            final String roadType = roadTypeWithIndex.getLeft();
            final Integer roadTypeIndex = roadTypeWithIndex.getRight();

            for (final Map.Entry<String, List<String>> listEntry : this.roadTypeAbbreviationsMap
                    .entrySet())
            {
                for (final String abbreviation : listEntry.getValue())
                {
                    if (roadType.matches(abbreviation + "\\.?$"))
                    {
                        return Optional.of(this
                                .createFlag(object,
                                        this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                                                addressStreet, roadType, listEntry.getKey()))
                                .addFixSuggestion(FeatureChange.add(
                                        (AtlasEntity) ((CompleteEntity) CompleteEntity
                                                .from((AtlasEntity) object))
                                                        .withTags(object.getTags())
                                                        .withReplacedTag(AddressStreetTag.KEY,
                                                                AddressStreetTag.KEY,
                                                                this.updateStreetAddress(
                                                                        splitStreetName,
                                                                        roadTypeIndex,
                                                                        listEntry.getKey())),
                                        object.getAtlas())));
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * create a {@link Pair} of assuming Road Type (pair.left) with its index (pair.right)
     *
     * @param splitStreetName
     *            {@link String[]}
     * @return
     */
    private Pair<String, Integer> getRoadTypeWithPositionalIndex(final String[] splitStreetName)
    {
        return this.isAddressStreetContainDirectional(splitStreetName[splitStreetName.length - 1])
                ? Pair.of(splitStreetName[splitStreetName.length - 2], splitStreetName.length - 2)
                : Pair.of(splitStreetName[splitStreetName.length - 1], splitStreetName.length - 1);
    }

    /**
     * Checks if the given street address token {@link String} is directional
     *
     * @param streetAddressToken
     *            {@link String}
     * @return boolean
     */
    private boolean isAddressStreetContainDirectional(final String streetAddressToken)
    {
        return streetAddressToken.matches("^(N|S|E|W|NE|SE|SW|NW)\\.?$");
    }

    /**
     * Parse Create K,V {@link Map} key -> full street abbreviation, value -> abbreviation
     * variations
     *
     * @return {@link Map} of Street Address abbreviations.
     */
    private Map<String, List<String>> parseAddressConventionConfig()
    {
        final Map<String, List<String>> roadTypeAbbreviationsMap = new HashMap<>();
        final BufferedReader reader;
        try
        {
            reader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(AbbreviatedAddressStreetCheck.class
                            .getResourceAsStream(DEFAULT_ABBREVIATION_RESOURCE))));
            String line = reader.readLine();
            while (line != null)
            {
                if (line.startsWith("#"))
                {
                    line = reader.readLine();
                    continue;
                }
                else
                {
                    final String roadType = line.split(":")[0];
                    final String roadTypeAbbreviations = line.split(":")[1];
                    final String[] abbreviationsVariations = roadTypeAbbreviations.split("\\|");
                    final List<String> temp = new ArrayList<>(
                            Arrays.asList(abbreviationsVariations));
                    roadTypeAbbreviationsMap.put(roadType, temp);
                }
                line = reader.readLine();
            }
            reader.close();
        }
        catch (final IOException exception)
        {
            logger.error(String.format("Could not read %s", DEFAULT_ABBREVIATION_RESOURCE),
                    exception);
        }
        return roadTypeAbbreviationsMap;
    }

    private String updateStreetAddress(final String[] streetAddress, final Integer roadTypeIndex,
            final String correctRoadType)
    {
        streetAddress[roadTypeIndex] = correctRoadType;
        return String.join(" ", streetAddress);
    }
}

# Mixed Case Name Check

This check flags objects with name tags that improperly use mixed cases.

Proper case use is defined by set standards and configurable exceptions. 

The standards are as follows:

* Words must start with a capital unless:
    * There are no other words in the name
* All other letters must be lower case unless: 
    * They follow an apostrophe and are not the last letter of the word
    * The entire word is uppercase

The standards are broken by the following configurable exceptions (with default values):

* Words that do not need to start with a capital:
    * and
    * to
    * of
    * the
* Symbols that may be followed by a capital:
    * \-
    * /
    * (
    * &
* Name affixes that may be followed by a capital:
    * Mc
    * Mac
    * Mck
    * Mhic
    * Mic
    
These configurables allow this check to be adapted to test different languages.  
OSM uses the `name` tag for the name in a locations primary language, and `name:[ISOcode]` for other languages.
This check uses two configurable to control what languages are checked.

The first is a list of ISO codes for countries that should have there `name` tag checked. It has default values of:

* AIA
* ATG
* AUT
* BHS
* BRB
* BLZ
* BMU
* BWA
* VGB
* CMR
* CAN
* CYM
* DMA
* FJI
* GMB
* GHA
* GIB
* GRD
* GUY
* IRL
* JAM
* KEN
* LSO
* MWI
* MLT
* MUS
* MSR
* NAM
* NZL
* NGA
* PNG
* SYC
* SLE
* SGP
* SLB
* ZAF
* SWZ
* TZA
* TON
* TTO
* TCA
* UGA
* GBR
* USA
* VUT
* ZMB
* ZWE

The second is a list of `name:[ISOcode]` tags to check. Default values are:

* name:en

#### Live Examples

1. Way [id:4780932622](https://www.openstreetmap.org/node/4780932622) has the name `NZ Convenience store`. It is flagged because the S in store should be capitalised. 

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; in our case, we’re are looking at
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java),
[Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java),
[Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java),
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java), and
[Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy the following conditions:

* It is an Edge, Line, Node, Point, or Area
* It is a country where the `name` tag should be checked and it has a `name` tag or it has a one of the `name:[ISOcode]` tags.
* It has not already been flagged

```java
@Override
public boolean validCheckForObject(final AtlasObject object)
{
    return !(object instanceof Relation) && !this.isFlagged(object.getOsmIdentifier())
            && ((object.getTags().containsKey(ISOCountryTag.KEY)
                    && checkNameCountries.contains(object.tag(ISOCountryTag.KEY).toUpperCase())
                    && Validators.hasValuesFor(object, NameTag.class))
                    || languageNameTags.stream()
                            .anyMatch(key -> object.getOsmTags().containsKey(key)));
}
```

Next the objects have each of their name tags, that are being checked, tested for proper use of case.  
Each improper tag is noted in the output.

```java
@Override
protected Optional<CheckFlag> flag(final AtlasObject object)
{
    final List<String> mixedCaseNameTags = new ArrayList<>();
    final Map<String, String> osmTags = object.getOsmTags();

    // Check ISO against list of countries for testing name tag
    if (checkNameCountries.contains(object.tag(ISOCountryTag.KEY).toUpperCase())
            && Validators.hasValuesFor(object, NameTag.class)
            && isMixedCase(osmTags.get(NameTag.KEY)))
    {
        mixedCaseNameTags.add("name");
    }
    // Check all language name tags
    for (final String key : languageNameTags)
    {
        if (osmTags.containsKey(key) && isMixedCase(osmTags.get(key)))
        {
            mixedCaseNameTags.add(key);
        }

    }

    // If mix case id detected, flag
    if (!mixedCaseNameTags.isEmpty())
    {
        this.markAsFlagged(object.getOsmIdentifier());
        final String osmType;
        // Get OSM type for object
        if (object instanceof LocationItem)
        {
            osmType = "Node";
        }
        else
        {
            osmType = "Way";
        }
        // Instruction includes type of OSM object and list of flagged tags
        return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0, osmType,
                object.getOsmIdentifier(), String.join(", ", mixedCaseNameTags))));
    }
    return Optional.empty();
}
```

The testing of the name values is performed by the following. It splits each name into words based on spaces and tests each. 
It returns true when improper use of case is found.

```java
private boolean isMixedCase(final String value)
{
    // Split into words based on spaces
    final String[] wordArray = value.split(" ");
    // Check each word
    for (final String word : wordArray)
    {
        // If there is more than 1 word and the word is not in the lower case list: check that
        // the first letter is a capital
        if (wordArray.length > 1 && !lowerCaseWords.contains(word))
        {
            final Matcher firstLetterMatcher = Pattern.compile("\\p{L}").matcher(word);
            if (firstLetterMatcher.find()
                    && Character.isLowerCase(firstLetterMatcher.group().charAt(0)))
            {
                return true;
            }
        }
        // If the word is not all upper case: check if all the letters not following config
        // specified characters and strings, and apostrophes, are lower case
        if (Pattern.compile("[\\p{L}&&[^\\p{Lu}]]").matcher(word).find()
                && Pattern
                        .compile(String.format(
                                "(\\p{L}.*(?<![\\Q%1$s\\E']|%2$s)(\\p{Lu}))|(\\p{L}.*(?<=')\\p{Lu}(?!.))",
                                this.specialCharacters, this.nameAffixes))
                        .matcher(word).find())
        {
            return true;
        }
    }
    return false;
}
```

To learn more about the code, please look at the comments in the source code for the check.  
[MixedCaseNameCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/MixedCaseNameCheck.java)
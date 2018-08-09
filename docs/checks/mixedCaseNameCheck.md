# Mixed Case Name Check

This check flags objects with name tags that improperly use mixed cases.

Proper case use is defined by set standards and configurable exceptions. 

The standards are as follows:

* Words must start with a capital unless:
    * The first letter is preceded by a number (ex. 20th)
    * All the words in the name are lower case (ex. ferry dock)
* All other letters must be lower case unless: 
    * They follow an apostrophe (ex. O'Flin) and, they are not the last letter of the word (ex. Smith's not Smith'S)
    * The entire word is uppercase, except the last letter if it follows or is followed by an apostrophe (ex. MAX'S or MAX's)

The standards are broken by the following configurable exceptions (with default values):

* Articles that are capitalised only if they are the first word:
    * a, an, the
* Prepositions that do not need to start with a capital:
    * and, from, to, of, by, upon, on, off, at, as, into, like, near, onto, per, till, up, via, with, for, in
* Name affixes that may be followed by a capital:
    * Mc, Mac, Mck, Mhic, Mic
* Mixed case units of measurement that are valid after a number:
    * kV
    
These configurables allow this check to be adapted to test different languages.  
OSM uses the `name` tag for the name in a locations primary language, and `name:[ISOcode]` for other languages.
This check uses two configurable to control what languages are checked.

The first is a list of ISO codes for countries that should have there `name` tag checked. It has default values of:

* AIA, ATG, AUS, BHS, BRB, BLZ, BMU, BWA, VGB, CMR, CAN, CYM, DMA, FJI, GMB, GHA, GIB, GRD, GUY, IRL, JAM, KEN, LSO, MWI, MLT, MUS, MSR, NAM, NZL, NGA, PNG, SYC, SLE, SGP, SLB, ZAF, SWZ, TZA, TON, TTO, TCA, UGA, GBR, USA, VUT, ZMB, ZWE

The second is a list of `name:[ISOcode]` tags to check. Default values are:

* name:en

A final configurable is a list of characters that names are split by to for words. Its default values are: 

* SPACE, \-, /, &, @, –

#### Live Examples

1. Way [id:4780932622](https://www.openstreetmap.org/node/4780932622) has the name `NZ Convenience store`. It is flagged because the S in store should be capitalized. 

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; in our case, we’re are looking at
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java),
[Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java),
[Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java),
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java), and
[Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy the following conditions (see `validCheckForObject` method):

* It is an Edge, Line, Node, Point, or Area
* It is a country where the `name` tag should be checked and it has a `name` tag or it has a one of the `name:[ISOcode]` tags.
* It has not already been flagged

Next the objects have each of their name tags, that are being checked, tested for proper use of case.  
If the object's ISO code is in checkNameCountries its `name` tag is checked, else only the tags in `languageNameTags` are checked.

The test for proper use of case uses multiple regular expressions to check both the entire name and each word.  
The most complex expression checks that all letters are lowercase, with the exception of the first letter and letters following apostrophes at the end of the word.

```java
return Pattern.compile(String.format(
    "(\\p{L}.*(?<!'|%1$s)(\\p{Lu}))|(\\p{L}.*(?<=')\\p{Lu}(?!.))", this.nameAffixes))
    .matcher(word).find();
```

To learn more about the code, please look at the comments in the source code for the check.  
[MixedCaseNameCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/MixedCaseNameCheck.java)
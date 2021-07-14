# Generic Tag Check

This check uses TagInfo and WikiData databases to look for invalid, deprecated, or otherwise invalid tags.
In addition, there is a fallback check that uses TagInfo data and WikiData items to determine if a tag is valid.
The fallback check can be enabled or disabled by setting `"fallback"` to `false`. The location of the databases are
set in a `"db"` section with either `"wikidata"` or `"taginfo"` as the key, and then the path as the value. This
_must_ be changed in Microsoft Windows environments (due to differences in file path separators). There is an additional
key for the `wikidata` database, `"wikidata.tag_removal"` in main GenericTagCheck check configuration section. This
controls the detection of tags that can be removed. By default, tags that have been marked with "abandoned",
"deprecated", "imported", "obsolete", or "rejected" are removable.

## First steps
You need to get a TagInfo db and a WikiData db. To that end, use the following scripts:
* [scripts/taginfo/downloadTagInfo.py](../../scripts/taginfo/downloadTagInfo.py)
* [scripts/wikidata/get\_wikidata.py](../../scripts/wikidata/get_wikidata.py)

The Tag Info script drops some tables and columns by default, to save space and transfer costs.

The Wiki Data script will write to a `wikidata.db` file in the current directory. If one exists, the database will be
updated with new entries first, and then old entries will be updated. There is an output of what changed, so QA/QC can
be performed on changes, if desired.

## Available configuration variables
Example json:
```json
  "GenericTagCheck": {
    "db": {
      "taginfo": "extra/taginfo-db.db",
      "wikidata": "extra/wikidata.db"
    },
    "tag": {
      "usage.min": 100,
      "percentage_of_key_for_popular": 10
    },
    "wikidata.tag_removal": ["abandoned", "deprecated", "imported", "obsolete", "rejected"],
    "challenge": {
      "description": "Tasks containing features with tags containing missing, conflicting, incorrect or illegal values",
      "blurb": "Features with invalid tags",
      "instruction": "Open your favorite editor and check that the listed tags are correct.",
      "difficulty": "Medium",
      "tags":"tags"
    }
  }
```

### `db.{taginfo,wikidata}`
This variable controls where the check looks for the TagInfo and WikiData databases. This is currently relative to the
`atlas` file directory.

### `tag.usage.min`
This tag controls how many usages are required before we recommend adding the item to the Wiki Data database and the
OSM Wiki. Anything below this value, if not in the wiki data database, will be recommended for removal.

### `tag.percentage_of_key_for_popular`
This controls the recommendation for whether or not a key-value combination should be added to wiki data, if the key
is a "Well-Defined" key. This also affects the removal recommendations.

### `wikidata.tag_removal`
This controls what types of key/tags should be recommend for removal.
Possible values (they have the type "status" or [Q11](https://wiki.openstreetmap.org/wiki/Item:Q11))
* [`de facto` (Q13)](https://wiki.openstreetmap.org/wiki/Item:Q13) -- not recommended for use here
* [`in use` (Q14)](https://wiki.openstreetmap.org/wiki/Item:Q14) -- not recommended for use here
* [`approved` (Q15)](https://wiki.openstreetmap.org/wiki/Item:Q15) -- not recommended for use here
* [`rejected` (Q16)](https://wiki.openstreetmap.org/wiki/Item:Q16) -- default
* [`voting` (Q17)](https://wiki.openstreetmap.org/wiki/Item:Q17) -- not recommended for use here
* [`draft` (Q18)](https://wiki.openstreetmap.org/wiki/Item:Q18) -- not recommended for use here
* [`abandoned` (Q19)](https://wiki.openstreetmap.org/wiki/Item:Q19) -- default
* [`proposed` (Q20)](https://wiki.openstreetmap.org/wiki/Item:Q20) -- not recommended for use here
* [`obsolete` (Q5060)](https://wiki.openstreetmap.org/wiki/Item:Q5060) -- default
* [`deprecated` (Q5061)](https://wiki.openstreetmap.org/wiki/Item:Q5061) -- default
* [`discardable` (Q7550)](https://wiki.openstreetmap.org/wiki/Item:Q7550) -- this is typically removed when editors
  touch an object, so these tags will naturally disappear over time.
* [`imported` (Q21146)](https://wiki.openstreetmap.org/wiki/Item:Q21146) -- default


## How to update an entry in OpenStreetMap Wiki Data
1. Log in to the OSM Wiki
2. Navigate to the appropriate page ( https://wiki.openstreetmap.org/wiki/Item:Q198 )
3. Click on "edit" to the right of use on nodes
is prohibited
4. Type in is allowed -- make certain you click on the dropdown
5. Click on save

## How to add a new entry in OpenStreetMap Wiki Data
1. Install [JOSM](https://josm.openstreetmap.de)
2. Install the [WikiData](https://gitlab.com/gokaart/josm_wikidata) plugin ([installation instructions](https://josm.openstreetmap.de/wiki/Help/Preferences/Plugins))
3. Click on `Create new WikiData item for ...`
4. Fill out the information
5. Click OK

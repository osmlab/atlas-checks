# ConstructionCheck

#### Description

The purpose of this check is to identify construction tags where the construction hasn't been checked on recently, or 
the expected finish date has been passed.

#### Live Examples

Out of date constructions
1. The way [id:511874260](https://www.openstreetmap.org/way/511874260) has had the construction tag for more than 2 years.
2. The way [id:722090480](https://www.openstreetmap.org/way/722090480) opening_date tag has been passed.

#### Code Review

This check evaluates all the types of Atlas objects: 
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java),
[Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java),
[Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java),
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java),
[Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java), and
[Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).

##### Validating the Object
We first validate that the incoming object is: 
* not already flagged
* has a valid construction tag

##### Flagging the Object
There are 3 ways we check if the object should be flagged:
1. We get one of the valid date tags (see DATE_TAGS in ConstructionCheck.java)
    * If one is present we attempt to parse the date string and compare it to today's date.
    * If it is before today's date we flag the object.
2. We check the "check_date" tag
    * If present we parse the date string and find how many months it is between the check_date and today's date
    * If it is more than the oldCheckDateMonths, as defined in the config, we flag the object
3. If all else fails we check the tag "last_edit_time"
    * We convert the timestamp to a date and find the days between that and today's date
    * If it is more than the oldConstructionDays, as defined in the config, we flag the object
    
##### Parsing the date string
While there is one specified way people should be tagging dates, ISO 8601 (yyyy-mm-dd, ie. 2020-01-16), but it is not always
in this format, we attempt to account for that by parsing as many date formats as we come across.
For dates that are missing either the day or day and month we assume the latest date that could be made with the
available information. A date string of "2020" would result in a date of 2020-12-31. If the construction was to be 
completed in 2020, then the best we can assume is that it will be done by the time 2020 is over so create a date that 
will only get flagged after 2020 is over. Same with a date string like "2020-1" we would parse this into 2020-01-31,
so that the construction would then be flagged once January is over.

To learn more about the code, please look at the comments in the source code for the check.  
[ConstructionCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/ConstructionCheck.java)

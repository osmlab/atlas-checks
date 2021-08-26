# InvalidTurnRestrictionCheck

#### Description

This check flags Relations with Turn restrictions, that cannot be processed. In code TrunRestriction.from() encounters exception attempting to build TurnRestriction from such a Relation. On the map such a Relation is simply incorrect in one way or another. 

#### Live Example

1) This relation [id:7070408] https://www.openstreetmap.org/relation/7070408 is having wrong number of members: via is missing.
2) This relation [id:3658651] https://www.openstreetmap.org/relation/3658651 is having bad member type: FROM has type "link" and is tagged as "disused"
3) This relation [id:8221967] https://www.openstreetmap.org/relation/8221967 is unconnected
4) This relation [id:5451368] https://www.openstreetmap.org/relation/5451368 doesn't match topology


#### Code Review

To learn more about the code, please look at the comments in the source code for the check.
[InvalidTurnRestrictionCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relations/InvalidTurnRestrictionCheck.java)

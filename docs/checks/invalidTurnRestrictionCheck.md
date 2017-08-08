# InvalidTurnRestrictionCheck

#### Description

This check flags Relations with Turn restrictions, that cannot be processed. In code TrunRestriction.from() encounters exception attempting to build TurnRestriction from such a Relation. On the map such a Relation is simply incorrect in one way or another. 
#### Code Review

To learn more about the code, please look at the comments in the source code for the check.
[InvalidTurnRestrictionCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relations/InvalidTurnRestrictionCheck.java)

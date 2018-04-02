# One Member Relation Check

#### Description

This check identifies relations that only contain one member. In OSM, a [Relation](https://wiki.openstreetmap.org/wiki/Elements#Relation)
is a multi-purpose data structure that documents a relationship between two or more data elements 
(nodes, ways, and/or other relations).

#### Live Example
The following examples illustrate two cases where a Relation contains only one member.

//// TO DO - add examples ////

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).

Our first goal is to validate the incoming Atlas Object. We use some preliminary filtering to target
Relation objects. Therefore, we use:
* Must be a Relation

```java
    @Override
        public boolean validCheckForObject(final AtlasObject object) {
            return object instanceof Relation;
        }
```

After the preliminary filtering of features, we get the members of that relation, get the member
entity type, and then collect them into a Set. Using this Set, we can check how many members it contains.
If the Set is of size = 1, then we flag this relation.


```java
    @Override
        protected Optional<CheckFlag> flag(final AtlasObject object) {
            Relation relation = (Relation) object;
            Set<AtlasObject> members = relation.members().stream()
                    .map(RelationMember::getEntity).collect(Collectors.toSet());
            if (members.size() == 1) {
                return Optional.of(createFlag(members,
                        this.getLocalizedInstruction(0, relation.getOsmIdentifier())));
            }
            return Optional.empty();
        }
```


To learn more about the code, please look at the comments in the source code for the check.
[OneMemberRelationCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relations/OneMemberRelationCheck.java)
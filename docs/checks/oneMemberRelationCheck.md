# One Member Relation Check

#### Description

This check identifies relations that only contain one member. In OSM, a [Relation](https://wiki.openstreetmap.org/wiki/Elements#Relation)
is a multi-purpose data structure that documents a relationship between two or more data elements 
(nodes, ways, and/or other relations).

#### Live Example
The following examples illustrate two cases where a Relation contains only one member.
1) This relation [id:4646212](https://www.openstreetmap.org/relation/4646212) has only one member.
2) This relation [id:7691824](https://www.openstreetmap.org/relation/7691824) has only one member.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).

Our first goal is to validate the incoming Atlas Object. We use some preliminary filtering to target
Relation objects. Therefore, we use:
* Must be a Relation
* Must not have been flagged before

```java
    @Override
        public boolean validCheckForObject(final AtlasObject object)
        {
            return object instanceof Relation;
        }
```

After the preliminary filtering of features, we get the members of that relation. If the number of
members in the relation is 1, then we flag the relation as problematic. One thing to note is that 
multi-polygon relations with one member are flagged with a more specific instruction than the
regular one member relation.

```java
      @Override
          protected Optional<CheckFlag> flag(final AtlasObject object)
          {
              final Relation relation = (Relation) object;
              final RelationMemberList members = relation.members();
      
              // If the number of members in the relation is 1
              if (members.size() == 1)
              {
                  // If the relation is a multi-polygon,
                  if (relation.isMultiPolygon()) {
                      return Optional.of(createFlag(
                              relation.members().stream().map(RelationMember::getEntity)
                                      .collect(Collectors.toSet()),
                              this.getLocalizedInstruction(1, relation.getOsmIdentifier())));
                  }
                  return Optional.of(createFlag(
                          relation.members().stream().map(RelationMember::getEntity)
                                  .collect(Collectors.toSet()),
                          this.getLocalizedInstruction(0, relation.getOsmIdentifier())));
              }
              return Optional.empty();
          }
```

To learn more about the code, please look at the comments in the source code for the check.
[OneMemberRelationCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relations/OneMemberRelationCheck.java)
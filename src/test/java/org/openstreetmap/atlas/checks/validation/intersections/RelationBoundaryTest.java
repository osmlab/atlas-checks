package org.openstreetmap.atlas.checks.validation.intersections;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author srachanski
 */
class RelationBoundaryTest
{

    private static final String TAG_1 = "tag1";
    private static final String TAG_2 = "tag2";
    private static final String TAG_3 = "tag3";
    private static final Long OSM_ID_1 = 1000L;
    private static final Long OSM_ID_2 = 1000L;
    private static final Long OSM_ID_3 = 1000L;
    private static final Relation RELATION_1 = Mockito.mock(Relation.class);
    private static final Relation RELATION_2 = Mockito.mock(Relation.class);
    private static final Relation RELATION_3 = Mockito.mock(Relation.class);

    RelationBoundary prepareRelationBoundary()
    {
        when(RELATION_1.getOsmIdentifier()).thenReturn(OSM_ID_1);
        when(RELATION_2.getOsmIdentifier()).thenReturn(OSM_ID_2);
        when(RELATION_3.getOsmIdentifier()).thenReturn(OSM_ID_3);
        final Map<String, Relation> map = new HashMap<>();
        map.put(TAG_1, RELATION_1);
        map.put(TAG_2, RELATION_2);
        map.put(TAG_3, RELATION_3);
        return new RelationBoundary(map, null);
    }

    @Test
    void shouldFindGivenId()
    {
        // Given
        final RelationBoundary relationBoundary = this.prepareRelationBoundary();

        // When
        final boolean result = relationBoundary.containsRelationId(OSM_ID_1);

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    void shouldFindRelationsByTags()
    {
        // Given
        final RelationBoundary relationBoundary = this.prepareRelationBoundary();
        final Set<String> tags = new HashSet<>();
        tags.add(TAG_1);
        tags.add(TAG_2);

        // When
        final Set<Relation> result = relationBoundary.getRelationsByBoundaryTags(tags);

        // Then
        Assertions.assertEquals(tags.size(), result.size());
        Assertions.assertTrue(result.contains(RELATION_1));
        Assertions.assertTrue(result.contains(RELATION_2));
    }

}

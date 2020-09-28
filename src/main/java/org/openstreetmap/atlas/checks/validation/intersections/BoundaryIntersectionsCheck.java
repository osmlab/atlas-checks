package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.tags.BoundaryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author srachanski
 */
public class BoundaryIntersectionsCheck extends BaseCheck<Long> {
    
    private static final String INVALID_BOUNDARY_FORMAT = "Boundary {0,number,#} with edge {1} is crossing invalidly with boundary(ies) {3} with edge {2}.";
    private static final String INSTRUCTION_FORMAT = INVALID_BOUNDARY_FORMAT + " Two boundaries should not intersect each other.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT, INVALID_BOUNDARY_FORMAT);
    
    private static final Predicate<Edge> EDGE_AS_BOUNDARY = edge -> edge.relations()
            .stream()
            .anyMatch(BoundaryIntersectionsCheck::isTypeBoundaryWithBoundaryTag);
    public static final String DELIMITER = ", ";
    public static final int INDEX = 0;
    
    public BoundaryIntersectionsCheck(final Configuration configuration) {
        super(configuration);
    }
    
    private static boolean isTypeBoundaryWithBoundaryTag(AtlasObject object) {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY) &&
                Validators.hasValuesFor(object, BoundaryTag.class);
    }
    
    @Override
    public boolean validCheckForObject(final AtlasObject object) {
        return object instanceof Relation &&
                isTypeBoundaryWithBoundaryTag(object);
    }
    
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object) {
        Relation relation = (Relation) object;
        RelationMemberList relationMembersEdges = relation.membersOfType(ItemType.EDGE);
        List<Edge> edges = getEdges(relationMembersEdges);
        CheckFlag checkFlag = prepareCheckFlag(object, relation, edges);
        if (checkFlag.getFlaggedObjects().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(checkFlag);
    }
    
    private List<Edge> getEdges(RelationMemberList relationMembersEdges) {
        return relationMembersEdges
                .stream()
                .map(this::castToEdge)
                .collect(Collectors.toList());
    }
    
    private Edge castToEdge(RelationMember relationMember) {
        return (Edge) relationMember.getEntity();
    }
    
    private CheckFlag prepareCheckFlag(AtlasObject object, Relation relation, List<Edge> edges) {
        CheckFlag checkFlag = new CheckFlag(this.getTaskIdentifier(object));
        edges.forEach(edge -> analyzeIntersections(object.getAtlas(), relation, checkFlag, edges, edge));
        return checkFlag;
    }
    
    private void analyzeIntersections(Atlas atlas, Relation relation, CheckFlag checkFlag, List<Edge> edges, Edge edge) {
        Set<Edge> knownIntersections = new HashSet<>();
        Iterable<Edge> intersections = atlas.edgesIntersecting(edge.bounds(), getPredicateForEdgesSelection(edges));
        for (Edge currentEdge : intersections) {
            if (!knownIntersections.contains(currentEdge)) {
                Set<Relation> intersectingBoundaries = getBoundary(currentEdge);
                addInformationToFlag(relation, checkFlag, edge, currentEdge, intersectingBoundaries);
                knownIntersections.add(currentEdge);
            }
        }
    }
    
    private void addInformationToFlag(Relation relation, CheckFlag checkFlag, Edge edge, Edge currentEdge, Set<Relation> intersectingBoundaries) {
        checkFlag.addPoints(getIntersectingPoints(edge, currentEdge));
        checkFlag.addObject(currentEdge);
        checkFlag.addObjects(intersectingBoundaries);
        checkFlag.addInstruction(this.getLocalizedInstruction(INDEX,
                relation.getOsmIdentifier(),
                edge.getOsmIdentifier(),
                currentEdge.getOsmIdentifier(),
                asList(intersectingBoundaries)));
    }
    
    private Predicate<Edge> getPredicateForEdgesSelection(List<Edge> edges) {
        return edgeToCheck -> EDGE_AS_BOUNDARY.test(edgeToCheck) && !edges.contains(edgeToCheck);
    }
    
    private String asList(Set<Relation> intersectingBoundaries) {
        return intersectingBoundaries
                .stream()
                .map(relation -> Long.toString(relation.getOsmIdentifier()))
                .collect(Collectors.joining(DELIMITER));
    }
    
    private Set<Relation> getBoundary(Edge currentEdge) {
        return currentEdge.relations()
                .stream()
                .filter(BoundaryIntersectionsCheck::isTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
    }
    
    private Set<Location> getIntersectingPoints(Edge edge, Edge currentEdge) {
        return currentEdge.asPolyLine().intersections(edge.asPolyLine());
    }
    
    @Override
    protected List<String> getFallbackInstructions() {
        return FALLBACK_INSTRUCTIONS;
    }
    
}

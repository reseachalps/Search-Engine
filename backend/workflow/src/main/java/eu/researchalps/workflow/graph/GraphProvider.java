package eu.researchalps.workflow.graph;

import eu.researchalps.db.model.Relation;
import eu.researchalps.db.model.RelationTypeEnum;
import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.full.*;
import eu.researchalps.db.model.full.*;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.workflow.full.FullStructureProvider;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provider to compute the graph of structures attached to this existing structure
 */
@Component
public class GraphProvider implements FullStructureProvider<List<GraphElement>> {
    public static final Set<FullStructureField> FIELDS = Sets.newHashSet(FullStructureField.STRUCTURE, FullStructureField.PROJECTS, FullStructureField.PUBLICATIONS);

    @Autowired
    private StructureRepository structureRepository;

    @Override
    public List<GraphElement> computeField(FullStructure structure) {
        // Map structureId => Graph Element
        final Map<String, GraphElement> graph = new HashMap<>();

        // Projects
        if (structure.getProjects() != null)
            for (FSProject project : structure.getProjects())
                project.getStructures().forEach(s -> addToGraphLink(graph, GraphElementType.PROJECT, s.getId()));

        // Parent and children
        if (structure.getParents() != null)
            structure.getParents().stream().forEach(it -> addToGraphLink(graph, GraphElementType.CHILD, it.getId()));
        if (structure.getChildren() != null)
            structure.getChildren().forEach(s -> addToGraphLink(graph, GraphElementType.PARENT, s.getId()));

        // Publications
        List<FSPublication> publications = structure.getPublications();
        if (publications != null) {
            for (FSPublication publication : publications) {
                GraphElementType graphElementType;
                if (publication == null) {
                    continue;
                }
                if (publication.getType() == null) {
                    graphElementType = GraphElementType.PUBLICATION;
                } else {
                    switch (publication.getType()) {
                        case THESIS:
                            graphElementType = GraphElementType.THESIS;
                            break;
                        case PATENT:
                            graphElementType = GraphElementType.PATENT;
                            break;
                        default:
                            graphElementType = GraphElementType.PUBLICATION;
                    }
                }
                publication.getStructures().forEach(s -> addToGraphLink(graph, graphElementType, s));
            }
        }

        // Spinoff
        if (structure.getStructure().getSpinoffs() != null) {
            // find spinoff in database
            structure.getStructure().getSpinoffs().stream()
                    .filter(s -> s.getIdCompany() != null)
                    .filter(s -> structureRepository.exists(s.getIdCompany()))
                    .forEach(s -> addToGraphLink(graph, GraphElementType.SPINOFF, s.getIdCompany()));
        }

        // Retrieve all structure by relations
        // we only keep the following relationships: ECOLE_DOCTORALE
        if (structure.getStructure().getRelations() != null && structure.getStructure().getRelations().size() > 0) {
            List<String> relationIds = structure.getStructure().getRelations().stream()
                    .filter(r -> RelationTypeEnum.ECOLE_DOCTORALE.equals(r.getType()))
                    .map(Relation::getId).collect(Collectors.toList());
            List<Structure> structureWithSameRelations = structureRepository.findIdsByRelationId(relationIds);
            structureWithSameRelations.forEach(s -> addToGraphLink(graph, GraphElementType.RELATION, s.getId()));
        }

        // now remove itself from the graph :-)
        graph.remove(structure.getId());

        // Package the result
        List<GraphElement> result = new ArrayList<>();
        Map<String, LightStructure> lightStructureMap = structureRepository.findByIdsLight(graph.keySet()).stream().collect(Collectors.toMap(Structure::getId, LightStructure::new));
        for (String key : graph.keySet()) {
            GraphElement graphElement = graph.get(key);
            LightStructure s = lightStructureMap.get(key);
            // structure is absent for some reason, ignore it
            if (s == null) {
                continue;
            }
            graphElement.setStructure(s);
            result.add(graphElement);
        }

        // order the graph (DESC from weight)
        result.sort((s1, s2) -> -Integer.compare(s1.getWeight(), s2.getWeight()));

        return result;
    }

    private void addToGraphLink(Map<String, GraphElement> graph, GraphElementType type, String structureId) {
        graph.putIfAbsent(structureId, new GraphElement());
        graph.get(structureId).addElement(type);
    }

    @Override
    public FullStructureField getField() {
        return FullStructureField.GRAPH;
    }

    @Override
    public Set<FullStructureField> getDependencies() {
        return FIELDS;
    }
}

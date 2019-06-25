package eu.researchalps.workflow.website.entity;

import eu.researchalps.db.model.Project;
import eu.researchalps.db.model.publication.Publication;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NamedEntity {
    public String id;
    public Type type;
    public List<Label> labels;

    public NamedEntity() {
    }

    public NamedEntity(Project p) {
        id = p.getId();
        type = Type.PROJECT;

        //noinspection ArraysAsListWithZeroOrOneArgument
        labels = new ArrayList<>(3);
        labels.add(new Label(id, Method.EXACT));
        if (p.getAcronym() != null) {
            labels.add(new Label(p.getAcronym(), Method.SHORT_LABEL));
        }
        if (p.getLabel() != null) {
            labels.add(new Label(p.getLabel(), Method.LONG_LABEL));
        }
    }

    public NamedEntity(Publication p) {
        id = p.getId();
        type = Type.PUBLICATION;

        labels = new LinkedList<>();
        labels.add(new Label(p.getTitle(), Method.LONG_LABEL));
        if (p.getIdentifiers() != null) {
            labels.addAll(p.getIdentifiers().stream().filter(it -> "PATENT".equals(it.getType())).map(it -> new Label(it.getId(), Method.EXACT)).collect(Collectors.toList()));
        }
    }

    public static class Label {
        public String label;
        public Method method;

        public Label() {
        }

        public Label(String label, Method method) {
            this.label = label;
            this.method = method;
        }
    }

    public enum Method {
        EXACT, SHORT_LABEL, LONG_LABEL
    }

    public enum Type {
        PROJECT, PUBLICATION
    }
}
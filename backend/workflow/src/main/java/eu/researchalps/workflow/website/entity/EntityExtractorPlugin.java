package eu.researchalps.workflow.website.entity;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.util.RepositoryLock;
import eu.researchalps.workflow.translate.TranslateJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by loic on 01/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class EntityExtractorPlugin extends QueueComponent implements QueueListener<EntityExtractorPlugin.Out> {
    public static final int MAX_OCCURENCES = 100;
    public static MessageQueue<Out> QUEUE_OUT = MessageQueue.get("NAMED_ENTITY_DETECTER_OUT", Out.class);
    public static MessageQueue<In> QUEUE = MessageQueue.get("NAMED_ENTITY_DETECTER", In.class);

    @Autowired
    private TranslateJobService translateJobService;

    private RepositoryLock<Website, String, WebsiteRepository> websiteRepository;

    @Autowired
    public void setWebsiteRepository(WebsiteRepository repository) {
        this.websiteRepository = RepositoryLock.get(repository);
    }

    @Override
    public void receive(Out out) {
        Website saved = websiteRepository.update(out.url, tx -> {
            Website w = tx.getNotNull();

            Map<NamedEntity.Type, List<String>> byType = out.entities.stream().collect(Collectors.groupingBy(it -> it.type, Collectors.mapping(it -> it.id, Collectors.toList())));
            w.setExtractedProjects(getExtractedIds(byType, NamedEntity.Type.PROJECT));
            w.setExtractedPublications(getExtractedIds(byType, NamedEntity.Type.PUBLICATION));
            tx.saveDeferred();
        }).getSaved();
        translateJobService.enqueue(saved.getId());
    }

    protected List<String> getExtractedIds(Map<NamedEntity.Type, List<String>> byType, NamedEntity.Type project) {
        List<String> ids = byType.getOrDefault(project, Collections.emptyList());
        if (ids.size() > MAX_OCCURENCES) {
            // Get only the 1000
            return ids.subList(0, MAX_OCCURENCES);
        }
        return ids;
    }

    public void execute(Website w) {
        queueService.push(new In(w.getId()), QUEUE, QUEUE_OUT);
    }

    @Override
    public MessageQueue<Out> getQueue() {
        return QUEUE_OUT;
    }

    public static class In {
        public String url;

        public In(String url) {
            this.url = url;
        }

        public In() {
        }
    }
    public static class Out {
        public String url;
        public List<Entity> entities;
    }

    public static class Entity {
        public NamedEntity.Type type;
        public String id;
    }
}

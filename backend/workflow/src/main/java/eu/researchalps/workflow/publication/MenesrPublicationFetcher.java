package eu.researchalps.workflow.publication;

import com.datapublica.common.http.DPHttpClient;
import eu.researchalps.workflow.structure.menesr.MenesrConfiguration;
import eu.researchalps.workflow.website.publication.dto.ExtractedPublicationsDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by loic on 04/05/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Service
public class MenesrPublicationFetcher {

    @Autowired
    private MenesrConfiguration config;

    @Autowired
    private DPHttpClient client;

    @Autowired
    private PublicationResolverPlugin publicationResolverPlugin;


    public Collection<ExtractedPublicationsDTO> fetch() {
        try {
            return parse(new StringReader(new String(client.execute(config.getPublicationDOIsAction()).getContent(), "UTF-8")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public long execute() {
        return fetch().stream().peek(publicationResolverPlugin::execute).count();
    }

    protected Collection<ExtractedPublicationsDTO> parse(Reader in) throws IOException {
        Map<String, ExtractedPublicationsDTO> result = new HashMap<>();
        JsonNode reader = new ObjectMapper().readTree(in);
        for (JsonNode record : reader) {
            JsonNode fields = record.get("fields");
            String id = fields.get("numero_national_de_structure_de_recherche").asText();
            ExtractedPublicationsDTO dto = result.get(id);
            if (dto == null) {
                dto = new ExtractedPublicationsDTO(id);
                result.put(id, dto);
            }
            String publication = fields.get("publication").asText();
            if (fields.get("type").asText().equals("doi")) {
                dto.dois.add(publication);
            } else {
                dto.references.add(publication);
            }
        }
        return result.values();
    }
}

package eu.researchalps.workflow.oai;

import eu.researchalps.db.model.publication.Publication;

import java.util.Date;

/**
 * Created by loic on 18/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class OAIEntry {
    public String id;
    public Date date;
    public boolean deleted;
    public Publication content;
}

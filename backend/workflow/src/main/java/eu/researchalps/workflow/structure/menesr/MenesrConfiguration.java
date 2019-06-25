package eu.researchalps.workflow.structure.menesr;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Configuration
public class MenesrConfiguration {
    @Value("${menesr.basedir:}")
    private String baseDir;
    @Value("${menesr.username:scanR}")
    private String username;
    @Value("${menesr.password:}")
    private String password;
    @Value("${menesr.publication.dataset:https://data.enseignementsup-recherche.gouv.fr/explore/dataset/rnsr-publications-scientifiques}")
    private String publicationDataset;
    @Value("${menesr.publication.username:scanr}")
    private String publicationUsername;
    @Value("${menesr.publication.password:}")
    private String publicationPassword;

    private String authorization;
    private String odsAuthorization;
    @Value("${menesr.file.rnsr:rnsr.json}")
    private String rnsrFile;
    @Value("${menesr.file.companies:entreprises.json}")
    private String companiesFile;
    @Value("${menesr.file.projects:projets.json}")
    private String projectsFile;
    @Value("${menesr.file.publications:publications.json}")
    private String publicationsFile;

    @PostConstruct
    private void init() throws UnsupportedEncodingException {
        authorization = "Basic "+Base64.encodeBase64String((this.username + ":" + this.password).getBytes("UTF-8"));
        odsAuthorization = "Basic "+Base64.encodeBase64String((this.publicationUsername + ":" + this.publicationPassword).getBytes("UTF-8"));
    }

    public File getRNSRAction() {
        return get(baseDir + rnsrFile);
    }

    public File getCompaniesAction() {
        return get(baseDir + companiesFile);
    }

    public File getProjectsAction() {
        return get(baseDir + projectsFile);
    }

    public File getPublicationAction() {
        return get(baseDir + publicationsFile);
    }

    private HttpGet get(String url, String authorization) {
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, authorization);
        return get;
    }

    private File get(String path) {
        return new File(path);
    }


    public HttpGet getPublicationDOIsAction() {
        return get(publicationDataset+"/download?format=json", odsAuthorization);
    }
}

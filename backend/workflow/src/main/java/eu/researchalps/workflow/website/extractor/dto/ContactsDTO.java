package eu.researchalps.workflow.website.extractor.dto;

import java.util.List;

/**
 * Created by antoine on 25/07/14 25/07/14 for companies-db-root.
 */
public class ContactsDTO {
    public List<Contact> contacts;
    public String companyId;

    public ContactsDTO(List<Contact> c, String companyId) {
        this.contacts = c;
        this.companyId = companyId;
    }

    public ContactsDTO() {}
}
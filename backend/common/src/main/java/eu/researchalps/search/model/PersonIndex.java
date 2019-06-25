package eu.researchalps.search.model;

import eu.researchalps.db.model.Person;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class PersonIndex {
    // FirstName + LastName
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String completeName;

    public PersonIndex(Person leader) {
        this.completeName = (leader.getFirstname() != null ? leader.getFirstname() + " " : "") + leader.getLastname();
    }

    public PersonIndex() {

    }

    public String getCompleteName() {
        return completeName;
    }
}

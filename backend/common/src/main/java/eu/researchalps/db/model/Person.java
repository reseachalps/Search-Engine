package eu.researchalps.db.model;

import java.util.List;

/**
 * Leader (director) of a strcuture
 */
public class Person {
    /**
     * first name of ghe director
     */
    private String firstname;
    /**
     * last name of ghe director
     */
    private String lastname;
    /**
     * title  of ghe director
     */
    private String title;
    /**
     * email of ghe director
     */
    private String email;
    /**
     * type of ghe director
     */
    private LeaderType type;

    /**
     * Source information
     */
    private List<Source> sources;

    public Person() {
    }

    public Person(String firstName, String lastname, String title, String email) {
        this.firstname = firstName;
        this.lastname = lastname;
        this.title = title;
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getTitle() {
        return title;
    }

    public String getEmail() {
        return email;
    }

    public LeaderType getType() {
        return type;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }
}

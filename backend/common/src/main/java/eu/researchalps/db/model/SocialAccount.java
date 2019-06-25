package eu.researchalps.db.model;

/**
 * Social network account.
 */
public class SocialAccount {
    /**
     * url of the account
     */
    private String account;
    /**
     * Matching score
     */
    private Double score;
    /**
     * picture associated with the social network
     */
    private String profilePictureUrl;

    public SocialAccount() {
    }

    public SocialAccount(String account, Double score, String profilePictureUrl) {
        this.account = account;
        this.score = score;
        this.profilePictureUrl = profilePictureUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocialAccount that = (SocialAccount) o;

        if (account != null ? !account.equals(that.account) : that.account != null) return false;

        return true;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    @Override
    public int hashCode() {
        return account != null ? account.hashCode() : 0;
    }

    public static int compareTo(SocialAccount s1, SocialAccount s2) {
        return (s1).score.compareTo((s2).score);
    }
}

package eu.researchalps.search.model.response;

import eu.researchalps.search.model.FullStructureIndex;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * This class is used to add score and highlights to the results
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FullStructureResult extends FullStructureIndex {
    private List<HighlightItem> highlights;
    private Object score;

    public List<HighlightItem> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<HighlightItem> highlights) {
        this.highlights = highlights;
    }

    public Object getScore() {
        return this.score;
    }

    public void setScore(Object score) {
        this.score = score;
    }

    public static class HighlightItem {
        public String type;
        public String value;

        public HighlightItem(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}

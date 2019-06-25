package eu.researchalps.search.model;

import eu.researchalps.db.model.Activity;
import eu.researchalps.db.model.ActivityTypeEnum;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 *
 */
public class ActivityIndex {
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private ActivityTypeEnum type;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String code;
    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text")
    private String label;

    public ActivityIndex(Activity activity) {
        this.type = activity.getActivityType();
        this.code = activity.getCode();
        this.label = activity.getLabel();
    }

    public ActivityIndex() {
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public ActivityTypeEnum getType() {
        return type;
    }
}

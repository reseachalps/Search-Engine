package eu.researchalps.search.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.*;

/**
 * Created by loic on 05/04/2018.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class MultilingualText {
    @Field(type = FieldType.String, analyzer = "french", searchAnalyzer = "french")
    public String fr;
    @Field(type = FieldType.String, analyzer = "english", searchAnalyzer = "english")
    public String en;
    @Field(type = FieldType.String, analyzer = "german", searchAnalyzer = "german")
    public String de;
    @Field(type = FieldType.String, analyzer = "italian", searchAnalyzer = "italian")
    public String it;
    @Field(type = FieldType.String, analyzer = "slovene", searchAnalyzer = "slovene")
    public String sl;
    @Field(type = FieldType.String, analyzer = "text", searchAnalyzer = "text")
    public String __;

    public void setText(Lang lang, String text) {
        if (lang == null) {
            __ = text;
            return;
        }
        switch (lang) {
            case FR:
                fr = text;
                break;
            case EN:
                en = text;
                break;
            case DE:
                de = text;
                break;
            case IT:
                it = text;
                break;
            case SL:
                sl = text;
                break;
            default:
                __ = text;
                break;
        }
    }

    @Transient
    @JsonIgnore
    public String getContent() {
        for (java.lang.reflect.Field field : MultilingualText.class.getFields()) {
            String o;
            try {
                o = (String) field.get(this);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if (o != null) {
                return o;
            }
        }
        throw new IllegalStateException("Empty multilingual text");
    }

    public enum Lang {
        // french
        // no "créole" because we don't have any stemmer for it and the syntax is vastly different
        FR("FR"),
        // english
        EN("EN"),
        // german
        DE("DE"),
        // italian
        IT("IT"),
        // slovene
        SL("SL");

        String[] isoCodes;
        Lang(String... isoCodes) {
            this.isoCodes = isoCodes;
        }

        static Map<String, Lang> resolveMap = new HashMap<>();

        public static Lang resolve(String lang) {
            return resolveMap.get(lang.toUpperCase());
        }
    }
}

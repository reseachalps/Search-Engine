package eu.researchalps.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by loic on 08/07/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class SearchRequestTest {
    @Test
    public void testWordFiltering() {
        // make sure this word is inserted
        TextFiltering.addLine("oiv");
        TextFiltering.addLine("opérateur importance vitale");
        TextFiltering.addLine("opérateur stratégique vitale");
//        System.out.println(FORBIDDEN_MULTI_WORDS_IDX);
        Assert.assertEquals(null, TextFiltering.filterQuery(null));
        Assert.assertEquals("", TextFiltering.filterQuery(""));
        Assert.assertEquals("", TextFiltering.filterQuery("oiv"));
        Assert.assertEquals("", TextFiltering.filterQuery("-oiv"));
        Assert.assertEquals("", TextFiltering.filterQuery("+oiv"));
        Assert.assertEquals("", TextFiltering.filterQuery("\"oiv\""));
        Assert.assertEquals("paris label:", TextFiltering.filterQuery("paris label:oiv"));
        Assert.assertEquals("paris", TextFiltering.filterQuery("paris oiv"));
        Assert.assertEquals("paris", TextFiltering.filterQuery("paris Oìv"));
        Assert.assertEquals("\"paris\"", TextFiltering.filterQuery("\"paris oiv\""));
        Assert.assertEquals("paris innovation", TextFiltering.filterQuery("paris -oiv innovation"));
        Assert.assertEquals("paris innovant", TextFiltering.filterQuery("paris opération d'importance Vitale innovant"));
        Assert.assertEquals("paris opération d'importance metalurgique innovant", TextFiltering.filterQuery("paris opération d'importance metalurgique innovant"));
        Assert.assertEquals("paris innovant", TextFiltering.filterQuery("paris \"opérations actuelles d'Importances Vitale\" innovant"));
        Assert.assertEquals("paris innovant", TextFiltering.filterQuery("paris vital à caractère stratégique opérateur innovant"));
    }
}
package eu.researchalps.util;

import org.junit.Assert;
import org.junit.Test;

import static eu.researchalps.util.NormalizeURL.normalize;
import static org.junit.Assert.*;

/**
 * Created by loic on 26/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class NormalizeURLTest {

    @Test
    public void testNormalizeForIdentification() throws Exception {
        Assert.assertEquals("data-publica.com", NormalizeURL.normalizeForIdentification("http://www.data-publica.com/"));
        Assert.assertEquals("data-publica.com/stuff", NormalizeURL.normalizeForIdentification("http://www.data-publica.com/stuff/"));
        Assert.assertEquals("data-publica.com/stuff.php?=youpi&canard=%20test%20&id=1", NormalizeURL.normalizeForIdentification("http://www.data-publica.com/stuff.php?id=1&canard=%20test%20&=youpi"));
        Assert.assertEquals("data-publica.com/stuff.php?=youpi&canard=%20test%20&id=1", NormalizeURL.normalizeForIdentification("http://www.data-publica.com/stuff.php?id=1&canard=%20test%20&=youpi&utm_source=twitter"));
        Assert.assertEquals("data-publica.com:8080/stuff", NormalizeURL.normalizeForIdentification("https://www.data-publica.com:8080/stuff"));
        Assert.assertEquals("data-publica.com", NormalizeURL.normalizeForIdentification("http://www.data-publica.com/index.php"));
        Assert.assertEquals("data-publica.com/bidule", NormalizeURL.normalizeForIdentification("http://www.data-publica.com/bidule/index.php"));
        try {
            NormalizeURL.normalizeForIdentification("www");
            fail("Should have trigger IAE");
        } catch (IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testNormalize() throws Exception {
        Assert.assertEquals("http://www.data-publica.com/index.php", NormalizeURL.normalize("http://www.data-publica.com/index.php"));
        Assert.assertEquals("http://www.data-publica.com/bidule/index.php", NormalizeURL.normalize("http://www.data-publica.com/bidule/index.php"));
    }
}
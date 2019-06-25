package com.datapublica.companies.util;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
public class NormalizeText {
    private static final Pattern PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final String ALPHANUM_REGEXP = "[^a-z0-9]";
    private static final String EMPTY = "";
    private static final String PUNCTUATION_REGEX = "\\p{P}\\p{S}";
    private static final String MULTISPACE_REGEX = "^ +| +$|( )+";
    private static final String SPACE = " ";
    private static final List<String> COMPANIES_ACRONYMS = Lists.newArrayList("sarl", "sas", "sa", "s.a.s.", "eurl", "s.a.s");

    /**
     * Normalize a string for item comparison.
     * 
     * @param text The text to normalize
     * @return ASCII lowercased string, removing non alphanum chars.
     */
    public static String normalize(String text) {
        if (text==null) return text;
        final String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        final String result = PATTERN.matcher(normalized).replaceAll(EMPTY);
        return result.toLowerCase().replaceAll(ALPHANUM_REGEXP, EMPTY);
    }

    /**
     * Normalize a text (company name) for ES query.
     *
     * @param text The text to normalize
     * @return ASCII lowercased string, replacing non alphanum chars by a space.
     */
    public static String normalize_query(String text) {
        if (text==null) return text;
        final String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String result = PATTERN.matcher(normalized).replaceAll(EMPTY);
        result = result.toLowerCase();
        Collection<String> words = Collections2.filter(Lists.newArrayList(Splitter.on(" ").split(result)), Predicates.not(Predicates.in(COMPANIES_ACRONYMS)));
        result = Joiner.on(" ").join(words);
        return result.replaceAll(ALPHANUM_REGEXP, SPACE).replaceAll(MULTISPACE_REGEX, SPACE);
    }

    public static boolean equalsAfterNormalize(String text1, String text2)    {
        if ( text1==null || text2==null) return text1==text2;
        return NormalizeText.normalize(text1).equals(NormalizeText.normalize(text2));
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String readableBadlyEncodedString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            if (b >= 0) {
                sb.append(b);
            } else {
                int v = b & 0xFF;
                sb.append("\\x");
                sb.append(hexArray[v >>> 4]);
                sb.append(hexArray[v & 0x0F]);
            }
        }
        return sb.toString();
    }
}

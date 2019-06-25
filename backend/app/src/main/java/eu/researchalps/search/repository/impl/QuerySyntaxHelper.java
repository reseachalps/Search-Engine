package eu.researchalps.search.repository.impl;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * samuel
 * 16/11/15, 16:31
 */
public class QuerySyntaxHelper {

    public static String fixQuery(String query) {
        if (query == null)
            return null;
        
        // Replace colon by space
        query = query.replace(':', ' ');

        query = balanceQuotes(query);
        query = balanceParenthesis(query);

        // Replace operators we don't need by their escaped version
        query = query.replaceAll("(?<!\\\\)([\\Q/^:[]{}?~\\E])", "\\\\$1");

        // Replace trailing + or - by their escaped version
        query = query.replaceAll("(?<!\\\\)([\\Q-+\\E])\\s*$", "\\\\$1");

        // Remove trailing AND OR NOT
        query = query.replaceAll("(^|\\s+)(AND|OR|NOT)\\s*$", "");
        query = query.replaceAll("^\\s*(AND|OR)(\\s+|$)", ""); // a leading NOT is valid
        return query;
    }


    public static String balanceQuotes(String query) {
        boolean isEscaped = false;
        int lastOpeningQuote = -1;

        for (int idx = 0; idx < query.length(); ++idx) {
            char c = query.charAt(idx);

            if (c == '\\') {
                // "not" operation since you can escape a "\"
                isEscaped = !isEscaped;
                continue;
            }

            // If the character is escaped, it's of no use
            if (isEscaped) {
                // It's not another "\" else it would have activated the previous block
                // So it was another escaped character, we reset escape state
                isEscaped = false;
                continue;
            }

            if (c == '"') {
                if (lastOpeningQuote == -1) {
                    lastOpeningQuote = idx;
                } else {
                    lastOpeningQuote = -1;
                }
                continue;
            }
        }

        if (lastOpeningQuote != -1) {
            query = query.substring(0, lastOpeningQuote) + ' ' + query.substring(lastOpeningQuote + 1);
        }

        return query;
    }

    public static String balanceParenthesis(String query) {
        boolean isEscaped = false;
        boolean isInsideQuotes = false;

        List<Integer> openingParenthesis = new ArrayList<>();
        // If we have closing parenthesis or brackets, extra opening characters must be deleted adding them
        // in this structure
        List<Integer> charactersToRemove = new ArrayList<>();

        for (int idx = 0; idx < query.length(); ++idx) {
            char c = query.charAt(idx);

            if (c == '\\') {
                // "not" operation since you can escape a "\"
                isEscaped = !isEscaped;
                continue;
            }

            // If the character is escaped, it's of no use
            if (isEscaped) {
                // It's not another "\" else it would have activated the previous block
                // So it was another escaped character, we reset escape state
                isEscaped = false;
                continue;
            }

            if (c == '"') {
                isInsideQuotes = !isInsideQuotes;
                continue;
            }

            // Everything inside a quoted string is allowed
            if (isInsideQuotes) {
                continue;
            }

            if (c == '(') {
                openingParenthesis.add(idx);
            }
            if (c == ')') {
                if (openingParenthesis.size() == 0) {
                    charactersToRemove.add(idx);
                } else {
                    openingParenthesis.remove(openingParenthesis.size() - 1);
                }
            }
        }

        charactersToRemove.addAll(openingParenthesis);
        charactersToRemove.sort(Integer::compareTo);
        Collections.reverse(charactersToRemove);

        for (Integer idx : charactersToRemove) {
            query = query.substring(0, idx) + ' ' + query.substring(idx + 1);
        }

        return query;
    }

    public static boolean checkValid(String query) {
        QueryParser qp = new QueryParser("", new WhitespaceAnalyzer());
        try {
            qp.parse(query);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

}
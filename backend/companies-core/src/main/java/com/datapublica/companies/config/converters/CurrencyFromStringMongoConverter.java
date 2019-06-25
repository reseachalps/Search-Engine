package com.datapublica.companies.config.converters;

import java.util.Currency;

/**
 *
 */
public class CurrencyFromStringMongoConverter implements org.springframework.core.convert.converter.Converter<String, Currency> {
    @Override
    public Currency convert(String source) {
        return Currency.getInstance(source);
    }
}

package com.datapublica.companies.config.converters;

import java.util.Currency;

/**
 *
 */
public class CurrencyToStringMongoConverter implements org.springframework.core.convert.converter.Converter<Currency, String> {
    @Override
    public String convert(Currency source) {
        return source.getCurrencyCode();
    }
}

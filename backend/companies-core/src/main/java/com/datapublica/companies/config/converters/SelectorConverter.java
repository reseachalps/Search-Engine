package com.datapublica.companies.config.converters;

import com.datapublica.companies.api.selector.Selector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class SelectorConverter implements GenericConverter {
    public static final HashSet<ConvertiblePair> TYPES;

    static {
        TYPES = new HashSet<>();
        TYPES.add(new ConvertiblePair(String.class, Selector.class));
    }

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return TYPES;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        final Class<?> targetClass = targetType.getType();
        try {
            return mapper.readValue((String) source, targetClass);
        } catch (IOException e) {
            throw new ConversionNotSupportedException(source, targetClass, e);
        }
    }
}

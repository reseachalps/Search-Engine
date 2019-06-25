package com.datapublica.companies.workflow;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Declaration of publish nodes
 */
public class ExchangeNode<DTO> {
    /*
        COMPANY_UPDATED(CompanyUpdated.class),
        WEBSITE_UPDATED(WebsiteUpdated.class),
        FULLCOMPANY_UPDATED(FullCompanyUpdated.class);
     */
    private Class<DTO> clazz;
    private String name;

    /**
     * The DTO class
     *
     * @param clazz The class
     */
    private ExchangeNode(String name, Class<DTO> clazz) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Exchange node name cannot be empty");
        }
        this.name = name;
        this.clazz = clazz;
    }

    public Class<DTO> getMessageClass() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeNode<?> that = (ExchangeNode<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    private static Map<String, ExchangeNode<?>> instances = new HashMap<>();

    public static <DTO> ExchangeNode<DTO> get(String name, Class<DTO> dtoClass) {
        name = name.toUpperCase();
        ExchangeNode<?> node = instances.get(name);
        if (node == null) {
            ExchangeNode<DTO> exchangeNode = new ExchangeNode<>(name, dtoClass);
            instances.put(name, exchangeNode);
            return exchangeNode;
        }
        if (!node.clazz.equals(dtoClass)) {
            throw new IllegalArgumentException("Declared exchange node " + name + " has conflicting dto class declaration [" + node.getMessageClass().toString() + "] / [" + dtoClass.toString() + "]");
        }
        //noinspection unchecked
        return (ExchangeNode<DTO>) node;
    }

    public static Collection<ExchangeNode<?>> all() {
        return instances.values();
    }
}

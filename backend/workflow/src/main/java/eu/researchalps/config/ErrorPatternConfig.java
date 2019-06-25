package eu.researchalps.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by loic on 09/06/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Configuration
public class ErrorPatternConfig {
    private static final Logger log = LoggerFactory.getLogger(ErrorPatternConfig.class);

    @Value("${errors.recover:}")
    private String[] recover;
    @Value("${errors.ignore:}")
    private String[] ignore;

    private Set<Pattern> recoverPatterns;
    private Set<Pattern> ignorePatterns;

    @PostConstruct
    private void init() {
        recoverPatterns = Stream.of(recover).map(String::toLowerCase).map(String::trim).map(Pattern::compile).collect(Collectors.toSet());
        ignorePatterns = Stream.of(ignore).map(String::toLowerCase).map(String::trim).map(Pattern::compile).collect(Collectors.toSet());
        log.info("Recover patterns are "+recoverPatterns+" and Ignored patterns are "+ignorePatterns);
    }

    public Set<Pattern> getRecover() {
        return recoverPatterns;
    }

    public Set<Pattern> getIgnore() {
        return ignorePatterns;
    }
}

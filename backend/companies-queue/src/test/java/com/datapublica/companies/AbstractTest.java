package com.datapublica.companies;

/**
 * Created by vyncent on 13/01/16.
 */

import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@ActiveProfiles({"test", "workflow"})
@WebAppConfiguration
@Configuration
@ComponentScan(basePackages = "com.datapublica")
@ContextConfiguration(classes = {AbstractTest.class})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractTest {

    /**
     * @return
     */
    @Bean
    public PropertyResourceConfigurer propertyResourceConfigurer() {
        final PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setLocation(new ClassPathResource("/datapublica.properties"));
        configurer.setNullValue("@null");
        return configurer;
    }

}

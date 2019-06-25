package eu.researchalps.config;

import eu.researchalps.api.util.UserLocaleResolver;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
public class WebMVCConfiguration extends WebMvcConfigurerAdapter {

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipart = new CommonsMultipartResolver();
        multipart.setMaxUploadSize(1 << 26); // 64MB
        return multipart;
    }

    @Bean
    public ServletRegistrationBean dispatcher(DispatcherServlet servlet) {
        return new ServletRegistrationBean(servlet, "/api/*");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new UserLocaleResolver());
    }
}

package eu.researchalps.config;

import eu.researchalps.api.util.UserLocale;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        TypeResolver typeResolver = new TypeResolver();
        AlternateTypeRule collectionRule
                = AlternateTypeRules.newRule(
                //replace Collection<T> for any T
                typeResolver.resolve(Collection.class, WildcardType.class),
                //with List<T> for any T
                typeResolver.resolve(List.class, WildcardType.class));


        return new Docket(DocumentationType.SWAGGER_2)
//                .pathMapping("/api")
                .genericModelSubstitutes()
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .forCodeGeneration(true)
                .ignoredParameterTypes(UserLocale.class)
                .alternateTypeRules(
                        collectionRule,
                        AlternateTypeRules.newRule(LocalDateTime.class, Date.class),
                        AlternateTypeRules.newRule(LocalDate.class, Date.class)
                )
                .useDefaultResponseMessages(false);
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfiguration.DEFAULT;
    }
}

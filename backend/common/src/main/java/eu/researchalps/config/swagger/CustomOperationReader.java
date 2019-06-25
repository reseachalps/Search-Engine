package eu.researchalps.config.swagger;

import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

@Component
public class CustomOperationReader implements OperationBuilderPlugin {
    @Override
    public void apply(OperationContext context) {
        String operationName = context.getHandlerMethod().getMethod().getName();
        context.operationBuilder().codegenMethodNameStem(operationName);
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}

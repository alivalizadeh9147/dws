package ir.av.dws.wallet.adapters.primary.rest.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new
                OpenAPI()
                .info(new Info()
                        .title("API Documentation")
                        .version("1.0"));
    }


    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("Wallets")
                .pathsToMatch("/wallets/**")
                .addOpenApiCustomizer(openApi -> {
                    Components components = openApi.getComponents();

                    if (components == null) {
                        components = new Components();
                        openApi.setComponents(components);
                    }

                    components.addSecuritySchemes(
                            "bearerAuth",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                    );

                    openApi.addSecurityItem(
                            new SecurityRequirement()
                                    .addList("bearerAuth")
                    );
                })
                .build();
    }
}

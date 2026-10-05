package ir.av.dws.user.swagger;

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
                .group("Authentication")
                .pathsToMatch("/api/auth/**")
                .addOpenApiCustomizer(openApi -> {
                    Components components = openApi.getComponents();
                    if (components == null) {
                        components = new Components();
                        openApi.setComponents(components);
                    }

                    components.addSecuritySchemes("myLoginAuth",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.OAUTH2)
                                    .flows(new OAuthFlows()
                                            .password(new OAuthFlow()
                                                    .tokenUrl("/api/auth/login")
                                                    .scopes(new Scopes())
                                            )
                                    )
                    );

                    openApi.addSecurityItem(new SecurityRequirement().addList("myLoginAuth"));
                })
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("Public")
                .pathsToMatch("/api/public/**")
                .build();
    }
}

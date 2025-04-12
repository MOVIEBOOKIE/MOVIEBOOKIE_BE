package project.luckybooky.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(title = "MOVIEBOOKIE API 명세서",
                description = "MOVIEBOOKIE API 명세서",
                version = "v1"),
        servers = @Server(url = "/", description = "Default Server URL"))// Server 정보 추가)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("LUCKYBOOKY API")
                .description("LUCKYBOOKY API Docs")
                .version("1.0");
    }
}

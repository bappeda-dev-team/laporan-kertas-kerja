package cc.kertaskerja.laporan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Laporan API Service")
                        .version("1.0.0")
                        .description("API untuk Koleksi Laporan Kertas Kerja")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")));
    }
}

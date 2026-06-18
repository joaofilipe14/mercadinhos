package pt.devoteam.identidade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🎯 Diz ao Spring para mapear o URL http://localhost:8082/uploads/...
        // diretamente para a pasta física "uploads/" na raiz do teu projeto.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
package pt.devoteam.identidade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // 🎯 A anotação obrigatória para o Gateway reativo!
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // Desativamos o CORS no Spring Security porque o application.yml do Gateway já trata disso
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Deixamos passar tudo a nível do Spring Security!
                        // Quem faz o verdadeiro "trabalho sujo" de bloquear pedidos sem token
                        // é o nosso JwtGatewayFilter que construímos no passo anterior.
                        .anyExchange().permitAll()
                )
                .build();
    }
}
package pt.devoteam.identidade.security;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // 1. Identifica se a rota é pública ou não
        boolean isRotaPublica = path.contains("/api/auth/") ||
                ((path.startsWith("/api/mercado") && "GET".equalsIgnoreCase(method)) ||
                        (path.startsWith("/cartazes-bucket") && "GET".equalsIgnoreCase(method)));

        // 2. Verifica se o cabeçalho Authorization existe
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Cenário A: Utilizador NÃO tem login (Não enviou token)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isRotaPublica) {
                // Se é público (ex: cidadão a ver a vitrine), deixa passar sem headers!
                return chain.filter(exchange);
            } else {
                // Se tenta aceder a rota privada sem token, bloqueia!
                System.out.println("🔴 [Gateway] Pedido bloqueado: Token ausente para rota privada.");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        // Cenário B: Utilizador TEM login (Enviou token)
        String token = authHeader.substring(7);

        try {
            // 3. Valida o Token
            Claims claims = JwtUtil.validateToken(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            System.out.println("🟢 [Gateway] Token processado para: " + email + " | Role: " + role);

            // 4. Muta o pedido HTTP para injetar os cabeçalhos para os microsserviços
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            // 5. Encaminha o pedido mutado e ENRIQUECIDO para o destino
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            System.out.println("⚠️ [Gateway] Token inválido ou expirado.");

            // Se a rota for pública, ignora o token estragado e deixa ver a montra
            if (isRotaPublica) {
                return chain.filter(exchange);
            }

            // Se for privada, bloqueia
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
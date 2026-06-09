package pt.devoteam.mercados.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.devoteam.mercados.entity.Mercado;
import pt.devoteam.mercados.service.MercadoService;

import java.util.List;

@RestController
@RequestMapping("/api/mercados")
public class MercadoController {

    private final MercadoService mercadoService;

    // Injeção limpa e recomendada via construtor
    public MercadoController(MercadoService mercadoService) {
        this.mercadoService = mercadoService;
    }

    @GetMapping
    public ResponseEntity<List<Mercado>> listarMercados(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        // 🛡️ Filtro Inteligente: Se for Autarquia vê tudo, senão só vê as feiras Aprovadas
        if (role != null && (role.equals("ROLE_MUNICIPO") || role.equals("ROLE_JUNTA"))) {
            return ResponseEntity.ok(mercadoService.listarCriadosPor(email));
        }
        return ResponseEntity.ok(mercadoService.listarMercadosAprovados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mercado> obterMercado(@PathVariable Long id) {
        // O Controller delega a pesquisa para o Service e lida com o HTTP 404 Not Found se não existir
        return mercadoService.obterMercado(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criarMercado(
            @RequestBody Mercado mercado,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String role,
            @RequestHeader(value = "X-User-Email", defaultValue = "anonym") String email) {
        // 🛡️ Filtro de Segurança Rest
        if (!role.equals("ROLE_MUNICIPO") && !role.equals("ROLE_JUNTA") && !role.equals("ROLE_ORGANIZADOR")) {
            return ResponseEntity.status(403).body("Apenas autarquias ou organizadores autorizados podem criar feiras.");
        }
        Mercado mercadoSalvo = mercadoService.criarMercado(mercado, role, email);
        return ResponseEntity.ok(mercadoSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarMercado(
            @PathVariable Long id,
            @RequestBody Mercado mercadoAtualizado,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String role) {
        // 🛡️ Bloqueia feirantes ou anónimos de tentarem injetar edições nas feiras via Postman/Terminal
        if (!role.equals("ROLE_MUNICIPO") && !role.equals("ROLE_JUNTA")) {
            return ResponseEntity.status(403).body("Sem permissões para editar feiras.");
        }
        try {
            Mercado mercadoSalvo = mercadoService.atualizarMercado(id, mercadoAtualizado);
            return ResponseEntity.ok(mercadoSalvo);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/municipio/dashboard")
    public ResponseEntity<pt.devoteam.mercados.dto.DashboardMunicipioDTO> getDashboard(
            @RequestHeader("X-User-Email") String emailMunicipio) {
        return ResponseEntity.ok(mercadoService.obterDadosDashboard(emailMunicipio));
    }

    @GetMapping("/proximos")
    public ResponseEntity<List<Mercado>> listarMercadosProximos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double raio) {

        List<Mercado> mercados = mercadoService.listarMercadosProximos(lat, lng, raio);
        return ResponseEntity.ok(mercados);
    }
}
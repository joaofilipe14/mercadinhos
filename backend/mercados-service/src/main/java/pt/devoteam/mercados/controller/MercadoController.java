package pt.devoteam.mercados.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.devoteam.mercados.entity.Mercado;
import pt.devoteam.mercados.service.MercadoService;

import java.util.List;

@RestController
@RequestMapping("/api/mercados")
public class MercadoController {

    private final MercadoService mercadoService;

    public MercadoController(MercadoService mercadoService) {
        this.mercadoService = mercadoService;
    }

    @GetMapping
    public ResponseEntity<Page<Mercado>> listarMercados(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable paginacao = PageRequest.of(page, size);
        List<Mercado> mercadosAprovados = mercadoService.listarMercadosAprovados();
        return ResponseEntity.ok(converteParaPage(mercadosAprovados, paginacao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mercado> obterMercado(@PathVariable Long id) {
        return mercadoService.obterMercado(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/criados-por/{email}")
    public ResponseEntity<List<Mercado>> obterMercadoPor(@PathVariable String email) {
        return ResponseEntity.ok(mercadoService.listarCriadosPor(email));
    }

    @PostMapping
    public ResponseEntity<?> criarMercado(
            @RequestBody Mercado mercado,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String role,
            @RequestHeader(value = "X-User-Email", defaultValue = "anonym") String email) {
        if (!role.equals("ROLE_MUNICIPIO") && !role.equals("ROLE_JUNTA") && !role.equals("ROLE_ORGANIZADOR")) {
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
        if (!role.equals("ROLE_MUNICIPIO") && !role.equals("ROLE_JUNTA")) {
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
    public ResponseEntity<Page<Mercado>> listarMercadosProximos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double raio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable paginacao = PageRequest.of(page, size);
        Page<Mercado> mercados = mercadoService.listarMercadosProximos(lat, lng, raio, paginacao);
        return ResponseEntity.ok(mercados);
    }

    /**
     * 🧮 Utilitário: Transforma sublistas em objetos estruturados Page do Spring Data
     */
    private <T> Page<T> converteParaPage(List<T> lista, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), lista.size());

        if (start > lista.size()) {
            return new PageImpl<>(List.of(), pageable, lista.size());
        }
        return new PageImpl<>(lista.subList(start, end), pageable, lista.size());
    }
}
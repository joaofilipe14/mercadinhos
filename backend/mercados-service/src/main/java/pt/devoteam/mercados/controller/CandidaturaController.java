package pt.devoteam.mercados.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.dto.CandidaturaDTO;
import pt.devoteam.mercados.entity.Candidatura;
import pt.devoteam.mercados.entity.enums.TipoDocumento;
import pt.devoteam.mercados.service.CandidaturaService;

import java.util.List;

@RestController
@RequestMapping("/api/candidaturas")
public class CandidaturaController {

    private final CandidaturaService candidaturaService;

    public CandidaturaController(CandidaturaService candidaturaService) {
        this.candidaturaService = candidaturaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidatura> obterCandidaturaPorId(@PathVariable Long id) {
        try {
            Candidatura candidatura = candidaturaService.obterCandidaturaPorId(id);
            return ResponseEntity.ok(candidatura);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/submeter")
    public ResponseEntity<String> submeter(@ModelAttribute CandidaturaDTO dto) {
        try {
            candidaturaService.submeterCandidatura(dto);
            return ResponseEntity.ok("Candidatura submetida e vaga reservada com sucesso!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao processar a candidatura: " + e.getMessage());
        }
    }

    @GetMapping("/inscritas")
    public ResponseEntity<List<Long>> obterInscricoesDoFeirante(@RequestParam("email") String email) {
        // O Controller apenas delega para a camada de negócio!
        List<Long> mercadosInscritos = candidaturaService.obterMercadosInscritosPorFeirante(email);
        return ResponseEntity.ok(mercadosInscritos);
    }

    @GetMapping("/mercado/{mercadoId}")
    public ResponseEntity<List<Candidatura>> obterCandidatos(@PathVariable Long mercadoId, @RequestHeader("X-User-Role") String role) {
        if (!role.equals("ROLE_MUNICIPO") && !role.equals("ROLE_JUNTA")) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(candidaturaService.listarCandidaturasPorMercado(mercadoId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> atualizarEstado(
            @PathVariable Long id,
            @RequestParam("estado") pt.devoteam.mercados.entity.enums.EstadoCandidatura estado,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ROLE_MUNICIPO") && !role.equals("ROLE_JUNTA")) {
            return ResponseEntity.status(403).build();
        }
        candidaturaService.atualizarEstadoCandidatura(id, estado);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/documentos/{tipoDocumento}")
    public ResponseEntity<Resource> descarregarDocumento(
            @PathVariable Long id,
            @PathVariable TipoDocumento tipoDocumento) throws Exception {

        Resource arquivo = candidaturaService.carregarDocumentoComoRecurso(id, tipoDocumento);

        // Este header obriga o Browser a fazer Download em vez de tentar abrir no ecrã e dá-lhe o nome certo (.pdf)
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + tipoDocumento.name() + ".pdf\"")
                .body(arquivo);
    }
}
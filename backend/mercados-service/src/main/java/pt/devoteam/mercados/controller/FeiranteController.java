package pt.devoteam.mercados.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.dto.FeiranteDTO;
import pt.devoteam.mercados.entity.Feirante;
import pt.devoteam.mercados.service.FeiranteService;

import java.util.List;

@RestController
@RequestMapping("/api/feirantes")
public class FeiranteController {
    private final FeiranteService feiranteService;

    public FeiranteController(FeiranteService feiranteService) {
        this.feiranteService = feiranteService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feirante> obterFeirantePorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(feiranteService.obterFeirantePorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<FeiranteDTO> obterPerfilPorEmail(@RequestParam String email) {
        try {
            return ResponseEntity.ok(feiranteService.obterFeirantePorEmail(email));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 🎯 NOVO ENDPOINT: Devolve a lista de códigos dos documentos que o feirante já tem arquivados.
     * Consumido pelo modal do Angular para acender os badges de reaproveitamento.
     */
    @GetMapping("/perfil/documentos-ativos")
    public ResponseEntity<List<String>> obterDocumentosAtivosDoPerfil(@RequestParam String email) {
        try {
            return ResponseEntity.ok(feiranteService.listarDocumentosAtivosDoPerfil(email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 🎯 NOVO ENDPOINT: Sincroniza e atualiza um documento específico na pasta digital do MinIO.
     * Chamado em background quando o feirante opta por atualizar o perfil a partir da inscrição.
     */
    @PostMapping(value = "/perfil/atualizar-documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> atualizarDocumentoDoPerfil(
            @RequestParam("email") String email,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("file") MultipartFile file) {
        try {
            feiranteService.guardarDocumentoNoPortfolio(email, tipoDocumento, file);
            return ResponseEntity.ok("Pasta digital do feirante atualizada com sucesso no MinIO.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Falha ao sincronizar documento do perfil: " + e.getMessage());
        }
    }

    @PostMapping(value = "/upload-portfolio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPortfolio(
            @RequestParam("email") String email,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("file") MultipartFile file) {
        try {
            feiranteService.guardarDocumentoNoPortfolio(email, tipoDocumento, file);
            return ResponseEntity.ok("Ficheiro guardado com sucesso na pasta digital.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Falha ao processar o upload: " + e.getMessage());
        }
    }
}
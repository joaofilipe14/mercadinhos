package pt.devoteam.mercados.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.dto.FeiranteDTO;
import pt.devoteam.mercados.entity.Feirante;
import pt.devoteam.mercados.service.FeiranteService;

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
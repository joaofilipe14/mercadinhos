package pt.devoteam.notificacoes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.devoteam.notificacoes.entity.Municipio;
import pt.devoteam.notificacoes.service.MunicipioService; // 🎯 Importa a nova camada

@RestController
@RequestMapping("/api/municipios")
public class MunicipioController {

    private final MunicipioService municipioService;

    // Injeção de dependência elegante via Construtor orientada ao Serviço
    public MunicipioController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<Municipio> obterPerfil(@RequestParam String email) {
        Municipio municipio = municipioService.obterOuCriarPerfilBase(email);
        return ResponseEntity.ok(municipio);
    }

    @PostMapping("/atualizar-perfil")
    public ResponseEntity<?> atualizarPerfil(@RequestBody Municipio dados) {
        municipioService.atualizarPerfil(dados);
        return ResponseEntity.ok().body("{\"message\": \"Perfil da Autarquia gravado via Service com sucesso!\"}");
    }
}
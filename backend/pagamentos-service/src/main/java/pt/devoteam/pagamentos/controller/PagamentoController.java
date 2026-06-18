package pt.devoteam.pagamentos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.devoteam.pagamentos.dto.ProcessarPagamentoDTO;
import pt.devoteam.pagamentos.service.TransacaoService; // 🎯 Importado

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final TransacaoService transacaoService;

    // Construtor limpo injetando apenas a camada de serviço
    public PagamentoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping("/efetuar")
    public ResponseEntity<String> efetuarPagamento(@RequestBody ProcessarPagamentoDTO dto) {
        try {
            // Executa a lógica de negócio e obtém o recibo bancário
            String recibo = transacaoService.registarEProcessarPagamento(dto);
            return ResponseEntity.ok("Liquidação autorizada. Recibo: " + recibo);

        } catch (IllegalStateException e) {
            // Captura o erro de idempotência (Candidatura já paga) e devolve um Bad Request (400)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Captura erros inesperados e devolve Internal Server Error (500)
            return ResponseEntity.status(500).body("Erro na gateway de pagamentos: " + e.getMessage());
        }
    }
}
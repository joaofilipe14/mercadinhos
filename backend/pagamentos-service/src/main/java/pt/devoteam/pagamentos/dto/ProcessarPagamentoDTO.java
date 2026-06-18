package pt.devoteam.pagamentos.dto;

public record ProcessarPagamentoDTO(
        Long candidaturaId,
        Double valor,
        String emailFeirante,
        String canalEscolhido
) {}
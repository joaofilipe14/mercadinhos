package pt.devoteam.pagamentos.event;

public record PagamentoConcluidoEvent(Long candidaturaId, String transacaoId, String estado) {}
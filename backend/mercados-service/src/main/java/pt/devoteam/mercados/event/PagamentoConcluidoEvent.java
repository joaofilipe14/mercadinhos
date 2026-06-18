package pt.devoteam.mercados.event;

public record PagamentoConcluidoEvent(Long candidaturaId, String transacaoId, String estado) {}
package pt.devoteam.pagamentos.event;

public record CandidaturaAprovadaEvent(
        Long candidaturaId,
        String emailFeirante,
        String nomeFeirante,
        String nomeMercado,
        Double precoTotal
) {}
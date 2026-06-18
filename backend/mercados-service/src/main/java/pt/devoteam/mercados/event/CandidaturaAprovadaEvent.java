package pt.devoteam.mercados.event;

public record CandidaturaAprovadaEvent(
        Long candidaturaId,
        String emailFeirante,
        String nomeFeirante,
        String nomeMercado,
        Double precoTotal
) {}
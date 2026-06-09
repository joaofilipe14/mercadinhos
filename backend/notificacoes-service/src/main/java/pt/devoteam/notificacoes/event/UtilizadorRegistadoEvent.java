package pt.devoteam.notificacoes.event;

public record UtilizadorRegistadoEvent(String email, String nome, String role) {}
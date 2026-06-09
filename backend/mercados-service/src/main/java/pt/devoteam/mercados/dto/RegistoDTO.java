package pt.devoteam.mercados.dto;

import lombok.Data;

@Data
public class RegistoDTO {

    private String nome;       // Nome do Feirante ou Designação da Autarquia (usado para o payload do Kafka)
    private String email;      // Irá mapear para o 'username' na entidade
    private String password;   // Senha em texto limpo vinda do Angular (para encriptar no Service)
    private String role;       // A Role selecionada no <select> do ecrã do Angular
}
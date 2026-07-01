package pt.devoteam.identidade.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilizadores")
@Data
@NoArgsConstructor
public class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    // Palavra-passe que será guardada sempre encriptada (BCrypt)
    @Column(nullable = false, length = 255)
    private String password;

    // Guarda uma das 6 Roles: ROLE_ADMIN, ROLE_MUNICIPIO, ROLE_JUNTA, ROLE_ORGANIZADOR, ROLE_FEIRANTE, ROLE_USER
    @Column(nullable = false, length = 30)
    private String role;

    // Campos de auditoria úteis para controlo interno
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "ativo")
    private boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }
}
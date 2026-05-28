package pt.devoteam.feirantes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feirantes")
@Data
@NoArgsConstructor
public class Feirante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String nif;

    private String documentoAtividade; // Número do registo da DGAE ou similar
}
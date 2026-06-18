package pt.devoteam.mercados.entity;

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
    private String email;
    private String nif;
    private String documentoAtividade;
    private String documentoFinancas;
}
package pt.devoteam.camaras.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "mercados")
@Data
@NoArgsConstructor
public class Mercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String municipio;

    private Double latitude;
    private Double longitude;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    private String estado; // ABERTO ou FECHADO
}
package pt.devoteam.cidadaos.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mercados_leitura")
@Data
@NoArgsConstructor
public class MercadoLeitura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mercadoId; // ID original vindo do camaras-service

    private String nome;

    private double latitude;

    private double longitude;
}
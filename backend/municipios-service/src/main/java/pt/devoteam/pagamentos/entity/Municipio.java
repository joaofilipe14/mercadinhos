package pt.devoteam.pagamentos.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "municipios")
@Data
@NoArgsConstructor
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // Elo de ligação idêntico ao username da segurança

    @Column(nullable = false)
    private String nomeCamara; // Ex: "Câmara Municipal de Aveiro"

    private String nifAutarquia; // NIPC da Câmara
    private String telefoneOficial;
    private String moradaPacosConcelho;
    private String brasaoUrl; // Link para a imagem do brasão oficial
}
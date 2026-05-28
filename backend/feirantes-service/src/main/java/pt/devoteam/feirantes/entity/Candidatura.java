package pt.devoteam.feirantes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidaturas")
@Data
@NoArgsConstructor
public class Candidatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mercadoId; // Relacionamento lógico (Bounded Context separado)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feirante_id", nullable = false)
    private Feirante feirante;

    private LocalDateTime dataSubmissao;

    private String estado; // PENDENTE, APROVADO, REJEITADO

    private String documentoPdfPath; // Caminho para o ficheiro PDF guardado
}
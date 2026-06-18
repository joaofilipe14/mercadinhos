package pt.devoteam.pagamentos.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
@Data
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long candidaturaId;
    private Double valor;
    private String emailFeirante;
    private String transacaoId; // Código único de recibo (ex: TX-A12B34)
    private String estado;      // PENDENTE, PAGO, FALHADO
    private LocalDateTime dataTransacao = LocalDateTime.now();
}
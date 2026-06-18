package pt.devoteam.mercados.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.devoteam.mercados.entity.enums.EstadoCandidatura;
import pt.devoteam.mercados.entity.enums.TipoDocumento;
import pt.devoteam.mercados.entity.enums.TipoInfraestrutura;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "candidaturas")
@Data
@NoArgsConstructor
public class Candidatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relação Perfeita: Uma candidatura pertence a um mercado real na mesma BD!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Mercado mercado;

    // 🎯 A TUA SUGESTÃO: A ligação perfeita ao feirante espelho
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feirante_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Feirante feirante;

    @ElementCollection
    @CollectionTable(
            name = "candidatura_documentos_anexados",
            joinColumns = @JoinColumn(name = "candidatura_id")
    )
    @MapKeyColumn(name = "tipo_documento")
    @MapKeyEnumerated(EnumType.STRING) // Guarda o ID do Enum (ex: "INICIO_ACTIVIDADE")
    @Column(name = "documento_pdf_path", nullable = false)
    private Map<TipoDocumento, String> documentosAnexados = new HashMap<>();;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private EstadoCandidatura estado = EstadoCandidatura.PENDENTE;

    private LocalDateTime dataSubmissao;
    @Column(name = "dias")
    private Integer dias;
    @Enumerated(EnumType.STRING)
    @Column(name = "opcao_infraestrutura", nullable = false)
    private TipoInfraestrutura opcaoInfraestrutura = TipoInfraestrutura.PROPRIO;

    @Column(name = "preco_total")
    private Double precoTotal;
    @PrePersist
    protected void onCreate() {
        this.dataSubmissao = LocalDateTime.now();
    }
}
package pt.devoteam.mercados.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.devoteam.mercados.entity.enums.EstadoMercado;
import pt.devoteam.mercados.entity.enums.TipoDocumento;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mercados")
@Data
@NoArgsConstructor
public class Mercado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String localizacao;

    // As nossas coordenadas geográficas para o cidadão ver mais tarde
    private double latitude;
    private double longitude;

    // Aqui estão as vagas que faltavam!
    @Column(nullable = false)
    private int vagas;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private EstadoMercado estado = EstadoMercado.PENDENTE;

    @ElementCollection(targetClass = TipoDocumento.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "mercado_documentos_exigidos", joinColumns = @JoinColumn(name = "mercado_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private List<TipoDocumento> documentosExigidos = new ArrayList<>();

    @Column(nullable = false)
    private String criadoPor;

    // 🎯 NOVOS CAMPOS DO REGULAMENTO DE TAXAS REAIS
    @Column(name = "tipo_preco")
    private String tipoPreco; // "EVENTO" ou "DIARIO"
    @Column(name = "aceita_street_food")
    private Boolean aceitaStreetFood;
    @Column(name = "disponibiliza_stands_organizacao")
    private Boolean disponibilizaStandsOrganizacao;

    @Column(name = "preco_artesanato_stand_proprio")
    private Double precoArtesanatoStandProprio;
    @Column(name = "preco_artesanato_stand_organizacao")
    private Double precoArtesanatoStandOrganizacao;
    @Column(name = "preco_street_food_stand_proprio")
    private Double precoStreetFoodStandProprio;
    @Column(columnDefinition = "TEXT")
    private String descricao;
    @Column(name = "pet_friendly")
    private boolean petFriendly = true;
    @Column(name = "tem_wc")
    private boolean temWc = true;
    @Column(name = "imagem_cartaz")
    private String imagemCartaz;
    @Transient
    private Double distancia;
}
package pt.devoteam.mercados.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.entity.enums.EstadoCandidatura;
import pt.devoteam.mercados.entity.enums.TipoInfraestrutura;

import java.util.List;

@Getter
@Setter
public class CandidaturaDTO {
    private Long id;
    private Long mercadoId;
    private String feiranteEmail;
    private TipoInfraestrutura opcaoInfraestrutura;
    private Integer dias;
    private Double precoTotal;
    private EstadoCandidatura estado;
    private List<MultipartFile> pdfFiles;
}
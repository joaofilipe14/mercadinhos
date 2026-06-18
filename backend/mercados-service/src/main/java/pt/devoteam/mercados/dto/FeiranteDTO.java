package pt.devoteam.mercados.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.entity.enums.TipoInfraestrutura;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class FeiranteDTO {
    private Long id;
    private String email;
    private String nome;
    private TipoInfraestrutura opcaoInfraestrutura;
    private Integer dias;
    private List<CandidaturaDTO> candidaturas;
    private Map<String, String> portfolioDocumentos;

}
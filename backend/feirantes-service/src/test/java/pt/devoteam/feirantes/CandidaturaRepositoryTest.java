package pt.devoteam.feirantes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import pt.devoteam.feirantes.entity.Candidatura;
import pt.devoteam.feirantes.entity.Feirante;
import pt.devoteam.feirantes.repository.CandidaturaRepository;
import pt.devoteam.feirantes.repository.FeiranteRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CandidaturaRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    @Autowired
    private FeiranteRepository feiranteRepository;

    @Autowired
    private CandidaturaRepository candidaturaRepository;

    @Test
    public void deveGravarFeiranteECandidaturaComSucesso() {
        // Arrange
        Feirante feirante = new Feirante();
        feirante.setNome("Manuel Silva");
        feirante.setNif("299999999");
        feirante.setDocumentoAtividade("P123456");
        Feirante feiranteSalvo = feiranteRepository.save(feirante);

        Candidatura candidatura = new Candidatura();
        candidatura.setMercadoId(1L); // ID do mercado simulado do outro serviço
        candidatura.setFeirante(feiranteSalvo);
        candidatura.setDataSubmissao(LocalDateTime.now());
        candidatura.setEstado("PENDENTE");
        candidatura.setDocumentoPdfPath("/uploads/certidao_2026.pdf");

        // Act
        Candidatura candidaturaSalva = candidaturaRepository.save(candidatura);

        // Assert
        assertThat(candidaturaSalva.getId()).isNotNull();
        assertThat(candidaturaSalva.getFeirante().getNif()).isEqualTo("299999999");
        assertThat(candidaturaSalva.getEstado()).isEqualTo("PENDENTE");
    }
}
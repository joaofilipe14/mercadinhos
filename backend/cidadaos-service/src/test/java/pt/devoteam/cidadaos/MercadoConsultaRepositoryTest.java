package pt.devoteam.cidadaos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import pt.devoteam.cidadaos.entity.MercadoLeitura;
import pt.devoteam.cidadaos.repository.MercadoConsultaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MercadoConsultaRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    @Autowired
    private MercadoConsultaRepository repository;

    @Test
    public void deveEncontrarMercadosProximosDentroDoRaio() {
        // Arrange - Mercado 1: Terreiro do Paço, Lisboa
        MercadoLeitura mercadoLisboa = new MercadoLeitura();
        mercadoLisboa.setMercadoId(1L);
        mercadoLisboa.setNome("Mercado da Baixa");
        mercadoLisboa.setLatitude(38.7075);
        mercadoLisboa.setLongitude(-9.1365);
        repository.save(mercadoLisboa);

        // Arrange - Mercado 2: Bolhão, Porto (Aprox. 270km de distância)
        MercadoLeitura mercadoPorto = new MercadoLeitura();
        mercadoPorto.setMercadoId(2L);
        mercadoPorto.setNome("Mercado do Bolhão");
        mercadoPorto.setLatitude(41.1486);
        mercadoPorto.setLongitude(-8.6061);
        repository.save(mercadoPorto);

        // Act - Cidadão está em Lisboa (Avenida da Liberdade) e procura num raio de 50km
        double minhaLat = 38.7223;
        double minhaLng = -9.1449;
        double raioKm = 50.0;

        List<MercadoLeitura> mercadosPerto = repository.buscarPorProximidade(minhaLat, minhaLng, raioKm);

        // Assert
        assertThat(mercadosPerto).hasSize(1);
        assertThat(mercadosPerto.get(0).getNome()).isEqualTo("Mercado da Baixa");
    }
}
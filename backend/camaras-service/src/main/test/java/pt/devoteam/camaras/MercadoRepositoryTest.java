package pt.devoteam.camaras;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import pt.devoteam.camaras.entity.Mercado;
import pt.devoteam.camaras.repository.MercadoRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MercadoRepositoryTest {

    // No Spring Boot 4.0, basta declarar o container como uma ligação de serviço.
    // O ciclo de vida do container é gerido automaticamente pelo contexto do Spring.
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start(); // Garante o arranque do container antes da inicialização do JPA
    }

    @Autowired
    private MercadoRepository mercadoRepository;

    @Test
    void deveGuardarEEncontrarMercado() {
        // Arrange
        Mercado mercado = new Mercado();
        mercado.setNome("Feira de São Mateus");
        mercado.setMunicipio("Viseu");
        mercado.setDataInicio(LocalDate.of(2026, 8, 10));
        mercado.setEstado("ABERTO");

        // Act
        Mercado mercadoGuardado = mercadoRepository.save(mercado);
        Mercado mercadoEncontrado = mercadoRepository.findById(mercadoGuardado.getId()).orElse(null);

        // Assert
        assertThat(mercadoEncontrado).isNotNull();
        assertThat(mercadoEncontrado.getNome()).isEqualTo("Feira de São Mateus");
    }
}
package pt.devoteam.camaras;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pt.devoteam.camaras.entity.Mercado;
import pt.devoteam.camaras.repository.MercadoRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MercadoRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
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
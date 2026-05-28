package pt.devoteam.camaras.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.camaras.entity.Mercado;

@Repository
public interface MercadoRepository extends JpaRepository<Mercado, Long> {
    // Por agora, para cumprir o nosso primeiro teste de TDD,
    // os métodos herdados do JpaRepository (como save e findById) são suficientes.
}
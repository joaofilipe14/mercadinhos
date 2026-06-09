package pt.devoteam.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.mercados.entity.Mercado;
import pt.devoteam.mercados.entity.enums.EstadoMercado;

import java.util.List;

@Repository
public interface MercadoRepository extends JpaRepository<Mercado, Long> {
    List<Mercado> findByEstado(EstadoMercado estado);

    // Para as Juntas ou Organizadores verem apenas os mercados que eles próprios criaram
    List<Mercado> findByCriadoPor(String criadoPor);
}
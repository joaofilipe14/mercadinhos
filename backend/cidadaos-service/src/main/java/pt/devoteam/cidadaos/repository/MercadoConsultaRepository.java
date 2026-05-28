package pt.devoteam.cidadaos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.devoteam.cidadaos.entity.MercadoLeitura;

import java.util.List;

@Repository
public interface MercadoConsultaRepository extends JpaRepository<MercadoLeitura, Long> {

    @Query(value = """
        SELECT *, (6371 * acos(
            cos(radians(:lat)) * cos(radians(m.latitude)) * cos(radians(m.longitude) - radians(:lng)) + 
            sin(radians(:lat)) * sin(radians(m.latitude))
        )) AS distancia 
        FROM mercados_leitura m 
        WHERE (6371 * acos(
            cos(radians(:lat)) * cos(radians(m.latitude)) * cos(radians(m.longitude) - radians(:lng)) + 
            sin(radians(:lat)) * sin(radians(m.latitude))
        )) <= :raio
        ORDER BY distancia ASC
        """, nativeQuery = true)
    List<MercadoLeitura> buscarPorProximidade(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("raio") double raio
    );
}
package pt.devoteam.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.mercados.entity.Feirante;
import java.util.Optional;

@Repository
public interface FeiranteRepository extends JpaRepository<Feirante, Long> {

    Optional<Feirante> findByEmail(String email);
}
package pt.devoteam.feirantes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.feirantes.entity.Feirante;

@Repository
public interface FeiranteRepository extends JpaRepository<Feirante, Long> {
}
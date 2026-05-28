package pt.devoteam.feirantes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.feirantes.entity.Candidatura;

@Repository
public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {
}
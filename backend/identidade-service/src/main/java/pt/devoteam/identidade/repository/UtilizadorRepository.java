package pt.devoteam.identidade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.identidade.entity.Utilizador;
import java.util.Optional;

@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {

    // Usado pelo Spring Security durante o processo de Login
    Optional<Utilizador> findByEmail(String email);

    // Usado para validação defensiva antes de criar uma conta
    boolean existsByEmail(String email);
}
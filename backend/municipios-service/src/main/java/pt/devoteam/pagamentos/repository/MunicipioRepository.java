package pt.devoteam.pagamentos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.pagamentos.entity.Municipio;
import java.util.Optional;

@Repository
public interface MunicipioRepository extends JpaRepository<Municipio, Long> {

    /**
     * 🎯 Método crucial para a ligação de dados do Perfil.
     * Permite que o MunicipioController procure as informações adicionais
     * da autarquia usando o email/username que veio do token de autenticação.
     */
    Optional<Municipio> findByEmail(String email);
}
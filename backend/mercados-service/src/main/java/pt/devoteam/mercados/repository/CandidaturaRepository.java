package pt.devoteam.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.mercados.entity.Candidatura;
import java.util.List;

@Repository
public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {

    // 🎯 CORREÇÃO: "FeiranteEmail" diz ao Spring para fazer JOIN com Feirante e procurar o email
    List<Candidatura> findByFeiranteEmail(String email);

    // Para a Câmara Municipal ver quem se inscreveu numa feira específica
    List<Candidatura> findByMercadoId(Long mercadoId);

    // 🎯 CORREÇÃO: Mesma lógica aplicada à validação de duplicação
    boolean existsByMercadoIdAndFeiranteEmail(Long mercadoId, String email);
}
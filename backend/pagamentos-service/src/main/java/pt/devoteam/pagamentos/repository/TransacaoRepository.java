package pt.devoteam.pagamentos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.devoteam.pagamentos.entity.Transacao;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    boolean existsByCandidaturaIdAndEstado(Long candidaturaId, String estado);
}
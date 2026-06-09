package pt.devoteam.mercados.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.devoteam.mercados.dto.RegistoDTO;
import pt.devoteam.mercados.entity.Feirante;
import pt.devoteam.mercados.repository.FeiranteRepository;

@Service
public class FeiranteService {
    private final FeiranteRepository feiranteRepository;

    public FeiranteService(FeiranteRepository feiranteRepository) {
        this.feiranteRepository = feiranteRepository;
    }

    /**
     * Sincroniza o feirante na base de dados local de forma segura e transacional.
     */
    @Transactional
    public void criarFeirante(RegistoDTO dto) {
        // Idempotência: Verifica de forma defensiva se já existe para evitar duplicações
        if (feiranteRepository.findByEmail(dto.getEmail()).isEmpty()) {
            Feirante novoFeirante = new Feirante();
            novoFeirante.setEmail(dto.getEmail());
            novoFeirante.setNome(dto.getNome());

            feiranteRepository.save(novoFeirante);
            System.out.println("🟢 [Serviço de Negócio] Feirante '" + dto.getNome() + "' criado no db_mercados!");

        } else {
            System.out.println("🟡 [Serviço de Negócio] O feirante com e-mail '" + dto.getEmail() + "' já existia. Sincronização ignorada.");
        }
    }
}
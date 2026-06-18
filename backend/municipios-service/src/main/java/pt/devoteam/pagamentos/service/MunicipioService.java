package pt.devoteam.pagamentos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.devoteam.pagamentos.entity.Municipio;
import pt.devoteam.pagamentos.repository.MunicipioRepository;

@Service
public class MunicipioService {

    private final MunicipioRepository municipioRepository;

    public MunicipioService(MunicipioRepository municipioRepository) {
        this.municipioRepository = municipioRepository;
    }

    /**
     * 🎯 Procura o perfil do município pelo e-mail.
     * Se não existir na tabela, cria um objeto transiente (esqueleto)
     * para o Angular carregar o ecrã sem rebentar erros de falta de dados.
     */
    @Transactional(readOnly = true)
    public Municipio obterOuCriarPerfilBase(String email) {
        return municipioRepository.findByEmail(email)
                .orElseGet(() -> {
                    Municipio novoEsqueleto = new Municipio();
                    novoEsqueleto.setEmail(email);
                    novoEsqueleto.setNomeCamara("Nova Autarquia Registada");
                    return novoEsqueleto;
                });
    }

    /**
     * 💾 Atualiza ou cria fisicamente os dados complementares da autarquia.
     */
    @Transactional
    public Municipio atualizarPerfil(Municipio dadosNovos) {
        Municipio municipioExistente = municipioRepository.findByEmail(dadosNovos.getEmail())
                .orElse(new Municipio());

        // Atualiza os campos fiscais e geográficos mapeados
        municipioExistente.setEmail(dadosNovos.getEmail());
        municipioExistente.setNomeCamara(dadosNovos.getNomeCamara());
        municipioExistente.setNifAutarquia(dadosNovos.getNifAutarquia());
        municipioExistente.setTelefoneOficial(dadosNovos.getTelefoneOficial());
        municipioExistente.setMoradaPacosConcelho(dadosNovos.getMoradaPacosConcelho());

        // Guarda as alterações de forma segura na tabela municipios_perfis
        return municipioRepository.save(municipioExistente);
    }
}
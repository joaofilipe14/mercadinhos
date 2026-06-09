package pt.devoteam.mercados.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.devoteam.mercados.dto.DashboardMunicipioDTO;
import pt.devoteam.mercados.entity.Mercado;
import pt.devoteam.mercados.entity.enums.EstadoMercado;
import pt.devoteam.mercados.repository.MercadoRepository;

import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

@Service
public class MercadoService {

    private final MercadoRepository mercadoRepository;

    public MercadoService(MercadoRepository mercadoRepository) {
        this.mercadoRepository = mercadoRepository;
    }

    /**
     * 🔓 Regra Pública: Procura apenas mercados aprovados para alimentar a vitrine.
     */
    @Transactional(readOnly = true)
    public List<Mercado> listarMercadosAprovados() {
        return mercadoRepository.findByEstado(EstadoMercado.APROVADO);
    }

    /**
     * 🏛️ Regra de Backoffice: Retorna todos os mercados (para a autarquia poder ver Pendentes/Cancelados)
     */
    @Transactional(readOnly = true)
    public List<Mercado> listarTodos() {
        return mercadoRepository.findAll();
    }


    @Transactional(readOnly = true)
    public List<Mercado> listarCriadosPor(String email) {
        return mercadoRepository.findByCriadoPor(email);
    }


    /**
     * 🎯 NOVO: Retorna os dados de um mercado específico para edição no Frontend
     */
    @Transactional(readOnly = true)
    public Optional<Mercado> obterMercado(Long id) {
        return mercadoRepository.findById(id);
    }

    /**
     * 🏛️ Regra Institucional: Processa a criação de feiras com base no nível de autoridade da Role.
     */
    @Transactional
    public Mercado criarMercado(Mercado mercado, String role, String email) {
        // Vincula o autor que submeteu o registo através do Gateway
        mercado.setCriadoPor(email);

        // A "Regra de Ouro" vive isolada e protegida na camada de negócio
        if ("ROLE_MUNICIPO".equals(role)) {
            mercado.setEstado(EstadoMercado.APROVADO); // Câmaras têm autoridade máxima imediata
        } else {
            mercado.setEstado(EstadoMercado.PENDENTE); // Juntas e Organizadores Privados aguardam validação
        }

        return mercadoRepository.save(mercado);
    }

    /**
     * ✏️ NOVO: Lógica centralizada para atualizar todos os campos do Mercado
     */
    @Transactional
    public Mercado atualizarMercado(Long id, Mercado mercadoAtualizado) {
        return mercadoRepository.findById(id).map(mercadoExistente -> {
            mercadoExistente.setNome(mercadoAtualizado.getNome());
            mercadoExistente.setLocalizacao(mercadoAtualizado.getLocalizacao());
            mercadoExistente.setDataInicio(mercadoAtualizado.getDataInicio());
            mercadoExistente.setDataFim(mercadoAtualizado.getDataFim());
            mercadoExistente.setVagas(mercadoAtualizado.getVagas());
            mercadoExistente.setEstado(mercadoAtualizado.getEstado());
            mercadoExistente.setDocumentosExigidos(mercadoAtualizado.getDocumentosExigidos());
            mercadoExistente.setLatitude(mercadoAtualizado.getLatitude());
            mercadoExistente.setLongitude(mercadoAtualizado.getLongitude());

            // 🎯 Sincronização dos Novos Campos Regulamentares
            mercadoExistente.setTipoPreco(mercadoAtualizado.getTipoPreco());
            mercadoExistente.setAceitaStreetFood(mercadoAtualizado.getAceitaStreetFood());
            mercadoExistente.setDisponibilizaStandsOrganizacao(mercadoAtualizado.getDisponibilizaStandsOrganizacao());

            mercadoExistente.setPrecoArtesanatoStandProprio(mercadoAtualizado.getPrecoArtesanatoStandProprio());
            mercadoExistente.setPrecoArtesanatoStandOrganizacao(mercadoAtualizado.getPrecoArtesanatoStandOrganizacao());
            mercadoExistente.setPrecoStreetFoodStandProprio(mercadoAtualizado.getPrecoStreetFoodStandProprio());
            mercadoExistente.setDescricao(mercadoAtualizado.getDescricao());
            mercadoExistente.setPetFriendly(mercadoAtualizado.isPetFriendly());
            mercadoExistente.setTemWc(mercadoAtualizado.isTemWc());
            mercadoExistente.setImagemCartaz(mercadoAtualizado.getImagemCartaz());
            return mercadoRepository.save(mercadoExistente);
        }).orElseThrow(() -> new RuntimeException("Mercado não encontrado com o ID: " + id));
    }

    public DashboardMunicipioDTO obterDadosDashboard(String emailMunicipio) {
        List<Mercado> mercadosDaAutarquia = mercadoRepository.findByCriadoPor(emailMunicipio);

        // 1. Soma das vagas disponíveis estendidas
        long vagasDisponiveis = mercadosDaAutarquia.stream()
                .mapToLong(Mercado::getVagas)
                .sum();

        // 2. 🪙 Cálculo Algorítmico da Faturação com base no Regulamento Oficial do Município
        double totalDinheiro = mercadosDaAutarquia.stream().mapToDouble(mercado -> {

            // 🎯 AJUSTE PARA LOCALDATE: Cálculo moderno e seguro usando ChronoUnit
            long diasDeFeira = 1;
            if (mercado.getDataInicio() != null && mercado.getDataFim() != null) {
                // Calcula a diferença em dias entre as duas datas locais e soma 1 (para incluir o dia inicial)
                diasDeFeira = java.time.temporal.ChronoUnit.DAYS.between(mercado.getDataInicio(), mercado.getDataFim()) + 1;

                if (diasDeFeira <= 0) diasDeFeira = 1;
            }

            // Passo B: Capturar os preçários reais configurados pela autarquia na BD
            double precoArtesanatoProprio = mercado.getPrecoArtesanatoStandProprio() != null ? mercado.getPrecoArtesanatoStandProprio() : 0.0;
            double precoArtesanatoOrg = mercado.getPrecoArtesanatoStandOrganizacao() != null ? mercado.getPrecoArtesanatoStandOrganizacao() : 0.0;
            double precoStreetFood = (mercado.getAceitaStreetFood() != null && mercado.getAceitaStreetFood() && mercado.getPrecoStreetFoodStandProprio() != null)
                    ? mercado.getPrecoStreetFoodStandProprio() : 0.0;

            // Se o modelo de faturação for Diário, multiplicamos os valores pelo número de dias do evento
            if ("DIARIO".equals(mercado.getTipoPreco())) {
                precoArtesanatoProprio *= diasDeFeira;
                precoArtesanatoOrg *= diasDeFeira;
                precoStreetFood *= diasDeFeira;
            }

            // Simulação de Auditoria de Inscrições:
            // - 2 Feirantes de Artesanato com banca própria
            // - 1 Feirante de Artesanato que alugou banca à Câmara (se disponível)
            // - 1 Feirante de Street Food/Roulotte (se a categoria estiver aberta)
            double faturacaoMercado = 0.0;

            faturacaoMercado += (precoArtesanatoProprio * 2);

            if (mercado.getDisponibilizaStandsOrganizacao() != null && mercado.getDisponibilizaStandsOrganizacao()) {
                faturacaoMercado += precoArtesanatoOrg;
            }

            if (mercado.getAceitaStreetFood() != null && mercado.getAceitaStreetFood()) {
                faturacaoMercado += precoStreetFood;
            }

            return faturacaoMercado;
        }).sum();

        // 3. Alertas Pendentes para o topo do Dashboard
        long pendentesAprovacao = 4;

        return new DashboardMunicipioDTO(totalDinheiro, pendentesAprovacao, vagasDisponiveis);
    }

    @Transactional(readOnly = true)
    public List<Mercado> listarMercadosProximos(double latUsuario, double lngUsuario, double raioKm) {
        List<Mercado> todosAprovados = listarMercadosAprovados();

        return todosAprovados.stream()
                .filter(mercado -> {

                    double distancia = calcularDistanciaHaversine(
                            latUsuario, lngUsuario,
                            mercado.getLatitude(), mercado.getLongitude()
                    );

                    // 🎯 Guardamos a distância calculada dentro do objeto (podes criar um campo Transient no modelo se quiseres,
                    // ou o Jackson envia se adicionares um método setDistancia no teu objeto Mercado)
                    mercado.setDistancia(distancia);

                    return distancia <= raioKm;
                })
                // Ordena do mais perto para o mais longe!
                .sorted((m1, m2) -> Double.compare(m1.getDistancia(), m2.getDistancia()))
                .toList();
    }

    /**
     * 🧮 Fórmula matemática de Haversine (Distância na esfera terrestre)
     */
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em KM

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Retorna a distância em Quilómetros
    }
}
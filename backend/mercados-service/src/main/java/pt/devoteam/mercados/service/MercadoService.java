package pt.devoteam.mercados.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.devoteam.mercados.dto.DashboardMunicipioDTO;
import pt.devoteam.mercados.entity.Mercado;
import pt.devoteam.mercados.entity.enums.EstadoMercado;
import pt.devoteam.mercados.entity.enums.TipoPreco;
import pt.devoteam.mercados.repository.MercadoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64; // 🎯 ADICIONADO PARA DESCODIFICAÇÃO NATIVA
import java.util.List;
import java.util.Optional;

@Service
public class MercadoService {

    private final MercadoRepository mercadoRepository;

    public MercadoService(MercadoRepository mercadoRepository) {
        this.mercadoRepository = mercadoRepository;
    }

    @Transactional(readOnly = true)
    public List<Mercado> listarMercadosAprovados() {
        List<Mercado> mercadosAprovados = mercadoRepository.findByEstado(EstadoMercado.APROVADO);
        mercadosAprovados.sort((m1, m2) -> {
            if (m1.getDataInicio() == null) return 1;
            if (m2.getDataInicio() == null) return -1;
            return m1.getDataInicio().compareTo(m2.getDataInicio());
        });
        return mercadosAprovados;
    }

    @Transactional(readOnly = true)
    public List<Mercado> listarTodos() {
        return mercadoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Mercado> listarCriadosPor(String email) {
        return mercadoRepository.findByCriadoPor(email);
    }

    @Transactional(readOnly = true)
    public Optional<Mercado> obterMercado(Long id) {
        return mercadoRepository.findById(id);
    }

    /**
     * 🎯 ATUALIZADO: Cria o mercado e extrai o payload Base64 para o disco físico se necessário
     */
    @Transactional
    public Mercado criarMercado(Mercado mercado, String role, String email) {
        mercado.setCriadoPor(email);

        if ("ROLE_MUNICIPO".equals(role)) {
            mercado.setEstado(EstadoMercado.APROVADO);
        } else {
            mercado.setEstado(EstadoMercado.PENDENTE);
        }

        // Primeiro gravamos para gerar o ID autonumérico do banco de dados
        Mercado mercadoSalvo = mercadoRepository.save(mercado);

        // Processa e extrai a imagem com base no ID definitivo
        processarESalvarCartazEmDisco(mercadoSalvo);

        return mercadoRepository.save(mercadoSalvo);
    }

    /**
     * 🎯 ATUALIZADO: Atualiza o regulamento e limpa/substitui o ficheiro de imagem no disco
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

            mercadoExistente.setTipoPreco(mercadoAtualizado.getTipoPreco());
            mercadoExistente.setAceitaStreetFood(mercadoAtualizado.getAceitaStreetFood());
            mercadoExistente.setDisponibilizaStandsOrganizacao(mercadoAtualizado.getDisponibilizaStandsOrganizacao());
            mercadoExistente.setPrecoArtesanatoStandProprio(mercadoAtualizado.getPrecoArtesanatoStandProprio());
            mercadoExistente.setPrecoArtesanatoStandOrganizacao(mercadoAtualizado.getPrecoArtesanatoStandOrganizacao());
            mercadoExistente.setPrecoStreetFoodStandProprio(mercadoAtualizado.getPrecoStreetFoodStandProprio());
            mercadoExistente.setDescricao(mercadoAtualizado.getDescricao());
            mercadoExistente.setPetFriendly(mercadoAtualizado.isPetFriendly());
            mercadoExistente.setTemWc(mercadoAtualizado.isTemWc());

            // Repassa o novo payload de imagem enviado pelo Angular
            mercadoExistente.setImagemCartaz(mercadoAtualizado.getImagemCartaz());

            // Processa o armazenamento físico de forma centralizada
            processarESalvarCartazEmDisco(mercadoExistente);

            return mercadoRepository.save(mercadoExistente);
        }).orElseThrow(() -> new RuntimeException("Mercado não encontrado com o ID: " + id));
    }

    /**
     * 🧰 MÉTODO UTILITÁRIO DE ELITE: Descodifica strings Base64 textuais em ficheiros binários reais de imagem
     */
    private void processarESalvarCartazEmDisco(Mercado mercado) {
        String payloadImagem = mercado.getImagemCartaz();

        // Se o campo estiver vazio ou se for apenas um link HTTP/Unsplash comum, ignoramos o processamento em disco
        if (payloadImagem == null || payloadImagem.trim().isEmpty() || payloadImagem.startsWith("http")) {
            return;
        }

        // Se o payload começar com o cabeçalho do FileReader do JavaScript (Ex: data:image/png;base64,...)
        if (payloadImagem.startsWith("data:image")) {
            try {
                // Split 1: Separa os metadados [0] ("data:image/png;base64") do corpo de dados [1]
                String[] partes = payloadImagem.split(",");
                String metadadosCabecalho = partes[0];
                String bytesBase64Puros = partes[1];

                // Deteta dinamicamente a extensão do ficheiro para não corromper o visual
                String extensao = "jpg";
                if (metadadosCabecalho.contains("image/png")) extensao = "png";
                else if (metadadosCabecalho.contains("image/webp")) extensao = "webp";
                else if (metadadosCabecalho.contains("image/gif")) extensao = "gif";

                // Converte a string de texto purificado de volta para bytes binários nativos
                byte[] imagemBytesOriginal = Base64.getDecoder().decode(bytesBase64Puros);

                // Configura de forma robusta a rota do diretório no servidor
                // 📁 Diretório físico isolado para armazenar as imagens promocionais das feiras
                String CARTAZES_DIR = "uploads/cartazes/";
                Path rotaDiretorio = Paths.get(CARTAZES_DIR).toAbsolutePath().normalize();
                if (!Files.exists(rotaDiretorio)) {
                    Files.createDirectories(rotaDiretorio);
                }

                // Nomeia o ficheiro de forma padronizada com base no ID do registo
                String nomeFicheiro = "cartaz_mercado_" + mercado.getId() + "." + extensao;
                Path caminhoFicheiroCompleto = rotaDiretorio.resolve(nomeFicheiro);

                // Escreve fisicamente a imagem no disco do servidor
                Files.write(caminhoFicheiroCompleto, imagemBytesOriginal);

                System.out.println("🟢 [Storage] Cartaz convertido e salvo fisicamente em: " + caminhoFicheiroCompleto);

                // Altera a propriedade da entidade para guardar apenas o caminho amigável relativo
                mercado.setImagemCartaz("uploads/cartazes/" + nomeFicheiro);

            } catch (IOException | IllegalArgumentException e) {
                System.err.println("🔴 Erro crítico ao processar e salvar a imagem do cartaz em disco: " + e.getMessage());
                // Fallback preventivo para não perder o formulário por falhas de escrita
                mercado.setImagemCartaz("");
            }
        }
    }

    public DashboardMunicipioDTO obterDadosDashboard(String emailMunicipio) {
        List<Mercado> mercadosDaAutarquia = mercadoRepository.findByCriadoPor(emailMunicipio);

        long vagasDisponiveis = mercadosDaAutarquia.stream()
                .mapToLong(Mercado::getVagas)
                .sum();

        double totalDinheiro = mercadosDaAutarquia.stream().mapToDouble(mercado -> {
            long diasDeFeira = 1;
            if (mercado.getDataInicio() != null && mercado.getDataFim() != null) {
                diasDeFeira = java.time.temporal.ChronoUnit.DAYS.between(mercado.getDataInicio(), mercado.getDataFim()) + 1;
                if (diasDeFeira <= 0) diasDeFeira = 1;
            }

            double precoArtesanatoProprio = mercado.getPrecoArtesanatoStandProprio() != null ? mercado.getPrecoArtesanatoStandProprio() : 0.0;
            double precoArtesanatoOrg = mercado.getPrecoArtesanatoStandOrganizacao() != null ? mercado.getPrecoArtesanatoStandOrganizacao() : 0.0;
            double precoStreetFood = (mercado.getAceitaStreetFood() != null && mercado.getAceitaStreetFood() && mercado.getPrecoStreetFoodStandProprio() != null)
                    ? mercado.getPrecoStreetFoodStandProprio() : 0.0;

            if (TipoPreco.DIARIO.equals(mercado.getTipoPreco())) {
                precoArtesanatoProprio *= diasDeFeira;
                precoArtesanatoOrg *= diasDeFeira;
                precoStreetFood *= diasDeFeira;
            }

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

        long pendentesAprovacao = 4;
        return new DashboardMunicipioDTO(totalDinheiro, pendentesAprovacao, vagasDisponiveis);
    }

    @Transactional(readOnly = true)
    public Page<Mercado> listarMercadosProximos(double latUsuario, double lngUsuario, double raioKm, Pageable pageable) {
        List<Mercado> todosAprovados = listarMercadosAprovados();

        List<Mercado> filtrados = todosAprovados.stream()
                .filter(mercado -> {
                    double distancia = calcularDistanciaHaversine(
                            latUsuario, lngUsuario,
                            mercado.getLatitude(), mercado.getLongitude()
                    );
                    mercado.setDistancia(distancia);
                    return distancia <= raioKm;
                })
                .sorted((m1, m2) -> Double.compare(m1.getDistancia(), m2.getDistancia()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtrados.size());

        if (start > filtrados.size()) {
            return new PageImpl<>(List.of(), pageable, filtrados.size());
        }

        return new PageImpl<>(filtrados.subList(start, end), pageable, filtrados.size());
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
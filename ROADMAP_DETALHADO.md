# 🗺️ ROADMAP_DETALHADO.md: Plano de Implementação Feature-by-Feature

Este plano foi desenhado para entregas diárias verticais (*User Stories completas*), garantindo que cada dia termina com uma funcionalidade demonstrável de ponta a ponta (Base de Dados -> Java -> Gateway -> Angular).

---

## 📅 DIA 1: Arrumar a Camada de Dados (Fase 2 - Fecho)
**Objetivo:** Garantir que todos os microsserviços têm a sua persistência validada com Testcontainers automáticos antes de avançar para as regras de negócio.

* **Passo 1 (`feirantes-service`):**
    * Executar o teste `CandidaturaRepositoryTest.java` no IntelliJ. Com o Lombok já configurado globalmente no POM pai, o teste deve passar imediatamente (Verde 🟢).
* **Passo 2 (`cidadaos-service`):**
    * Criar a interface `MercadoConsultaRepository.java` em `pt.devoteam.cidadaos.repository`.
    * Implementar a Query Nativa com a fórmula de Haversine para filtrar por raio de KM:
        ```java
        @Query(value = "SELECT * FROM mercados_leitura m WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(m.latitude)) * cos(radians(m.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(m.latitude)))) <= :raio", nativeQuery = true)
        List<MercadoLeitura> buscarPorProximidade(@Param("lat") double lat, @Param("lng") double lng, @Param("raio") double raioKm);
        ```
    * Executar o `MercadoConsultaRepositoryTest.java` no IntelliJ e garantir que a distância entre Lisboa e Porto é calculada perfeitamente.
* **Demonstração do Dia:** Executar `mvn clean package` na raiz do pai e ver os testes de todos os módulos passarem com sucesso.

---

## 📅 DIA 2: Segurança Core - Autenticação JWT & Perfis (Roles)
**Objetivo:** Proteger o ecossistema com login stateless e definir quem pode aceder a cada funcionalidade.

* **Passo 1 (Backend - `webapp` Gateway):**
    * Adicionar as dependências do `spring-boot-starter-security` e `jjwt` no API Gateway.
    * Criar o endpoint `POST /api/auth/login`. Para a PoC, usar utilizadores mockados em memória para acelerar o desenvolvimento:
        * `camara@test.com` / `password` -> Role: `ROLE_MUNICIPO`
        * `feirante@test.com` / `password` -> Role: `ROLE_FEIRANTE`
    * Configurar o `SecurityWebFilterChain` para validar o cabeçalho `Authorization: Bearer <TOKEN>` e extrair as Claims.
* **Passo 2 (Frontend - Angular):**
    * Criar o `AuthService` usando Angular **Signals** para expor de forma reativa o estado do utilizador atual (`currentUser = signal(...)`).
    * Implementar o ecrã básico de Login (`LoginComponent`).
    * Criar um `AuthInterceptor` para anexar automaticamente o token JWT em todos os pedidos HTTP direcionados à porta 8080.
* **Demonstração do Dia:** Fazer login com a conta da câmara, receber o token no painel Network do browser e verificar que o utilizador é reconhecido com o perfil correto.

---

## 📅 DIA 3: Feature 1 - O Fluxo das Câmaras (Criar Mercados)
**Objetivo:** Permitir a gestão e publicação de novos mercados municipais.

* **Passo 1 (Backend - `camaras-service`):**
    * *TDD Unitário:* Criar `MercadoServiceTest` para barrar a criação se a data de fim for anterior à de início ou se as vagas forem menores ou iguais a zero.
    * *TDD API:* Escrever teste `MockMvc` para `POST /api/mercados` validando o payload JSON.
    * *Implementação:* Codificar o `MercadoService` e `MercadoController`. Proteger o endpoint com `@PreAuthorize("hasRole('MUNICIPO')")`.
* **Passo 2 (Gateway & Frontend):**
    * Mapear no `application.yml` do Gateway o encaminhamento de `/api/mercados/**` para a porta 8081.
    * No Angular, criar o formulário reativo `CriarMercadoComponent` (Validações visuais, campos de vagas, coordenadas Lat/Lng com Tailwind CSS).
* **Demonstração do Dia:** Autenticar como funcionário da Câmara, preencher o formulário, submeter, e ver a linha inserida em tempo real na tabela `db_camaras`.

---

## 📅 DIA 4: Feature 2 - O Fluxo dos Feirantes (Inscrição com PDF)
**Objetivo:** Permitir que os feirantes se candidatem às vagas dos mercados anexando documentação legal.

* **Passo 1 (Backend - `feirantes-service`):**
    * *TDD Unitário:* Criar testes para validar o upload. O arquivo tem de ser obrigatoriamente um PDF e ter tamanho máximo de 5MB.
    * *TDD API:* Criar teste `MockMvc` simulando o envio multi-part (`MultipartFile`).
    * *Implementação:* Codificar o `CandidaturaService` (armazenar o arquivo localmente numa pasta do container) e o `CandidaturaController` protegido por `hasRole('FEIRANTE')`.
* **Passo 2 (Gateway & Frontend):**
    * Mapear as rotas `/api/candidaturas/**` para a porta 8082 no Gateway.
    * No Angular, criar o componente `InscreverFeiranteComponent` contendo a listagem de mercados e o botão de upload de arquivos (`FormData`).
* **Demonstração do Dia:** Entrar com a conta de feirante, fazer o upload de um PDF real e verificar o registo no banco e o arquivo guardado no diretório do microsserviço.

---

## 📅 DIA 5: Feature 3 - O Fluxo do Cidadão (Geolocalização Ativa)
**Objetivo:** Disponibilizar ao público a consulta inteligente e de proximidade de mercados.

* **Passo 1 (Backend - `cidadaos-service`):**
    * *TDD API:* Escrever teste `MockMvc` para o endpoint `GET /api/consultas/proximidade?lat=X&lng=Y&raio=Z`.
    * *Implementação:* Criar o `ConsultaController` injetando o repositório matemático que calcula a proximidade no Postgres.
* **Passo 2 (Gateway & Frontend):**
    * Mapear as rotas de consulta para a porta 8083.
    * No Angular, criar o ecrã público `ProcurarMercadosComponent`.
    * **Showcase Impact:** Injetar a API nativa do browser `navigator.geolocation.getCurrentPosition()` para capturar a latitude e longitude reais de onde estás a apresentar e sugerir os mercados criados num raio ajustável (Ex: slider de 10km, 50km, 100km).
* **Demonstração do Dia:** Abrir o ecrã como utilizador anónimo, permitir a geolocalização e ver o sistema listar os mercados mais próximos em relação à tua cadeira.

---

## 📅 DIA 6: Diferenciais Enterprise - Eventos & Resiliência (Showcase Final)
**Objetivo:** Impressionar os avaliadores demonstrando resiliência arquitetural de sistemas distribuídos complexos.

* **Passo 1 (Comunicação Event-Driven - Sincronização):**
    * Implementar um evento simples (via HTTP síncrono ou WebClient) para que, no momento em que a câmara criar um mercado com sucesso no `camaras-service`, uma cópia simplificada dos dados (ID, Nome, Lat, Lng) seja enviada e guardada na tabela `mercados_leitura` do `cidadaos-service`.
    * *Isto demonstra o conceito real de segregação de bases de dados e CQRS.*
* **Passo 2 (Resiliência com Circuit Breaker):**
    * Configurar o **Resilience4j** nas rotas do Gateway (`webapp`).
    * Definir um *fallback* amigável caso o `feirantes-service` fique indisponível.
* **Demonstração do Caos (A Apresentação Perfeita):**
    * Durante a apresentação, vais ao terminal e derrubas de propósito o container do feirante: `docker stop feirantes-service`.
    * Acedes ao ecrã do feirante no Angular. Em vez de a aplicação rebentar com um erro cinzento ou erro 500, o Gateway ativa o Circuit Breaker e mostra uma mensagem elegante na tela: *"O sistema de inscrições está em manutenção técnica programada. Os seus dados estão seguros."*

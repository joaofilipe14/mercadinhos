# PoC: Sistema de Gestão de Mercados e Feirantes (Enterprise Edition)

## Arquitetura do Sistema
- **`frontend`**: Aplicação SPA desenvolvida em Angular (Versão mais recente com Standalone Components & Signals).
- **`webapp` (API Gateway)**: O ponto de entrada único em Java/Spring que faz o roteamento inteligente para os microsserviços (porta 8080).
- **`camaras-service`**: Gestão de mercados (Criação, datas, vagas, editais) (porta 8081).
- **`feirantes-service`**: Gestão de perfis e submissão de candidaturas (upload de PDFs) (porta 8082).
- **`cidadaos-service`**: Consulta de mercados ativos com filtro geográfico ("perto de mim") (porta 8083).

## Stack Tecnológica
- Java 21
- Spring Boot 4.0 (Configuração moderna com @ServiceConnection)
- Spring Cloud (Gateway MVC & Gestão de dependências)
- Angular (Versão mais recente - Standalone)
- Base de Dados: PostgreSQL 16 (Isoladas via Docker)
- Testes (TDD): JUnit 5, AssertJ, Testcontainers, Spring MockMvc
- ORM: Spring Data JPA / Hibernate
- Utilitários: Lombok 1.18.34 (Compatível com Java 21+)
- Orquestração/Deploy: Docker & Kubernetes (K8s)

---

## Fases de Execução TDD (Red -> Green -> Refactor)

### Fase 1: Esqueleto e Infraestrutura de Base [x]
- [x] Criar `pom.xml` pai (Multi-module) com dependências do Spring Boot e Spring Cloud.
- [x] Resolver erros de herança do Maven e caminhos relativos (`<relativePath>`).
- [x] Solucionar o conflito do compilador Java 21 vs Lombok usando a versão `1.18.34`.
- [x] Criar os poms e a estrutura de pacotes correta (`pt.devoteam.camaras`) para os 4 módulos Java.
- [x] Criar o ficheiro `docker-compose.yml` na raiz com 3 instâncias independentes de PostgreSQL.
- [x] Configurar os ficheiros `application.yml` para ligação autónoma às portas `5432`, `5433` e `5434`.
- [x] Validar que o projeto compila na totalidade com `BUILD SUCCESS`.

### Fase 2: TDD - Camada de Dados (Repositories & Entities) [/]
- [x] **`camaras-service`**:
    - [x] *Test:* Escrever teste de integração moderno usando a abordagem Spring Boot 4.0 (`@ServiceConnection` + Testcontainers).
    - [x] *Code:* Criar Entidade `Mercado` e a interface `MercadoRepository`.
    - [x] *Validar:* Teste executado com sucesso (**DEU VERDE!** 🟢).
- [ ] **`feirantes-service`**:
    - [ ] *Test:* Escrever teste de integração com Testcontainers para submeter e persistir uma Candidatura de feirante.
    - [ ] *Code:* Criar Entidades `Feirante`, `Candidatura` e respetivos Repositórios.
- [ ] **`cidadaos-service`**:
    - [ ] *Test:* Escrever teste para a query de pesquisa nativa/geográfica.
    - [ ] *Code:* Criar modelo de leitura e repositório de consulta rápida.

### Fase 3: TDD - Lógica de Negócio (Services) [ ]
- [ ] **`camaras-service`**:
    - [ ] *Test:* Escrever testes unitários (com Mockito) para as regras de validação de datas e vagas de mercados.
    - [ ] *Code:* Implementar `MercadoService`.
- [ ] **`feirantes-service`**:
    - [ ] *Test:* Escrever testes unitários para a validação do formato e tamanho dos PDFs das candidaturas.
    - [ ] *Code:* Implementar `CandidaturaService`.
- [ ] **`cidadaos-service`**:
    - [ ] *Test:* Testar matematicamente a fórmula de Haversine (Raio de distância em KM).
    - [ ] *Code:* Implementar `LocalizacaoService`.

### Fase 4: TDD - Exposição de APIs (Controllers) [ ]
- [ ] Escrever testes de API usando `MockMvc` para todos os endpoints antes de os implementar (Abordagem REST).
- [ ] Implementar os REST Controllers nos microsserviços.
- [ ] Garantir o sucesso do endpoint de upload de `MultipartFile` (PDF) no `feirantes-service`.

### Fase 5: API Gateway & Frontend (Angular) [ ]
- [ ] Configurar as rotas no `webapp` (Spring Cloud Gateway) para reencaminhar os pedidos do Frontend para as portas `8081`, `8082` e `8083`.
- [ ] Gerar o projeto Frontend Angular na pasta `/frontend`.
- [ ] Desenvolver os ecrãs e serviços em Angular consumindo o Gateway (porta 8080).

### Fase 6: Containerização e Orquestração (Docker / Kubernetes) [ ]
- [ ] Criar o `Dockerfile` otimizado (Multi-stage build) para os microsserviços e para o app Angular.
- [ ] Escrever os manifestos Kubernetes (`Deployment`, `Service`, `ConfigMap`, `Secret`) na pasta `/k8s`.
- [ ] Testar o deploy local num cluster Kubernetes (Minikube / Kind).
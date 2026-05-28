# PoC: Sistema de Gestão de Mercados e Feirantes (Enterprise Edition)

## Arquitetura do Sistema
- **`api-gateway` / `webapp`**: O ponto de entrada da aplicação (Front-end e roteamento para os microsserviços).
- **`camaras-service`**: Gestão de mercados (Criação, datas, vagas, editais).
- **`feirantes-service`**: Gestão de perfis e submissão de candidaturas (upload de PDFs).
- **`cidadaos-service`**: Consulta de mercados ativos com filtro geográfico ("perto de mim").

## Stack Tecnológica
- Java 21 (ou superior)
- Spring Boot 4.x
- Spring Cloud (Gateway & Gestão de dependências)
- Base de Dados: PostgreSQL
- Testes (TDD): JUnit 5, Mockito, Testcontainers, Spring MockMvc
- ORM: Spring Data JPA / Hibernate
- Utilitários: Lombok
- Orquestração/Deploy: Docker & Kubernetes (K8s)

---

## Fases de Execução TDD (Red -> Green -> Refactor)

### Fase 1: Esqueleto e Infraestrutura de Base [ ]
- [ ] Criar `pom.xml` pai (Multi-module) com dependências do Spring Boot 4.x e Spring Cloud.
- [ ] Criar os 4 módulos (`webapp`, `camaras-service`, `feirantes-service`, `cidadaos-service`).
- [ ] Criar o ficheiro `docker-compose.yml` na raiz para subir 3 instâncias de PostgreSQL (uma para cada microsserviço).
- [ ] Configurar os `application.yml` para ligação ao PostgreSQL local.

### Fase 2: TDD - Camada de Dados (Repositories & Entities) [ ]
- [ ] **`camaras-service`**:
    - *Test:* Escrever teste de integração com Testcontainers para gravar/ler um Mercado.
    - *Code:* Criar Entidade `Mercado` e `MercadoRepository`.
- [ ] **`feirantes-service`**:
    - *Test:* Escrever teste de integração para submeter uma Candidatura.
    - *Code:* Criar Entidades `Feirante`, `Candidatura` e Repositórios.
- [ ] **`cidadaos-service`**:
    - *Test:* Escrever teste para a query de pesquisa geográfica.
    - *Code:* Criar view/modelo de leitura do mercado.

### Fase 3: TDD - Lógica de Negócio (Services) [ ]
- [ ] **`camaras-service`**:
    - *Test:* Escrever testes unitários (com Mockito) para as regras de validação de datas e vagas de mercados.
    - *Code:* Implementar `MercadoService`.
- [ ] **`feirantes-service`**:
    - *Test:* Escrever testes unitários para a validação do formato e tamanho dos PDFs das candidaturas.
    - *Code:* Implementar `CandidaturaService`.
- [ ] **`cidadaos-service`**:
    - *Test:* Testar matematicamente a fórmula de Haversine (Raio de distância em KM).
    - *Code:* Implementar `LocalizacaoService`.

### Fase 4: TDD - Exposição de APIs (Controllers) [ ]
- [ ] Escrever testes de API usando `MockMvc` para todos os endpoints antes de os implementares.
- [ ] Implementar os REST Controllers.
- [ ] Garantir o sucesso do endpoint de upload de `MultipartFile` no `feirantes-service`.

### Fase 5: A "App" e Gateway [ ]
- [ ] Configurar o Spring Cloud Gateway para reencaminhar pedidos (Ex: `/api/mercados/**` para o `camaras-service`).
- [ ] Desenvolver a interface visual da App para os 3 perfis de utilizador consumirem os serviços.

### Fase 6: Containerização e Orquestração (Docker / Kubernetes) [ ]
- [ ] Criar o `Dockerfile` otimizado (Multi-stage build) para cada um dos 4 microsserviços.
- [ ] Escrever os manifestos Kubernetes (`Deployment`, `Service`, `ConfigMap`, `Secret` para o Postgres) na pasta `/k8s`.
- [ ] Testar o deploy local usando Minikube ou Docker Desktop (Kubernetes ativo).
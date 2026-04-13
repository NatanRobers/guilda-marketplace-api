# Documentação do Projeto "Guilda" (Registro Oficial)

## Visão geral
Este projeto é uma API REST em **Java 17** com **Spring Boot 3.2.4**, organizada no padrão **Controller → Service → Repository → Banco**. Ele também possui:

- **Cache** com Spring Cache (para acelerar consultas específicas).
- **Elasticsearch** (para buscas e agregações de produtos no marketplace).

## Bibliotecas e por que elas existem
As dependências estão em `pom.xml`.

- **spring-boot-starter-web**: cria endpoints REST (`@RestController`), serialização JSON, etc.
- **spring-boot-starter-validation**: validação de DTOs com Bean Validation (`@Valid`, constraints).
- **spring-boot-starter-data-jpa**: persistência com JPA/Hibernate, repositórios Spring Data, Specifications, paginação.
- **spring-boot-starter-cache**: infraestrutura do Spring Cache (`@EnableCaching`, `@Cacheable`, etc).
- **co.elastic.clients:elasticsearch-java (8.12.2)**: cliente oficial do Elasticsearch (API tipada) usado para search/aggregations.
- **jackson-databind**: serialização/desserialização JSON (já vem no starter web, mas está explícito no POM).
- **postgresql**: driver JDBC do PostgreSQL (runtime).
- **lombok**: reduz boilerplate (getters/setters/constructors) quando utilizado nas classes.
- **spring-boot-starter-test (test)**: JUnit + stack de testes do Spring.

## Organização de pastas (src/main)
Estrutura principal:

- `src/main/java/com/guilda/registro/`
  - `GuildaApplication.java`: bootstrap do Spring Boot e ativação do cache (`@EnableCaching`).
  - `controller/`: camada HTTP (endpoints, parâmetros, status codes).
  - `service/`: regras de negócio e orquestração (chama repositories, integra integrações).
  - `repository/`: acesso a dados (Spring Data JPA e queries).
  - `domain/`: entidades e enums do domínio (ex.: `aventura/`, `enums/` e `legacy/`).
  - `dto/`: objetos de entrada/saída dos endpoints.
  - `mapper/`: conversão entre entidade ↔ DTO (ex.: `MissaoMapper`, `AventureiroMapper`).
  - `exception/`: tratamento centralizado de erros (`@RestControllerAdvice`).

Recursos:

- `src/main/resources/application.properties`: configurações (Postgres, Elasticsearch).
- `src/main/resources/data.sql`: carga inicial de dados (habilitada por `spring.sql.init.mode=always`).

Observação:

- `target/` é diretório gerado pelo build (Maven). Normalmente **não deve ser versionado** (idealmente entra no `.gitignore`).

## Fluxo do código (do request até o dado)
Padrão geral do projeto:

1. **Controller** recebe o HTTP request e extrai parâmetros (`@RequestParam`, `@PathVariable`).
2. **Service** aplica regra de negócio e chama:
   - **Repository (JPA)** para dados relacionais (Postgres), usando paginação e filtros (Specifications).
   - **ElasticsearchClient** para busca/analytics no índice do marketplace.
3. **Mapper/DTOs** transformam entidade em formato de resposta.
4. **ExceptionHandler** intercepta erros comuns e padroniza respostas.

Exemplos concretos:

- **Missões**
  - `MissaoController` → `MissaoService`
  - O service monta filtros via `MissaoSpecifications.comFiltros(...)`
  - O repositório executa `findAll(spec, pageable)` e mapeia para `MissaoDetalhadaResponse`

- **Painel tático**
  - `PainelTaticoController` → `PainelTaticoService` → `PainelTaticoRepository`
  - A lista “top missões” é cacheada (ver seção de cache)

- **Marketplace (Elasticsearch)**
  - `MarketplaceController` → `MarketplaceService`
  - O service faz `search` e `aggregations` no índice `guilda_loja`

## Cache: como foi implementado
O cache foi implementado com **Spring Cache**:

- **Ativação global**: `@EnableCaching` em `GuildaApplication.java`.
- **Uso pontual**: `PainelTaticoService.buscarTopMissoesRecentes()` está anotado com:
  - `@Cacheable(value = "topMissoes")`

O que isso significa na prática:

- Na **primeira chamada**, o método executa e o resultado é armazenado no cache `"topMissoes"`.
- Nas **chamadas seguintes**, o Spring retorna o valor cacheado e **não re-executa** a consulta do repositório (enquanto a entrada estiver no cache).

Detalhes importantes desta implementação:

- **Chave do cache**: como o método não tem parâmetros, a chave efetiva é única (uma entrada para `"topMissoes"`).
- **Provider do cache**: não há dependência explícita de Redis/Caffeine/Ehcache no `pom.xml`. Então, por padrão, o Spring Boot usa um cache **em memória** (implementação simples via `ConcurrentMapCache`) salvo se existir configuração/bean customizado no projeto.
- **TTL/expiração**: não há configuração de tempo de expiração no projeto. Assim, o comportamento de expiração depende do provider; no cache simples em memória, em geral **não há TTL** automático.

## Elasticsearch: como foi feito
O Elasticsearch foi integrado com o **cliente oficial** `elasticsearch-java`:

- **Configuração de URL**: em `src/main/resources/application.properties`:
  - `spring.elasticsearch.uris=http://localhost:9200`

Com isso, o Spring Boot auto-configura um `ElasticsearchClient` (bean) e ele é injetado no service:

- `MarketplaceService(ElasticsearchClient esClient)`

### Índice e operações implementadas
O `MarketplaceService` usa o índice:

- `INDEX = "guilda_loja"`

Operações disponíveis:

- **Busca textual**
  - Match por `nome` (`buscaPorNome`)
  - Match por `descricao` (`buscaPorDescricao`)
  - Phrase match em `descricao` (`buscaFraseExataDescricao`)
  - Fuzzy em `nome` (`buscaFuzzyNome`)
  - Multi-match em `nome` + `descricao` (`buscaMultiCampos`)

- **Filtros**
  - Filtro por categoria (usando `categoria.keyword`) (`buscaComFiltroCategoria`)
  - Faixa de preço (range em `preco`) (`buscaFaixaPreco`)
  - Busca avançada combinando categoria/raridade/preço (`buscaAvancada`)

- **Agregações**
  - Termos por categoria (`agregarPorCategoria`)
  - Termos por raridade (`agregarPorRaridade`)
  - Média de preço (`agregarPrecoMedio`)
  - Range por faixas de preço (`agregarFaixasPreco`)

Observação importante:

- As agregações do marketplace agora retornam **DTOs simples** (ex.: `List<TermoContagemDTO>`, `PrecoMedioDTO`, `List<FaixaPrecoDTO>`) em vez de expor o `SearchResponse` do Elasticsearch na API.

### Como a API expõe isso
Endpoints principais do marketplace ficam em:

- `MarketplaceController` com base `/produtos`
  - `/produtos/busca/*` para buscas
  - `/produtos/agregacoes/*` para agregações

### Teste de integração
Existe um teste simples de conectividade:

- `src/test/java/com/guilda/registro/MarketplaceIntegrationTest.java`
  - faz `elasticsearchClient.ping()` e espera `true` (pressupõe Elasticsearch rodando localmente, por exemplo via Docker).

## Execução local (resumo de requisitos)
- **PostgreSQL** configurado em `application.properties` (host `127.0.0.1`, porta `5432`, user/pass `postgres`).
- **Elasticsearch** em `http://localhost:9200` (conforme `spring.elasticsearch.uris`).

## Endpoints principais (resumo prático)

### Aventureiros (`/aventureiros`)

- **GET** `/aventureiros`
  - Filtros: `ativo` (Boolean), `classe` (ClasseAventureiro), `nivelMinimo` (Integer), `page`, `size`, `sortBy`

- **GET** `/aventureiros/busca?nome=...`

- **GET** `/aventureiros/{id}`
  - Retorna: `AventureiroDetalhadoResponse`

- **POST** `/aventureiros`
  - Body: `AventureiroCreateRequest`

```json
{
  "organizacaoId": 1,
  "usuarioResponsavelId": 1,
  "aventureiro": {
    "nome": "Aragorn",
    "classe": "GUERREIRO",
    "nivel": 5
  }
}
```

- **PUT** `/aventureiros/{id}`
  - Body: `AventureiroRequest`

```json
{
  "nome": "Aragorn Elessar",
  "classe": "GUERREIRO",
  "nivel": 6
}
```

- **POST** `/aventureiros/{id}/recrutar`
  - Reativa o aventureiro (`ativo=true`)

- **POST** `/aventureiros/{id}/companheiro`
  - Body: `CompanheiroRequest` (cria/atualiza o companheiro do aventureiro)

```json
{
  "nome": "Sombra",
  "especie": "LOBO",
  "lealdade": 80
}
```

- **DELETE** `/aventureiros/{id}/companheiro`

### Missões (`/missoes`)

- **GET** `/missoes`
  - Filtros: `status` (StatusMissao), `perigo` (NivelPerigo), `inicio` (OffsetDateTime ISO), `termino` (OffsetDateTime ISO), `page`, `size`
  - `StatusMissao`: `PLANEJADA`, `EM_ANDAMENTO`, `CONCLUIDA`, `CANCELADA`

- **GET** `/missoes/{id}`

- **GET** `/missoes/relatorio`

- **POST** `/missoes`
  - Body: `MissaoCreateRequest`

```json
{
  "organizacaoId": 1,
  "titulo": "Caçar o dragão",
  "nivelPerigo": "ALTO",
  "status": "PLANEJADA",
  "dataInicio": "2026-04-13T12:00:00Z",
  "dataTermino": null
}
```

- **DELETE** `/missoes/{id}`

- **DELETE** `/missoes/{missaoId}/participacoes/{aventureiroId}`
  - Remove o vínculo de participação do aventureiro na missão (deleta a linha de `participacao_missao` pela chave composta).

### Participações (`/participacoes`)

- **POST** `/participacoes`

```json
{
  "missaoId": 1,
  "aventureiroId": 1,
  "papel": "LIDER"
}
```



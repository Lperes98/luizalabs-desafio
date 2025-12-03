# LuizaLabs - Desafio Técnico Vertical Logística

Sistema de processamento e normalização de pedidos desenvolvido em **Java 21** com **Spring Boot 3.4.1**, utilizando **Clean Architecture** e **PostgreSQL**.

---

## Índice

- [Sobre o Desafio](#sobre-o-desafio)
- [Solução Proposta](#solução-proposta)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Como Executar](#como-executar)
- [Endpoints da API](#endpoints-da-api)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Build e Testes](#build-e-testes)
- [Trade-offs e Decisões](#trade-offs-e-decisões)

---

## Sobre o Desafio

Integração entre um sistema legado que gera arquivos de pedidos desnormalizados (largura fixa) e um sistema moderno que consome dados normalizados em JSON.

### Requisitos

1. **API REST** para upload de arquivo de largura fixa
2. **Processamento** do arquivo desnormalizado → JSON normalizado
3. **API REST** para consulta de pedidos com filtros:
   - Por ID do pedido
   - Por intervalo de datas (data início e data fim)

### Formato de Entrada (Largura Fixa)

| Campo | Tamanho | Tipo | Posição |
|-------|---------|------|---------|
| user_id | 10 | numérico | 0-9 |
| name | 45 | texto | 10-54 |
| order_id | 10 | numérico | 55-64 |
| product_id | 10 | numérico | 65-74 |
| value | 12 | decimal | 75-86 |
| date | 8 | numérico (yyyymmdd) | 87-94 |

**Observação:** Campos numéricos preenchidos com '0' à esquerda, demais com espaço à esquerda.

---

## Solução Proposta

### Abordagem: **Clean Architecture com Stream Processing**

Desenvolvi uma API REST em Java 21 com Spring Boot 3.4.1 utilizando Clean Architecture. A solução é capaz de:

1. Receber arquivos de largura fixa via upload
2. Processar linha a linha usando Stream (para eficiência de memória)
3. Agregar os dados em estrutura normalizada (User -> Orders -> Products)
4. Persistir no PostgreSQL utilizando JSONB
5. Consultar pedidos com filtros (por orderId ou intervalo de datas)
6. Registrar logs de processamento com detalhes de erros

#### Por que essa escolha?

1. **Clean Architecture**: Separação clara de responsabilidades (Domain, Use Cases, Adapters)
2. **Stream Processing**: Processa arquivos linha a linha sem carregar tudo na memória
3. **PostgreSQL com JSONB**: Flexibilidade do JSON + performance de queries relacionais
4. **Testabilidade**: Arquitetura permite testes unitários isolados

---

## Arquitetura


### Clean Architecture

O projeto segue a Clean Architecture.
A ideia central é manter o domínio isolado de frameworks e detalhes de infraestrutura:

```
+-----------------------------------------------------+
|                  app/api (API REST)                 |
|     Controllers | DTOs | Exception Handlers         |
+-------------------------+---------------------------+
                          | depende de
+-------------------------v---------------------------+
|              core/use-case (Casos de Uso)           |
|  ProcessFileUseCase | QueryOrdersUseCase | Parser   |
+-------------------------+---------------------------+
                          | depende de
+-------------------------v---------------------------+
|              core/domain (Entidades Puras)          |
|     User | Order | Product | Exceptions | Ports     |
+-------------------------+---------------------------+
                          | implementa
+-------------------------v---------------------------+
|        adapter/data-provider (Infraestrutura)       |
|   JPA Repositories | PostgreSQL | Flyway Migrations |
+-----------------------------------------------------+
```

Os benefícios dessa abordagem:
- O Domain não conhece frameworks, então é testável isoladamente
- Fica fácil trocar PostgreSQL por outro banco se necessário
- Os use cases são testáveis com mocks simples
- Violações de arquitetura viram erros de compilação (por causa do multi-módulo)

### Princípios SOLID

| Princípio | Aplicação no Projeto |
|-----------|----------------------|
| Single Responsibility | `FileParserService` só parseia, `DataAggregatorService` só agrega |
| Open/Closed | Posso adicionar novas portas de saída sem alterar use cases |
| Liskov Substitution | Qualquer implementação de `OrderDataPort` pode substituir outra |
| Interface Segregation | `ProcessFileUseCase` e `QueryOrdersUseCase` são interfaces separadas |
| Dependency Inversion | Use cases dependem de Ports (interfaces), não de implementações |

---

### Padrões de Projeto Utilizados

| Padrão | Onde | Por quê |
|--------|------|---------|
| Repository | `OrderDataPort`, `FileProcessingLogPort` | Abstrai a persistência |
| Adapter | `OrderDataAdapter`, `FileProcessingLogAdapter` | Implementa as portas de saída |
| Builder | `User`, `Order`, `Product`, `ProcessingResult` | Criação fluente de objetos |
| Strategy | `FixedWidthParser` | Extração configurável de campos |
| Factory | `ErrorResponse.builder()` | Criação de respostas de erro |

---

### Fluxo de Processamento

```
1. Upload arquivo.txt via POST /api/v1/orders/upload
                  │
                  ▼
2. FileParser: lê linha a linha (Stream)
                  │
                  ▼
3. LineParser: extrai campos por posição
                  │
                  ▼
4. Aggregator: agrupa por User → Order → Products
                  │
                  ▼
5. Repository: persiste no PostgreSQL (JSONB)
                  │
                  ▼
6. Response: retorna JSON normalizado
```

---

## Tecnologias

| Categoria | Tecnologia | Versão | Justificativa |
|-----------|-----------|--------|---------------|
| **Linguagem** | Java | 21 | Performance + Virtual Threads |
| **Framework** | Spring Boot | 3.4.1 | Maturidade + Produtividade |
| **Banco de Dados** | PostgreSQL | 16 | JSONB + Queries complexas |
| **Build Tool** | Gradle | 8.x | Multi-módulo + Convention Plugins |
| **Migrations** | Flyway | 9.22.3 | Controle de versão do schema |
| **Documentação** | Springdoc OpenAPI | 2.4.0 | Swagger automático |
| **Mapeamento** | MapStruct | 1.6.3 | Conversão DTO ↔ Domain |
| **Testes** | JUnit 5 + Mockito + AssertJ | - | Qualidade de código |
| **Integração** | TestContainers | 1.19.3 | Testes com PostgreSQL real |
| **Coverage** | JaCoCo | 0.8.11 | Cobertura de testes |

---

## Estrutura do Projeto

### Módulos Gradle

```
luizalabs-desafio/
|-- common/                    # Utilitários compartilhados
|   +-- FixedWidthParser          # Parser de linhas de largura fixa
|
|-- core/
|   |-- domain/               # Entidades e Contratos (sem frameworks)
|   |   |-- entity/              # User, Order, Product
|   |   |-- dto/                 # DTOs de domínio
|   |   |-- exception/           # Exceções de negócio
|   |   |-- enums/               # ProcessingStatus
|   |   +-- mapper/              # DomainMapper
|   |
|   +-- use-case/             # Lógica de Negócio
|       |-- impl/
|       |   |-- order/           # ProcessFileUseCaseImpl, QueryOrdersUseCaseImpl
|       |   |   |-- FileParserService
|       |   |   |-- DataAggregatorService
|       |   |   +-- FileValidator
|       |   +-- log/             # QueryLogsUseCaseImpl
|       +-- port/
|           |-- ProcessFileUseCase    # Interface de entrada
|           |-- QueryOrdersUseCase    # Interface de entrada
|           +-- data/                 # Portas de saída
|               |-- OrderDataPort
|               +-- FileProcessingLogPort
|
|-- adapter/
|   +-- data-provider/        # Implementação PostgreSQL
|       |-- adapter/             # OrderDataAdapter, FileProcessingLogAdapter
|       |-- table/               # Entidades JPA (UserOrderTable, etc.)
|       |-- repository/          # Spring Data JPA Repositories
|       |-- mapper/              # UserOrderDataMapper (MapStruct)
|       +-- resources/
|           +-- db/migration/    # Flyway migrations (V1, V2, V3)
|
|-- app/
|   +-- api/                  # REST API
|       |-- controller/          # OrderController
|       |-- exception/           # GlobalExceptionHandler, ErrorResponse
|       |-- config/              # OpenApiConfig, HomeController
|       +-- resources/
|           +-- application.yml  # Configurações Spring
|
|-- report/                   # Agregação de cobertura JaCoCo
|
+-- buildSrc/                 # Convention Plugins Gradle
    |-- java-conventions.gradle     # Configurações Java 21 + JaCoCo
    +-- spring-conventions.gradle   # Configurações Spring Boot
```

---

### Dependências entre Módulos

```
api --------------+--------------> use-case ------> domain ------> common
                  |                    |
                  |                    v
                  +---> data-provider -+
```

| Módulo | Depende de |
|--------|------------|
| common | nenhuma |
| domain | common |
| use-case | domain, common |
| data-provider | use-case |
| api | use-case, data-provider |

---

## Como Executar

### Pré-requisitos

- Java 21 (JDK) - https://adoptium.net/
- Docker e Docker Compose - https://www.docker.com/
- Make (opcional) - para comandos auxiliares

---

### Execução Local

**Passo 1: Subir o PostgreSQL**

```bash
make docker-up
```

**Passo 2: Executar a API**

```bash
make run
```

**Passo 3: Acessar**

| Recurso | URL                                          |
|---------|----------------------------------------------|
| API Base | http://localhost:8080/api/                        |
| Swagger UI | http://localhost:8080/api/swagger-ui/index.html#/ |
| Health Check | http://localhost:8080/api/actuator/health        |

---

## Endpoints da API

### 1. Upload de Arquivo

**POST** `/api/v1/orders/upload`

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/orders/upload \
  -F "file=@data_1.txt" \
  -H "Content-Type: multipart/form-data"
```

**Response:** `200 OK`
```json
[
  {
    "user_id": 1,
    "name": "Zarelli",
    "orders": [...]
  }
]
```

---

### 2. Listar Todos os Pedidos

**GET** `/api/v1/orders`

**Request:**
```bash
curl http://localhost:8080/api/v1/orders
```

**Response:** `200 OK`
```json
[...]
```

---

### 3. Filtrar por Order ID

**GET** `/api/v1/orders?orderId={id}`

**Request:**
```bash
curl http://localhost:8080/api/v1/orders?orderId=123
```

---

### 4. Filtrar por Intervalo de Datas

**GET** `/api/v1/orders?startDate={yyyy-MM-dd}&endDate={yyyy-MM-dd}`

**Request:**
```bash
curl "http://localhost:8080/api/v1/orders?startDate=2021-01-01&endDate=2021-12-31"
```

---

### 5. Buscar Log de Processamento por ID

**GET** `/api/v1/orders/logs/{id}`

**Request:**
```bash
curl http://localhost:8080/api/v1/orders/logs/1
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "fileName": "data_example.txt",
  "uploadedAt": "2025-11-30T12:00:00",
  "status": "SUCCESS",
  "totalLines": 10,
  "processedLines": 10,
  "errorLines": 0,
  "errors": [],
  "processingTimeMs": 1234
}
```

---

### 6. Listar Logs de Processamento (com filtros)

**GET** `/api/v1/orders/logs?status={status}&page={page}&size={size}`

**Parâmetros:**
- `status` (opcional): `SUCCESS`, `PARTIAL_SUCCESS`, `FAILED`, `PROCESSING`
- `page` (opcional, default: `0`): Número da página
- `size` (opcional, default: `20`): Tamanho da página

**Exemplos:**

```bash
# Listar todos os logs
curl http://localhost:8080/api/v1/orders/logs

# Filtrar logs com sucesso
curl "http://localhost:8080/api/v1/orders/logs?status=SUCCESS"

# Filtrar logs com erro e paginação
curl "http://localhost:8080/api/v1/orders/logs?status=FAILED&page=0&size=10"
```

**Response:** `200 OK`
```json
[
  {
    "id": 2,
    "fileName": "orders2.txt",
    "uploadedAt": "2025-11-30T13:00:00",
    "status": "PARTIAL_SUCCESS",
    "totalLines": 5,
    "processedLines": 4,
    "errorLines": 1,
    "errors": [
      {
        "lineNumber": 3,
        "lineContent": "0000000070...",
        "errorMessage": "Erro ao parsear valor"
      }
    ],
    "processingTimeMs": 567
  }
]
```

---

## Build e Testes

| Comando         | Descrição            |
|-----------------|----------------------|
| `make build`    | Build completo       |
| `make test`     | Executar testes      |
| `make run`      | Rodar API            |
| `make coverage` | Relatório de cobertura |
| `make check`    | check código         |
| `make format`   | Formatar código      |
| `make clean`    | Limpar build         |


---

## Trade-offs e Decisões

### PostgreSQL com JSONB vs MongoDB

| Critério | PostgreSQL + JSONB | MongoDB |
|----------|-------------------|---------|
| Queries complexas | Nativo SQL | Aggregation pipelines (mais verboso) |
| Transações ACID | Completo | Limitado a single document |
| Flexibilidade JSON | JSONB | Nativo |
| Índices em JSON | GIN indexes | Sim |
| Joins | SQL nativo | Não suporta |

**Decisão:** Escolhi PostgreSQL porque as queries SQL complexas (filtro por data, orderId) ficam mais simples de escrever, e consigo resolver tudo em uma única query sem precisar de lookups adicionais.

---

### Stream Processing vs Carregar Arquivo Inteiro

| Critério | Stream | Carregar tudo |
|----------|--------|---------------|
| Uso de memória | Constante O(1) | Proporcional O(n) |
| Arquivos grandes | Suporta GB | Risco de OutOfMemory |
| Complexidade | Maior | Simples |

**Decisão:** Optei por stream processing porque permite processar arquivos de qualquer tamanho sem risco de estouro de memória. O trade-off é um código um pouco mais complexo, mas vale a pena.

---

### Gradle Multi-Módulo vs Projeto Único

| Critério | Multi-módulo | Projeto único |
|----------|--------------|---------------|
| Separação física | Impossível violar | Dependência circular possível |
| Builds paralelos | Mais rápido | Sequencial |
| Reutilização | Módulos independentes | Tudo acoplado |
| Complexidade | Mais arquivos de config | Simples |

**Decisão:** Multi-módulo garante que violações de arquitetura sejam erros de compilação, não apenas convenções que podem ser ignoradas. Isso é importante para manter a arquitetura limpa ao longo do tempo.

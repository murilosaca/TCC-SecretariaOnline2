# Secretaria Online 2 (SO2)

Repositório oficial do TCC — plataforma da secretaria acadêmica do SEPT/UFPR.

## Documentação

| Arquivo | Conteúdo |
|---|---|
| [`docs/tcc-docs.md`](docs/tcc-docs.md) | Documento principal de requisitos do TCC: atores, RFs/RNFs, regras de negócio, arquitetura (Kotlin + React), segurança e qualidade. |
| [`docs/telas-figma.md`](docs/telas-figma.md) | Mapa de rotas e fluxos de interface (F0–F8). O detalhe de cada tela está em [`docs/telas/`](docs/telas/). |
| [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md) | Relatório da auditoria de fundação (regras, backend, frontend) e o que ainda falta. |
| [`.cursorrules`](.cursorrules) | Regras e skills para o time e para a IA. Lombok é proibido. |

A pasta `frontend/` ainda contém o Angular antigo (processo `ng serve` pode travar a exclusão). O portal oficial da spec é **`frontend-react`**.

## Stack

| Camada | Tecnologia oficial (`docs/tcc-docs.md`) | Nesta fundação |
|---|---|---|
| Backend | Kotlin + Spring Boot 3 + Maven + PostgreSQL 16 + Flyway | Java 21 temporário, mesmos contratos |
| Frontend | React 18 + Vite + TypeScript + TanStack Query | `frontend-react/` |
| Local | Docker Compose (PostgreSQL) | `docker-compose.yml` |

Arquitetura: monólito modular com Clean Architecture. Sem Lombok.

## Como subir

```bash
# 1. Banco
docker compose up -d

# 2. API (http://localhost:8080/swagger-ui)
cd backend
mvn spring-boot:run

# 3. Web React 18 + Vite (http://localhost:5173)
cd frontend-react
npm run dev
```

O frontend Vite encaminha `/academico`, `/publico` e `/auth` para `http://localhost:8080`. O CORS libera `http://localhost:5173`.

## O que já existe nesta base

- Configuração OpenAPI / Swagger UI.
- CRUDs acadêmicos: Curso (RF-F5-004-a), Disciplina (RF-F5-004-b), Aluno (RF-F5-003, com GRR — sem `idade` do legado) e Período letivo (RF-F5-004-c, sem sobreposição).
- Página pública de contato (RF-F0-004), erros HTTP (RF-F0-005) e esqueleto das rotas Figma (`/login`, `/secretaria/*`, etc.).
- Value objects `Grr`, `Email`, `Cpf`; erros RFC 7807; paginação com `_links` HATEOAS; UUID v7.

## Módulos previstos (ainda não implementados)

`iam`, `solicitacoes`, `formativas`, `estagio`, `tcc`, `presenca`, `comunicacao`, `certificados`, `auditoria`, `arquivos`, `bff`.

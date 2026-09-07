# Secretaria Online 2 (SO2)

Repositório oficial do TCC — plataforma da secretaria acadêmica do SEPT/UFPR.

## Documentação

| Arquivo | Conteúdo |
|---|---|
| [`docs/tcc-docs.md`](docs/tcc-docs.md) | Documento principal de requisitos do TCC: atores, RFs/RNFs, regras de negócio, arquitetura (Kotlin + React), segurança e qualidade. |
| [`docs/telas-figma.md`](docs/telas-figma.md) | Mapa de rotas e fluxos de interface (F0–F8). O detalhe de cada tela está em [`docs/telas/`](docs/telas/). |
| [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md) | Relatório da auditoria de fundação, P0 fechado e dívidas conscientes. |
| [`.cursorrules`](.cursorrules) | Regras e skills para o time e para a IA. Lombok é proibido. |

O portal oficial deste repositório é **`frontend-react/`** (React 18 + Vite). A pasta `frontend/` (Angular) **não** é a stack oficial — não recrie nem commite.

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 3 + Maven + PostgreSQL 16 + Flyway |
| Frontend | React 18 + Vite + TypeScript + TanStack Query (`frontend-react/`) |
| Arquivos | API S3-compatível (MinIO no desenvolvimento) |

Arquitetura: monólito modular com Clean Architecture. Sem Lombok. Identificadores UUID v7.

## Como subir (P0)

O Postgres do IAM/SO2 neste ambiente é o container **`so2_postgres_iam`** na porta **5433**, banco `secretaria_dev`. O `docker compose` do repositório sobe **outro** histórico em **5432** — não use 5432 para esta API.

```bash
# 1. Banco IAM (já no ar se o container so2_postgres_iam existir)
#    Porta hospedeira 5433 → secretaria_dev

# 2. API (http://localhost:8080)
cd backend
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/secretaria_dev"
mvn spring-boot:run

# 3. Web (http://localhost:5174 — 5173 costuma estar ocupado)
cd frontend-react
npm run dev
```

Proxies Vite (`/auth`, `/academico`, `/publico`, `/requests`, `/request-types`, `/bff`, `/events`) apontam para `http://localhost:8080`. CORS libera `http://localhost:5173` e `http://localhost:5174`.

Senha de todos os usuários de seed: `TroqueEstaSenha1!` (somente local; override por `IAM_DEV_SEED_PASSWORD`).

| Usuário | Papel no smoke |
|---|---|
| `aluno.dev@ufpr.br` / `GRR20240001` | Aluno com senha já alterada |
| `novo.dev@ufpr.br` / `GRR20240002` | Primeiro acesso (`senhaAlterada=false`) |
| `professor.dev@ufpr.br` / `GRR20240003` | Hospedeiro F3.2 (`event.manage`, `event.host`) |

A oficina seed `"Oficina Proof of Stay (dev)"` pode já estar `COMPLETA` para `aluno.dev`. PIN `123456` vale só para essa oficina antiga e some se a API reiniciar (`HostPinStore` em memória). Para repetir o circuito, crie um evento novo no professor.

## Smoke P0 (um usuário por papel)

1. **Aluno** — `aluno.dev@ufpr.br` → `/inicio`: saudação, período ou alerta, solicitações, `eventosHoje` / próximos se houver janela. “Nova solicitação” só se `_links.novaSolicitacao`. `/eventos` → presença SECRET_SINGLE.
2. **Primeiro acesso** — `novo.dev@ufpr.br` → `/primeiro-acesso`. `/inicio`, formativas, eventos e host bloqueados (403 no gate).
3. **Professor** — `professor.dev@ufpr.br` → `/inicio` **não** mostra “Olá, aluno” (403 honesto). `/professor/eventos` → cria SECRET_SINGLE → abre janela → PIN só no painel → aluno confirma → some `confirmar-entrada`. Encerrar → `CONCLUIDO`.
4. **Anônimo** — `/login`, `/contato`, `/publico/verificar-protocolo/{PROT-…}` 200; `/bff` e `/events` 401. Informe um `PROT-AAAA-NNNNN` gerado em `/solicitacoes/nova` (não use `/demo`).
5. **Aluno** em `GET /events?mine=true` e host-session → 403.
6. **`/academico/**`** continua 200 (ainda sem FGAC).

F0.7 (certificado) permanece stub. Horas formativas e certificados no dashboard ficam `null` — os módulos não existem.

## O que o P0 já entrega

- IAM: login JWT RS256, refresh httpOnly, recuperação/redefinição, primeiro acesso + LGPD.
- Motor de solicitações: `RequestType` + `form_schema` + `workflow_json`.
- BFF do aluno com degradação por bloco (HTTP 200) e 403 se a sessão não for de aluno.
- Presença SECRET_SINGLE (aluno) + hospedeiro F3.2 (PIN só na host-session).
- F0.6: consulta pública de metadados de protocolo (sem PDF).
- CRUDs acadêmicos (fundação, ainda `permitAll`): curso, disciplina, aluno (GRR, sem `idade`) e período letivo.
- Value objects `Grr`, `Email`, `Cpf`; erros RFC 7807; paginação com `_links` HATEOAS; UUID v7.

## Módulos ainda não abertos

`formativas`, `estagio`, `tcc`, `comunicacao`, `certificados`, `auditoria` (módulo), `arquivos`. Dívidas conscientes: ver [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md).

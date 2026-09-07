# Secretaria Online 2 (SO2)

Plataforma digital da secretaria acadêmica do **SEPT/UFPR**. Este é o repositório oficial do TCC.

O SO2 não substitui o juízo de docentes, comissões ou secretaria. Ele garante trilha de auditoria, integridade de dados e automação de trâmites repetitivos.

**Estado atual: P0 fechado e demonstrável** em `main`. O circuito oficial do MVP: login → primeiro acesso/LGPD → dashboard do aluno → nova solicitação → presença SECRET_SINGLE (aluno + hospedeiro professor) → consulta pública de protocolo.

---

## O que é este projeto

Atores previstos no TCC: Público (F0), Aluno (F1), Egresso (F2), Professor (F3), Comissões CAAF/COE (F4), Secretaria (F5), Coordenação (F6), Admin (F7) e transversal (F8).

O P0 do Figma cobre só o núcleo que prova o produto:

| Rota | Quem | Situação |
|---|---|---|
| `/login` (+ recuperar senha, nova senha, primeiro acesso) | Público / todos | Entregue |
| `/inicio` | Aluno (BFF); outros perfis veem 403 honesto | Entregue |
| `/solicitacoes/nova` | Aluno | Entregue (motor genérico, não tela por tipo) |
| `/eventos` + `/eventos/:id/presenca` | Aluno | Entregue (SECRET_SINGLE) |
| `/professor/eventos` + operação | Professor | Entregue (necessário para provar a presença) |
| `/publico/verificar-protocolo` | Anônimo | Entregue (metadados; sem PDF) |

O resto do mapa F0–F8 (formativas, CAAF, estágio, TCC, certificados, FGAC de menu, dashboard professor/secretaria) **ainda não foi aberto**. Está especificado; não está implementado.

A spec original cita Kotlin + React. **Neste repositório o grupo adotou Java 21 + React 18.** Preserve domínio, RNFs e contratos. Não reintroduza Kotlin, Lombok, Angular nem o CRUD didático do legado (`idade` em Aluno, `ddl-auto=update`).

---

## Documentação (fonte de verdade)

| Onde | Para quê |
|---|---|
| [`docs/tcc-docs.md`](docs/tcc-docs.md) | Requisitos (RFs/RNFs), atores, regras de negócio, segurança, qualidade. A spec ainda fala Kotlin; os contratos valem. |
| [`docs/telas-figma.md`](docs/telas-figma.md) | Mapa de rotas F0–F8. Detalhe de cada tela em [`docs/telas/`](docs/telas/). |
| [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md) | O que a fundação e o P0 entregaram, e a lista de dívidas conscientes. |
| [`.cursorrules`](.cursorrules) e [`.cursor/rules/`](.cursor/rules/) | Convenções do time e da IA (nomenclatura, Clean Architecture, proibições). |
| [`.env.example`](.env.example) | Variáveis de ambiente. Não commite `.env` nem chaves JWT. |

Swagger (fora de produção): `http://localhost:8080/swagger-ui`. OpenAPI: `/v3/api-docs`.

---

## Onde estão as coisas

```
TCC-SecretariaOnline2/
├── backend/                 API Java 21 + Spring Boot 3 (Maven)
│   └── src/main/java/br/ufpr/sept/so2/
│       ├── shared/          Transversal: RFC 7807, Security, CORS, UUID v7, VOs
│       └── modules/         Um bounded context por pasta
├── frontend-react/          Portal oficial (React 18 + Vite + TypeScript)
├── docs/                    Requisitos e telas (não é código)
├── docker-compose.yml       Postgres 16 em :5432 — outro histórico; ver aviso abaixo
├── .env.example
└── .cursorrules
```

**Não use** uma pasta `frontend/` Angular. Não é a stack deste repo; não recrie nem commite.

### Backend — módulos que existem

Pacote raiz: `br.ufpr.sept.so2`. Cada módulo segue Clean Architecture (`domain` → `application` → `infrastructure` / `api`). `domain` não importa Spring/JPA. Módulos conversam por ports.

| Módulo | Pasta | O que faz hoje |
|---|---|---|
| Transversal | `shared/` | Exception handler RFC 7807, CORS, Security, `Grr`/`Email`/`Cpf`, `Uuids.v7()` |
| Público | `modules/publico/` | `GET /publico/contato` |
| Acadêmico | `modules/academico/` | CRUD curso, disciplina, aluno (GRR, sem `idade`), período letivo (sem sobreposição). Ainda `permitAll` — sem FGAC |
| IAM | `modules/iam/` | Login, refresh, logout, primeiro acesso + LGPD, recuperação via Outbox. JWT RS256 15 min; cookie `so2_refresh` (`httpOnly; Secure; SameSite=Lax; Path=/auth`). Senha só Argon2id |
| Solicitações | `modules/solicitacoes/` | Motor `RequestType` + `form_schema` + `workflow_json`. Seed `DECLARACAO_SIMPLES`. Protocolo `PROT-AAAA-NNNNN`. `GET /publico/protocolos/{protocolo}` |
| Presença | `modules/presenca/` | Evento + Proof of Stay SECRET_SINGLE. Aluno confirma PIN. Professor hospeda (janela, encerrar). PIN em claro só na host-session (`HostPinPort` / memória). Persistido só como Argon2id |
| BFF | `modules/bff/` | `GET /bff/dashboard/aluno` — agrega identidade, período, solicitações e eventos. Degrada por bloco (HTTP 200). 403 se a sessão não for de aluno |

Módulos **previstos e ainda sem código**: `formativas`, `estagio`, `tcc`, `comunicacao`, `certificados`, `auditoria` (módulo dedicado), `arquivos`.

### Flyway (imutável)

`backend/src/main/resources/db/migration/`. **Não edite** migration já aplicada.

| Versão | Conteúdo |
|---|---|
| V001 | Extensões Postgres |
| V002 | Acadêmico (`curso`, `disciplina`, `aluno`, `periodo_letivo`) |
| V003 | IAM (`usuario`, authorities, refresh, outbox, `audit_log`) |
| V004 | Solicitações (`tipo_solicitacao`, `solicitacao`, protocolo) |
| V005 | Presença (`evento`, `presenca`) |
| V006 | `evento.id_anfitriao` (V005 já tinha rodado no banco local) |

Próxima migration, quando um módulo novo precisar de tabela: **V007**.

### Frontend — onde cada tela mora

`frontend-react/src/`

| Pasta | Uso |
|---|---|
| `pages/publico/` | Login, recuperar/nova senha, contato, erros HTTP, verificar protocolo/certificado |
| `pages/aluno/` | Primeiro acesso, solicitações, eventos, presença |
| `pages/inicio/` | Dashboard (`/inicio`) — hoje só consome o BFF do aluno |
| `pages/professor/` | Lista / nova / detalhe / operação de evento |
| `pages/secretaria/` | CRUD acadêmico da fundação (atalho de dev; ainda sem FGAC) |
| `api/` | Cliente HTTP (`client.ts` guarda o access token **só em memória**) |
| `auth/` | Sessão, guards (`mustChangePassword` bloqueia o resto do sistema) |
| `hooks/useActions.ts` | UI cega a perfil: botão só se existir `_links` |
| `components/` | `DynamicForm`, `AttendanceWidget`, `HostActionBar` |

Rotas da UI (`/cursos` redireciona para `/secretaria/cursos`) **não** repetem o prefixo da API, para o proxy do Vite não interceptar a navegação.

---

## Stack e regras que não dobram

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 3 + Maven + PostgreSQL 16 + Flyway |
| Frontend | React 18 + Vite + TypeScript + TanStack Query (`frontend-react/`) |
| Arquivos (futuro) | API S3-compatível (MinIO no desenvolvimento) |
| IDs | UUID v7. Proibido `Long`/`IDENTITY` em entidade de negócio |
| Senha | Só Argon2id. Proibido MD5, SHA-1, SHA-256 e bcrypt para senha |
| Autorização | Capability `dominio.acao` + `_links` HATEOAS. Nunca `hasRole` / `ROLE_*` |
| Erros | RFC 7807 (`application/problem+json`) |
| Lombok | **Proibido** (entidade com getters manuais; DTO como `record`) |

Outras invariantes: login aceita `@ufpr.br`, e-mail pessoal ou GRR (`GRR` + 8 dígitos). `senhaAlterada = false` bloqueia tudo até senha forte + aceite LGPD. Solicitações não se duplicam por tipo. Certificado oficial só gerado pelo sistema (módulo ainda inexistente). Presença sem geofence, trust score ou aula SIGA. Access token nunca vai para `localStorage`.

---

## APIs que o P0 expõe

| Prefixo | Auth | Função |
|---|---|---|
| `/auth/*` | Misto (login anônimo; `me` autenticado) | IAM |
| `/publico/**` | Anônimo | Contato, protocolo |
| `/academico/**` | `permitAll` (dívida) | CRUD da fundação |
| `/request-types`, `/requests` | JWT + `request.*` | Motor de solicitações |
| `/events` | JWT + `attendance.*` / `event.*` | Eventos e presença |
| `/bff/dashboard/aluno` | JWT + `dashboard.view_own` + capability de aluno | Dashboard agregado |

JSON em camelCase. Datas ISO-8601 UTC. Listagens `Pageable` (size 20, máx. 100) com `_links`.

---

## Plano geral (depois do P0)

Ordem sugerida — uma fatia vertical por vez, sem abrir dois módulos no mesmo sprint:

1. **Formativas a partir de presença validada** (RF-F1-006) — fecha o valor acadêmico do Proof of Stay (horas no BFF deixam de ser sempre `null` quando houver aprovação). Sem certificado e sem lote CAAF neste passo.
2. **Presença v4.1 restante** — QR e/ou `SECRET_DUAL` + janela de saída no mesmo motor. Sem geofence.
3. **Deliberação de solicitações** (professor/secretaria, `_links` do workflow). O motor já existe; falta a fila e o parecer.
4. **CAAF / COE** — lote CAAF só com presença já validada; parecer COE sempre individual.
5. **Certificados** — só gerados pelo sistema; F0.7 deixa de ser stub. Sem upload externo oficial.
6. **Comunicação + dispatcher do Outbox** — hoje os eventos (`presenca.confirmada`, `evento.encerrado`, recuperação de senha) ficam `PENDING`. Sem e-mail síncrono nunca.
7. **FGAC (F7)** — fechar `/academico/**`, nav por `_links` (hoje o menu ainda mostra atalhos de dev: Eventos prof., CRUD da secretaria), escopo por curso.
8. **Egresso, estágio, TCC, coordenação (F6.1)** — quando o requisito entrar no sprint.

Dívida consciente (não é P0): ver a tabela em [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md). Destaques: BFF professor (F3.1) ausente; horas/certificados `null`; F0.7 stub; `/academico/**` aberto; PIN da host-session some se a API reiniciar; backend em Java (spec Kotlin); sem ArchUnit; rate limit em memória (sem Redis).

---

## Como subir

Neste ambiente o Postgres do SO2 é o container **`so2_postgres_iam` na porta 5433**, banco `secretaria_dev` / usuário `secretaria`.

O `docker compose` do repositório sobe **outro** Postgres em **5432**, com outro histórico Flyway. **Não aponte a API do P0 para 5432.**

```bash
# 1. Banco (já no ar se so2_postgres_iam existir)
#    5433 → secretaria_dev

# 2. API — http://localhost:8080
cd backend
# PowerShell:
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/secretaria_dev"
mvn spring-boot:run

# 3. Web — http://localhost:5174 (5173 costuma estar ocupado)
cd frontend-react
npm install
npm run dev
```

Copie `.env.example` se for usar variáveis no shell. A URL JDBC padrão do `application.yml` ainda é `:5432` — no P0 local **sobrescreva para 5433**.

Proxies Vite (`/auth`, `/academico`, `/publico`, `/requests`, `/request-types`, `/bff`, `/events`, `/v3`, `/swagger-ui`, `/actuator`) vão para `http://localhost:8080`. CORS libera `http://localhost:5173` e `http://localhost:5174`.

Testes pontuais:

```bash
cd backend && mvn -q test
cd frontend-react && npm test
```

Não é obrigatório rodar a suíte inteira se 5433 + `:8080` + `:5174` já estiverem no ar. Reinicie a API depois de mudar Java.

---

## Contas de desenvolvimento

Senha de todos: `TroqueEstaSenha1!` (só local; override `IAM_DEV_SEED_PASSWORD`).

| Usuário | GRR | O que testa |
|---|---|---|
| `aluno.dev@ufpr.br` | `GRR20240001` | Aluno com senha já alterada |
| `novo.dev@ufpr.br` | `GRR20240002` | Primeiro acesso (`senhaAlterada=false`) |
| `professor.dev@ufpr.br` | `GRR20240003` | Hospedeiro (`event.manage`, `event.host`) |

A oficina seed `"Oficina Proof of Stay (dev)"` pode já estar `COMPLETA` para `aluno.dev`. O PIN `123456` (`EVENT_DEV_PIN`) vale só para essa oficina e some se a API reiniciar (store em memória). Para repetir o circuito, crie um evento novo em `/professor/eventos` — o PIN novo aparece **somente** na host-session.

---

## Smoke P0 (um usuário por papel)

1. **Aluno** — `aluno.dev` → `/inicio`: saudação, período ou alerta de calendário, solicitações, `eventosHoje` / próximos se houver janela. “Nova solicitação” só se `_links.novaSolicitacao`. `/eventos` → presença SECRET_SINGLE.
2. **Primeiro acesso** — `novo.dev` → `/primeiro-acesso`. O resto do sistema (dashboard, eventos, host) responde 403 no gate.
3. **Professor** — `professor.dev` → `/inicio` **não** mostra “Olá, aluno” (403 honesto). `/professor/eventos` → cria SECRET_SINGLE → abre janela → PIN só no painel → aluno confirma → some `confirmar-entrada`. Encerrar → `CONCLUIDO`.
4. **Anônimo** — `/login`, `/contato`, `/publico/verificar-protocolo/{PROT-AAAA-NNNNN}` 200. `/bff` e `/events` 401. O número `PROT-…` sai de `/solicitacoes/nova` (não use `/demo`).
5. **Aluno** em `GET /events?mine=true` e `host-session` → 403.
6. **`/academico/**`** continua 200 (ainda sem FGAC).

Horas formativas e certificados no dashboard ficam `null` — os módulos não existem. F0.7 (verificar certificado) permanece stub.

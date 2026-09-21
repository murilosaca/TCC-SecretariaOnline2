# Secretaria Online 2 (SO2)

Plataforma digital da secretaria acadêmica do **SEPT/UFPR**. Este é o repositório oficial do TCC.

O SO2 não substitui o juízo de docentes, comissões ou secretaria. Ele garante trilha de auditoria, integridade de dados e automação de trâmites repetitivos.

**Onde estamos:** P0 fechado e demonstrável. Itens 1–**7** entregues. **Próxima fatia = 8 Expo.** Esta fatia **não** é o portal admin F7 (sem F7.1–F7.9). Não abrir lote CAAF, COE, F6.1 nem MinIO neste passo.

Circuito demonstrável: login → primeiro acesso (senha + LGPD) → `/inicio` do aluno → solicitação + deep-link ao professor → presença **QR\|SECRET × SINGLE\|DUAL** → formativa (`PENDENTE_CONFIRMACAO` → aluno confirma → `AGUARDANDO_CAAF`) → CAAF aprova → certificado oficial (PDF + hash + ED25519) → `/certificados` + F0.7. Secretaria (`secretaria.dev`) opera o CRUD de TADS; aluno/professor/CAAF não veem Cursos.

---

## Documentação (fonte de verdade)

Nesta ordem. Se divergirem, não invente regra de negócio.

| Onde | Para quê |
|---|---|
| [`docs/tcc-docs.md`](docs/tcc-docs.md) | Requisitos (RFs/RNFs), atores, regras de negócio. |
| [`docs/telas-figma.md`](docs/telas-figma.md) + [`docs/telas/`](docs/telas/) | Mapa de rotas F0–F8 e detalhe de cada tela. |
| Este README | Estado do **repo**: o que está no ar, como subir, o que não abrir. |
| [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md) | Detalhe da fundação/P0/itens 1–7 e dívida consciente. |
| [`.cursorrules`](.cursorrules) e [`.cursor/rules/`](.cursor/rules/) | Convenções (nomenclatura, Clean Architecture, proibições). |
| [`.env.example`](.env.example) | Variáveis. Não commite `.env` nem chaves JWT. |

Swagger só fora de produção: `http://localhost:8080/swagger-ui`. OpenAPI: `/v3/api-docs`.

---

## Onde estão as coisas

```
TCC-SecretariaOnline2/
├── backend/                 API Kotlin + Spring Boot 3 + JVM 21 (Maven)
│   └── src/main/kotlin/br/ufpr/sept/so2/
│       ├── shared/          RFC 7807, Security, CORS, UUID v7, VOs
│       └── modules/         Um bounded context por pasta
├── frontend-react/          Portal oficial (React 18 + Vite + TypeScript)
├── frontend-react-native/   Esqueleto Expo (fora do circuito P0)
├── docs/
├── docker-compose.yml       Outro Postgres em :5432 — ver “Como subir”
├── .env.example
└── .cursorrules
```

**Não use** pasta `frontend/` Angular. Não é a stack deste repo.

### Módulos hoje

Pacote `br.ufpr.sept.so2`. Clean Architecture; módulos conversam por ports.

| Módulo | O que faz hoje |
|---|---|
| `shared/` | RFC 7807, CORS, Security, `Grr`/`Email`/`Cpf`, `Uuids.v7()` |
| `publico` | `GET /publico/contato` |
| `academico` | CRUD curso, disciplina, aluno (GRR, sem `idade`), período letivo **global** (sem sobreposição, sem `id_curso`). FGAC (`course.manage` / `subject.manage` / `user.manage_students` / `calendar.manage`) + escopo `curso_secretario` ∪ `idCoordenador` (`CursoEscopoPort`). Seed TADS 120 h |
| `iam` | Login, refresh, logout, primeiro acesso + LGPD, recuperação via Outbox despachada. JWT RS256 15 min; cookie `so2_refresh`; senha só Argon2id. JWT de deliberação (72 h) |
| `solicitacoes` | Motor `RequestType` + `form_schema` + `workflow_json`. Seed `DECLARACAO_SIMPLES`. Protocolo público. Fila e transições autenticadas (`request.*`) |
| `presenca` | Evento + Proof of Stay **QR\|SECRET × SINGLE\|DUAL**. Segredo em claro só na host-session. Sem geofence |
| `bff` | `GET /bff/dashboard/aluno` (agrega; degrada por bloco). Sem BFF professor |
| `formativas` | Gatilho na mesma TX da presença **COMPLETA**. Aluno confirma; CAAF aprova/indefere (sem lote). Aprovar emite certificado |
| `certificados` | Emissão só pelo sistema na aprovação CAAF. PDF `bytea` (sem MinIO). SHA-256 + ED25519 + F0.7. Sem upload oficial |
| `comunicacao` | Dispatcher Outbox → SMTP (Mailpit). Sem hub F1.6 |

Módulos **previstos e ainda sem código**: `estagio`, `tcc`, `auditoria` (módulo dedicado), `arquivos`.

### Flyway (imutável)

`backend/src/main/resources/db/migration/`. **Não edite** migration já aplicada. **Próxima = V012.**

| Versão | Conteúdo |
|---|---|
| V001 | Extensões Postgres |
| V002 | Acadêmico (`curso`, `disciplina`, `aluno`, `periodo_letivo`) |
| V003 | IAM (`usuario`, authorities, refresh, outbox, `audit_log`) |
| V004 | Solicitações |
| V005 | Presença (`evento`, `presenca`) |
| V006 | `evento.id_anfitriao` |
| V007 | Formativas |
| V008 | Parecer CAAF em `formativa` |
| V009 | Certificados (`bytea`; UNIQUE `id_formativa` / `hash_sha256`) |
| V010 | Outbox dispatcher (`last_error`, `processed_at`) |
| V011 | `curso_secretario` (N:N; PK `id_curso`+`id_usuario`) |

### Frontend — pastas

`frontend-react/src/`

| Pasta | Uso |
|---|---|
| `pages/secretaria/` | CRUD F5.6–F5.9 |
| `pages/aluno/`, `pages/inicio/`, `pages/professor/` | Aluno, BFF `/inicio`, hospedeiro |
| `pages/solicitacoes/`, `pages/formativas/`, `pages/publico/` | Fila/deliberar, CAAF, F0 |
| `layouts/` | `AuthLayout` (F0) e `AppLayout`. Nav: `useActions(links)` |
| `hooks/useActions.ts` | Botão só se existir `_links` |
| `api/` | Access token **só em memória** (nunca `localStorage`) |

Rotas planas `/cursos` → `/secretaria/cursos` (redirect temporário). Em geral a UI **não** repete o prefixo da API. Exceção: `/formativas` é SPA e API — o proxy devolve `index.html` quando `Accept` inclui `text/html`. `/certificados` (SPA) e `/certificates` (API) não colidem.

---

## Stack e regras que não dobram

| Camada | Tecnologia |
|---|---|
| Backend | Kotlin + Spring Boot 3 + JVM 21 + Maven + PostgreSQL 16 + Flyway |
| Web | React 18 + Vite + TypeScript + TanStack Query. CSS próprio. **Sem Tailwind** |
| Mobile | React Native + Expo, **mesma API**. NativeWind no mobile **não** autoriza Tailwind em `frontend-react/` |
| IDs / senha / erros | UUID v7. Senha só Argon2id. RFC 7807 |
| Autorização | Capability `dominio.acao` + `_links` HATEOAS. Nunca `hasRole` / `ROLE_*` / `user.role` |
| Lombok / Angular | **Proibidos** |

Login: `@ufpr.br`, e-mail pessoal ou GRR (`GRR` + 8 dígitos). `senhaAlterada = false` bloqueia até senha forte + aceite LGPD. Access token só em memória. Flyway imutável. Certificado oficial só gerado pelo sistema. Presença sem geofence, trust score ou aula SIGA.

---

## APIs entregues

| Prefixo | Auth | Função |
|---|---|---|
| `/auth/*` | Misto (login anônimo; `me` autenticado) | IAM. `GET /auth/me`._links = rotas de **UI** (menu). Sem `/bff/menu` |
| `/publico/**` | Anônimo | Contato; protocolo; verificação de certificado |
| `/academico/**` | JWT + capability da tela | CRUD. Anônimo → 401. Sem cap → 403. Fora do escopo (curso) → 404. GRR/e-mail malformado → 422 `validation-error`. `_links.disciplinas` só com `subject.manage`. Períodos: `calendar.manage` all-or-nothing |
| `/request-types`, `/requests/**` | JWT + `request.*` (**não** anônimo) | Motor. Inbox `?canDeliberate=true`. `POST /{id}/transitions` |
| `/events` | JWT + `attendance.*` / `event.*` | Presença v4.1 (QR\|SECRET × SINGLE\|DUAL) |
| `/bff/dashboard/aluno` | JWT + `dashboard.view_own` + (`attendance.view_open` **ou** `request.view_own`) | Dashboard agregado (HTTP 200; degrada por bloco) |
| `/formativas` | JWT + `formative.view_own` / `confirm_own` / `review` | Sem POST avulso. Sem lote. Aprovar emite certificado |
| `/certificates` | JWT + `certificate.view_own` | Lista/download do dono. Sem POST. Outro aluno → 404 |
| `/.well-known/jwks.json` | Anônimo | Chave pública ED25519 (par separado do RSA do JWT) |

JSON camelCase. Datas ISO-8601 UTC. Listagens `Pageable` (20 / máx. 100) com `_links`.

---

## Plano depois do P0

O mapa F0–F8 do Figma descreve o produto; **não é cronograma**. A ordem privilegia: (1) fechar o que já existe no código; (2) valor de banca (deliberação, horas `N / 120`); (3) dependência real (certificado exige formativa `APROVADA`; COE exige estágio). Uma fatia vertical por sprint. Não abrir dois bounded contexts no mesmo PR.

| # | Fatia | Estado |
|---|---|---|
| 1 | Formativas via presença | **Feito** |
| 2 | Deliberação do motor | **Feito** |
| 3 | CAAF individual | **Feito** |
| 4 | Presença v4.1 (QR / `SECRET_DUAL`) | **Feito** |
| 5 | Certificados oficiais + F0.7 | **Feito** |
| 6 | Dispatcher do Outbox + SMTP + deep-link | **Feito** |
| 7 | FGAC acadêmico + nav | **Feito** — fecha `/academico/**` e a nav; **não** é o portal admin F7 |
| 8 | Cliente Expo (React Native) | **Próxima** — mesma API; web já provou os contratos |
| 9 | Estágio + COE, TCC, egresso, F6.1 | Módulos novos |

**1. Formativas** — V007; gatilho na TX de `ConfirmarPresencaUseCase` quando a presença fica **COMPLETA**. Aluno confirma ou cancela. Sem `POST /formativas`. BFF soma `APROVADA` (`null` sem cadastro). Não: CAAF/certificado (3 e 5), lote.

**2. Deliberação** — `POST /requests/{id}/transitions` + fila `?canDeliberate=true`. UI `/solicitacoes?to=me` (secretaria reutiliza). Deep-link no item 6; `request.triage` não recebe o e-mail. Não: lote, F3.1, FORWARD, novo `RequestType`.

**3. CAAF individual** — `AGUARDANDO_CAAF` → `APROVADA`/`INDEFERIDA`. Seed `formative.review` em `caaf.dev`. Fila `?canReview=true`. Horas = `cargaHoraria` já gravada. Não: lote/F4.1, filtro por comissão, COE, comprovante.

**4. QR / `SECRET_DUAL`** — quatro modos no mesmo motor; janelas ao vivo (15 min). Formativa só na presença **COMPLETA** (não na `ENTRADA` de DUAL). Encerrar sem PDF. Não: janelas pré-agendadas, lista de inelegíveis, câmera Expo.

**5. Certificados + F0.7** — V009; emite na aprovação CAAF (mesma TX). PDF `bytea`, SHA-256 + ED25519, JWKS, F0.7 no browser. Sem POST/upload oficial. Não: MinIO, CA-04, revogação, egresso, PDF ao encerrar evento.

**6. Dispatcher + SMTP + deep-link** — at-least-once + Mailpit. F0.2 anti-enumeração. `?token=` **exige sessão**. JTI na TX da deliberação. Tipos no-op → `SENT`. Não: hub F1.6, F3.8, F7.5, push, e-mail de `certificado.emitido`.

**7. FGAC acadêmico + nav** — feito

Fecha `/academico/**` e o menu. **Não** é F7. Detalhe (anti-lockout do PUT, join IAM→aluno, `_links.criar`, F5.2 fora): [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md).

- `anyRequest().authenticated()`; `/requests/**` **não** anônimo; deep-link `?token=` continua exigindo sessão.
- Capabilities: `course.manage` / `subject.manage` / `user.manage_students` / `calendar.manage` (não `curso.manage` / `student.manage`).
- V011 + escopo no use case (`CursoEscopoPort`). Claim `cursoIds` **não** entrou no JWT.
- POST inclui o criador na mesma TX; PUT re-adiciona quem edita (anti-lockout). Transferir curso depende de F7.1.
- `request.view_curso` filtra a fila; professor só com `request.deliberate` **não** herda o filtro. Secretaria tem triage+deliberate e **não** recebe deep-link. Rel de menu `solicitacoes` exige `request.view_own` (secretaria não ganha esse item; usa `deliberar` = `/solicitacoes?to=me`). **F5.2 ficou de fora.**
- Menu: `MenuLinks` é o único ponto que olha caps; UI `useActions(links)`.
- Seed `secretaria.dev` / `GRR20240005` (four manage + triage/deliberate/`view_curso`). As quatro `*.manage` andam juntas no seed — combo Alunos/Disciplinas reusa `GET /academico/cursos` (`course.manage`). Form F5.7 pede UUID cru (picker = F7.1).

**Não entrou:** F7, F6.1, `cursoIds` no JWT, fila F5.2, Expo.

**8. Expo** — próxima

Mesma API (`/auth`, `/bff`, `/events`, `/formativas`, `/requests`). Token em Keychain/Keystore. NativeWind ≠ Tailwind na web. O esqueleto em `frontend-react-native/` **sozinho não fecha o P0**. Menu (web e Expo futuro) só por `_links` de `GET /auth/me`, nunca `authorities.includes`.

**9. Estágio + COE, TCC, egresso, F6.1**

Módulos novos, um por vez. Parecer COE sempre individual. Egresso read-only. F6.1 é da coordenação, não da secretaria.

Dívida consciente: tabela **ainda aberta** em [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md). Destaques: BFF professor (F3.1); MinIO/S3 (PDF em `bytea`); encerrar evento sem PDF; CA-04; F6.1 e F7; claim `cursoIds`; filtro CAAF por comissão; HostPin em memória; ArchUnit; rate limit em memória. **`/academico/**` e o menu de atalho de dev já não são dívida.**

---

## Como subir

Neste ambiente o Postgres do SO2 é o container **`so2_postgres_iam` na porta 5433**, banco `secretaria_dev` / usuário `secretaria`.

O `docker compose` deste repositório sobe **outro** Postgres em **:5432**, com outro histórico Flyway. **Não aponte a API para 5432.** O mesmo compose sobe o **Mailpit** (SMTP `:1025`, UI `:8025`) — este **é** o do SO2.

```bash
# 1. Banco SO2: so2_postgres_iam :5433 / secretaria_dev
#    Mailpit: docker compose up -d mailpit

# 2. API :8080 (PowerShell — sobrescreve o default :5432 do application.yml)
cd backend
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/secretaria_dev"
$env:MAIL_HOST="localhost"; $env:MAIL_PORT="1025"; $env:MAIL_FROM="so2@localhost"
mvn spring-boot:run

# 3. Web :5174 (vite.config.ts; 5173 não é o default deste repo)
cd frontend-react && npm install && npm run dev

# 4. Mobile (opcional)
cd frontend-react-native && npm install && npx expo start
```

Copie `.env.example` para o shell. Ele já aponta JDBC `:5433` e `FRONTEND_BASE_URL=http://localhost:5174`. O `application.yml` default ainda é JDBC `:5432` e `frontend-base-url` `:5173` — **sobrescreva**.

Proxies Vite → `http://localhost:8080`: `/auth`, `/academico`, `/publico`, `/requests`, `/request-types`, `/bff`, `/events`, `/formativas` (HTML → `index.html`), `/certificates`, `/.well-known`, `/v3`, `/swagger-ui`, `/actuator`. CORS: `http://localhost:5173` e `http://localhost:5174`.

```bash
cd backend && mvn -q test
cd frontend-react && npm test
```

Não é obrigatório rodar a suíte se 5433 + `:8080` + `:5174` já estiverem no ar. Reinicie a API depois de mudar o backend.

---

## Contas de desenvolvimento

Senha de todos: `TroqueEstaSenha1!` (só local; override `IAM_DEV_SEED_PASSWORD`).

**Login aceita só e-mail ou GRR** (`GRR` + 8 dígitos). `professor.dev` ou `aluno.dev` **sem** `@ufpr.br` não são identificadores válidos.

| Usuário | GRR | O que testa |
|---|---|---|
| `aluno.dev@ufpr.br` | `GRR20240001` | Aluno com senha já alterada + cadastro TADS (120 h) + `certificate.view_own` |
| `novo.dev@ufpr.br` | `GRR20240002` | Primeiro acesso (`senhaAlterada=false`); também tem cadastro acadêmico |
| `professor.dev@ufpr.br` | `GRR20240003` | Hospedeiro (`event.manage`, `event.host`) e deliberante (`request.deliberate`). Sem `formative.*` — `GET /formativas?canReview=true` é 403. Seed acadêmico o coloca como **coordenador do TADS** (`CursoEscopoPort`); inofensivo hoje porque não tem `course.manage` |
| `caaf.dev@ufpr.br` | `GRR20240004` | Revisor CAAF (`formative.review`). Sem `event.manage` / `request.deliberate` |
| `secretaria.dev@ufpr.br` | `GRR20240005` | CRUD TADS (`course.manage`, `subject.manage`, `user.manage_students`, `calendar.manage`) + `request.view_curso` + `request.triage` + `request.deliberate`. Sem `formative.review` / `event.manage`. Não recebe deep-link. Nav **sem** o item “Solicitações” do aluno (só Deliberar) |

O seed IAM (`@Profile("dev")`, `iam.seed.enabled`) **substitui** o conjunto de authorities (revoga extras) e registra no log. O seed acadêmico respeita o mesmo flag e **não** apaga secretários extras de TADS (só adiciona `secretaria.dev` se ausente). O PUT de curso **não** é `replaceAll` da lista enviada: re-adiciona o `usuarioId` de quem edita (anti-lockout). Transferir o curso só com o payload depende do picker da F7.1.

A oficina seed `"Oficina Proof of Stay (dev)"` pode já estar `COMPLETA` para `aluno.dev`. O PIN `123456` (`EVENT_DEV_PIN`) vale só para essa oficina e some se a API reiniciar (store em memória). Para repetir o circuito, crie um evento novo em `/professor/eventos` — PIN ou token QR aparece **somente** na host-session.

---

## Smoke (um usuário por papel)

1. **Aluno** — `aluno.dev@ufpr.br` / `GRR20240001` → `/inicio`: saudação, período ou alerta, solicitações, eventos. Horas `N / 120` após CAAF (sem aprovação → `0 / 120`). `/certificados` baixa se `_links.download`. `/eventos` → presença (oficina seed = `SECRET_SINGLE`).
2. **Primeiro acesso** — `novo.dev@ufpr.br` → `/primeiro-acesso`. O resto do sistema responde 403 no gate.
3. **Professor** — `professor.dev@ufpr.br` → `/inicio` 403 honesto (não “Olá, aluno”). Nav **sem** Cursos. `/professor/eventos` hospeda os 4 modos. Encerrar → `CONCLUIDO` sem PDF. `GET /formativas?canReview=true` → 403. Fila `/solicitacoes?to=me` **sem** filtro `view_curso`.
4. **Anônimo** — `/login`, `/contato`, protocolo 200. `/bff`, `/events` e **`/academico/**`** → 401. Número `PROT-…` sai de `/solicitacoes/nova`.
5. **Aluno sem cap acadêmica** — `GET /academico/cursos` → 403. Nav **sem** Cursos / Eventos prof. / Revisão CAAF. `GET /events?mine=true` e host-session → 403.
6. **Secretaria** — `secretaria.dev@ufpr.br` → CRUD TADS 200/201; curso de outro secretário → 404. Nav **sem** Revisão CAAF / Eventos prof. / item Solicitações do aluno (só Deliberar). Fila `view_curso` não lista aluno de outro curso.
7. **Formativas** — presença **COMPLETA** → `PENDENTE_CONFIRMACAO` → aluno confirma → `AGUARDANDO_CAAF`. Em DUAL não nasce na entrada. F5 em `/formativas` recarrega a SPA, não a API.
8. **Deliberação** — aluno abre `DECLARACAO_SIMPLES`. Mailpit (`:8025`) e-mail ao professor com `?token=`. Sem sessão: banner + login. Deferir consome o JTI. Secretaria não recebe deep-link. Sem `request.deliberate` → 403 na transição.
9. **Recuperar senha** — `/recuperar-senha` com e-mail válido ou inexistente: mesmo 202. Só o cadastrado chega no Mailpit (`/nova-senha?token=`).
10. **CAAF** — `caaf.dev@ufpr.br` nav **sem** Eventos prof. / Cursos. Aprova em `/formativas?to=me` → certificado na mesma TX. Aluno: horas `N / 120` e KPI ≥ 1. F0.7 verifica o hash. Sem `formative.review` / `certificate.view_own` → 403.

F0.7 não aceita upload de PDF (CA-04). Encerrar evento continua sem PDF. Egresso não acessa `/formativas` nem `/certificados`.

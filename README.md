# Secretaria Online 2 (SO2)

Plataforma digital da secretaria acadêmica do **SEPT/UFPR**. Este é o repositório oficial do TCC.

O SO2 não substitui o juízo de docentes, comissões ou secretaria. Ele garante trilha de auditoria, integridade de dados e automação de trâmites repetitivos.

**Onde estamos:** P0 fechado na web e no Expo (itens 1–**8**). **Item 9.1 feito** (estágio + parecer individual do orientador). **Item 9.2 feito** (TCC + avaliação individual). **Item 9.3 feito** (portal do egresso read-only). Esta fatia **não** é o portal admin F7 (sem F7.1–F7.9). F4.2 (pool COE), lote CAAF, F6.1, MinIO, o CRUD F5 de estágio, o cadastro F5 de TCC e o diploma (F5.11) continuam fora.

Circuito demonstrável: login → primeiro acesso (senha + LGPD) → `/inicio` do aluno → solicitação + deep-link ao professor → presença **QR\|SECRET × SINGLE\|DUAL** → formativa (`PENDENTE_CONFIRMACAO` → aluno confirma → `AGUARDANDO_CAAF`) → CAAF aprova → certificado oficial (PDF + hash + ED25519) → `/certificados` + F0.7. Secretaria (`secretaria.dev`) opera o CRUD de TADS; aluno/professor/CAAF não veem Cursos. Egresso (`egresso.dev`) entra em `/egresso/inicio` e reemite o PDF com o mesmo hash; `aluno.dev` não entra em `/egresso/**`.

---

## Documentação (fonte de verdade)

Nesta ordem. Se divergirem, não invente regra de negócio.

| Onde | Para quê |
|---|---|
| [`docs/tcc-docs.md`](docs/tcc-docs.md) | Requisitos (RFs/RNFs), atores, regras de negócio. |
| [`docs/telas-figma.md`](docs/telas-figma.md) + [`docs/telas/`](docs/telas/) | Mapa de rotas F0–F8 e detalhe de cada tela. |
| Este README | Estado do **repo**: o que está no ar, como subir, o que não abrir. |
| [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md) | Detalhe da fundação/P0/itens 1–8 e dívida consciente. |
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
├── frontend-react-native/   Expo P0 aluno (login, início, solicitação, presença)
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
| `comunicacao` | Dispatcher Outbox → SMTP (Mailpit). Sem hub F1.6. `estagio.*`, `tcc.submitted` e `tcc.reviewed` fecham SENT sem e-mail (no-op, como `certificado.emitido`) |
| `estagio` | Estágio do aluno + parecer **individual** do orientador (`internship.view_own` / `internship.review`). PDF em `bytea`. Sem lote, sem pool COE, sem CRUD F5 |
| `tcc` | TCC do aluno + avaliação **individual** do orientador/banca (`tcc.view_own` / `tcc.review`). PDF em `bytea`. Sem lote, sem certificado de conclusão, sem CRUD F5 |

Módulos **previstos e ainda sem código**: `auditoria` (módulo dedicado), `arquivos`.

### Flyway (imutável)

`backend/src/main/resources/db/migration/`. **Não edite** migration já aplicada. **Próxima = V014.**

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
| V012 | Estágio (`estagio`, `estagio_documento`, `estagio_parecer`). PDF em `bytea` |
| V013 | TCC (`tcc`, `tcc_membro`, `tcc_avaliacao`). PDF em `bytea`. Um `ATIVO` por aluno |

### Frontend — pastas

`frontend-react/src/`

| Pasta | Uso |
|---|---|
| `pages/secretaria/` | CRUD F5.6–F5.9 |
| `pages/aluno/`, `pages/inicio/`, `pages/professor/` | Aluno, BFF `/inicio`, hospedeiro |
| `pages/solicitacoes/`, `pages/formativas/`, `pages/estagios/`, `pages/tccs/`, `pages/publico/` | Fila/deliberar, CAAF, estágio, TCC, F0 |
| `layouts/` | `AuthLayout` (F0) e `AppLayout`. Nav: `useActions(links)` |
| `hooks/useActions.ts` | Botão só se existir `_links` |
| `api/` | Access token **só em memória** (nunca `localStorage`) |

Rotas planas `/cursos` → `/secretaria/cursos` (redirect temporário). Em geral a UI **não** repete o prefixo da API. Exceção: `/formativas`, `/estagios` e `/tccs` são SPA e API — o proxy devolve `index.html` quando `Accept` inclui `text/html`. `/certificados` (SPA) e `/certificates` (API) não colidem.

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
| `/estagios` | JWT + `internship.view_own` / `internship.review` | `?aluno=me` ou fila `?canReview=true`. Upload PDF, parecer individual, `POST /{id}/encerrar`. Sem lote. Outro aluno ou orientador alheio → 404 |
| `/tccs` | JWT + `tcc.view_own` / `tcc.review` | `?aluno=me` ou fila `?canReview=true`. Upload PDF, avaliação individual. Sem lote e sem certificado. Outro aluno ou banca alheia → 404 |
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
| 8 | Cliente Expo (React Native) | **Feito** — P0 aluno na mesma API; web intacta |
| 9.1 | Estágio + parecer individual | **Feito** — seed, upload PDF, parecer um a um, arquivar |
| 9.2 | TCC + avaliação individual | **Feito** — seed, upload PDF, parecer um a um. Sem certificado |
| 9.3 | Egresso (F2) | **Feito** — painel read-only e reemissão do mesmo PDF. Sem diploma |
| 9.4 | F6.1 configuração do curso | Ainda não |

**1. Formativas** — V007; gatilho na TX de `ConfirmarPresencaUseCase` quando a presença fica **COMPLETA**. Aluno confirma ou cancela. Sem `POST /formativas`. BFF soma `APROVADA` (`null` sem cadastro). Não: CAAF/certificado (3 e 5), lote.

**2. Deliberação** — `POST /requests/{id}/transitions` + fila `?canDeliberate=true`. UI `/solicitacoes?to=me` (secretaria reutiliza). Deep-link no item 6; `request.triage` não recebe o e-mail. Não: lote, F3.1, FORWARD, novo `RequestType`.

**3. CAAF individual** — `AGUARDANDO_CAAF` → `APROVADA`/`INDEFERIDA`. Seed `formative.review` em `caaf.dev`. Fila `?canReview=true`. Horas = `cargaHoraria` já gravada. Não: lote/F4.1, filtro por comissão, COE, comprovante.

**4. QR / `SECRET_DUAL`** — quatro modos no mesmo motor; janelas ao vivo (15 min). Formativa só na presença **COMPLETA** (não na `ENTRADA` de DUAL). Encerrar sem PDF. Não: janelas pré-agendadas, lista de inelegíveis, câmera Expo.

**5. Certificados + F0.7** — V009; emite na aprovação CAAF (mesma TX). PDF `bytea`, SHA-256 + ED25519, JWKS, F0.7 no browser. Sem POST/upload oficial. Não: MinIO, CA-04, revogação, PDF ao encerrar evento. A reemissão do egresso (mesmo hash, sem nova assinatura) entrou no item 9.3.

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

**Não entrou no item 7:** F7, F6.1, `cursoIds` no JWT, fila F5.2, Expo.

**8. Expo** — feito

Cliente `frontend-react-native/` (Expo Router 57 + NativeWind + TanStack Query) no **aluno**. NativeWind **neste** pacote não autoriza Tailwind em `frontend-react/`.

- Login `{ identificador, senha }` (o esqueleto mentia `{ login, senha }` e CPF). Primeiro acesso bloqueia o resto. `/inicio` = `GET /bff/dashboard/aluno` (horas `N / 120` ou `null`, sem mock). Nova solicitação = motor `GET /request-types` + `form_schema` (sem tela `DECLARACAO_SIMPLES`). Presença SECRET (PIN) e QR (`expo-camera`, fallback colar token).
- Menu só com `_links` de `GET /auth/me` + `useActions`. Aluno não vê Cursos / Eventos prof. / Revisão CAAF. Sem `authorities.includes` na nav.
- Refresh **(A)**: cookie `so2_refresh` da web intacto. Nativo não persiste httpOnly; manda `X-SO2-Client: native` no login (JSON com `refreshToken`) e `POST /auth/refresh` / `/logout` com `{ refreshToken }` no body. Valor no Keychain/Keystore (`expo-secure-store`), nunca AsyncStorage. Access token em memória. `deviceUuid` estável no SecureStore.
- Recuperar senha no app (202); o Mailpit continua abrindo a **web** `/nova-senha?token=`. Sem deep-link de deliberação no app.
- Base URL: `EXPO_PUBLIC_API_URL`. Default emulador Android `10.0.2.2:8080`, iOS `localhost:8080`. Aparelho: IP LAN. Detalhe: [`frontend-react-native/README.md`](frontend-react-native/README.md).

**Fora do item 8:** F7, F6.1, COE, lote CAAF, MinIO, BFF professor, FCM, Expo web, CRUD F5 no app, formativas/certificados no Expo. O parecer individual de estágio entrou no **9.1**, não neste item.

**9.1 Estágio + parecer individual** — feito

V012. Seed dev: estágio `ATIVO` do `aluno.dev` no TADS, empresa fictícia, orientador `professor.dev`, documentos TCE e Relatório final em estado que emite `_links.upload`. Secretaria **não** cadastra estágio nesta fatia (CRUD F5 fica para depois).

- Aluno (`internship.view_own`): `GET /estagios?aluno=me` e UI `/estagios` (F1.13). Sem `_links.novo` — não há “Novo estágio”. Detalhe `/estagios/:id` (F1.14) traz documentos e pareceres numa resposta. `POST /estagios/{id}/documentos` (multipart PDF, até 5 MB, `bytea`) → `AGUARDANDO_PARECER`. Outbox `estagio.documento_enviado` na mesma TX.
- Orientador (`internship.review`): fila `GET /estagios?canReview=true` e UI `/estagios?to=me` (F3.6), só estágios em que `id_orientador` é o usuário. `POST .../documentos/{docId}/parecer` `{ acao, parecer }`. Reprovar/indeferir exige parecer ≥ 20 caracteres. `_links.arquivar` só com todos os obrigatórios `APROVADO`; `POST /estagios/{id}/encerrar` → `CONCLUIDO` + Outbox `estagio.encerrado`. Pendência → 422. `CONCLUIDO` imutável.
- HATEOAS `upload` / `revisar` / `aprovar` / `reprovar` / `arquivar` só com capability **e** estado. Menu: rel `estagios` ou `estagios-revisao`. UI `useActions`. Sem `internship.approve_batch`, sem checkbox de lote, sem `/commissions/coe`.
- Fora do vínculo (outro aluno ou orientador que não é o do estágio) → 404. Sem cap → 403. Anônimo → 401.
- Dispatcher: `estagio.documento_enviado`, `estagio.parecer_emitido` e `estagio.encerrado` fecham `SENT` sem SMTP (no-op, como `certificado.emitido`).
- PDF em `bytea`. MinIO e `modules/arquivos` não nasceram. Expo não ganhou F1.13: o menu nativo continua a whitelist P0 e ignora o rel novo.

**9.2 TCC + avaliação individual** — feito

V013. Seed dev: TCC `ATIVO` / `EM_ELABORACAO` do `aluno.dev` no TADS, título fictício, orientador `professor.dev` como único membro da banca (papel `ORIENTADOR`). Secretaria **não** cadastra TCC nesta fatia (CRUD F5 fica para depois).

- Aluno (`tcc.view_own`): `GET /tccs?aluno=me` e UI `/tccs` (F1.15). Sem `_links.novo` — não há “Novo TCC”. Detalhe `/tccs/:id` (F1.16) traz banca, datas e avaliações numa resposta. `POST /tccs/{id}/versao-final` (multipart PDF, até 5 MB, `bytea`) só com `_links.upload-final` (`EM_ELABORACAO` ou `CORRECOES_SOLICITADAS`) → `SUBMETIDO`. Outbox `tcc.submitted` na mesma TX.
- Orientador/banca (`tcc.review`): fila `GET /tccs?canReview=true` (padrão `SUBMETIDO`) e UI `/tccs?to=me` (F3.7), só TCCs em que o usuário é membro. `POST /tccs/{id}/avaliacoes` `{ acao, nota, parecer }`. Indeferir e solicitar correções exigem parecer ≥ 20 caracteres. Aprovar exige parecer não vazio e nota de 0 a 10 (uma casa). A capability da tela nos docs é `tcc.supervise`; esta fatia usa `tcc.review`, no mesmo critério de `internship.review`.
- Download `GET /tccs/{id}/arquivo` com `_links.download` serve o `bytea`. Sem URL pré-assinada.
- HATEOAS `upload-final` / `avaliar` / `download` só com capability **e** estado. Menu: rel `tccs` ou `tccs-revisao`. UI `useActions`. Sem lote, sem checkbox.
- Fora do vínculo (outro aluno ou revisor que não é da banca) → 404. Sem cap → 403. Anônimo → 401.
- Dispatcher: `tcc.submitted` e `tcc.reviewed` fecham `SENT` sem SMTP (no-op).
- **Certificado de conclusão não é emitido.** RF-F3-006 manda emitir quando aprovado e elegível; a elegibilidade (nota mínima e regras de banca da F6.1, consolidação “a definir”, colação F5.11) está ambígua. Dívida na auditoria.
- Expo não ganhou tela de TCC: o menu nativo continua a whitelist P0.

**9.3 Egresso read-only (F2)** — feito

Sem migration nova: reusa `aluno.situacao = EGRESSO` e a tabela `certificado`. Diploma e colação **não** nasceram (F5.11).

- Seed `egresso.dev` / `GRR20240006`, senha padrão do dev, situação EGRESSO, só `alumni.view_own`. Sem `request.open`, `attendance.*`, `formative.*`, `internship.*` nem `tcc.view_own`.
- Menu: rel `egresso-inicio` → `/egresso/inicio`. Sem início do aluno, solicitações, formativas, estágios, TCC ou presença.
- `GET /egressos/me` (`alumni.view_own`). Painel read-only: curso, data de conclusão indisponível, horas formativas validadas, certificados. Diploma e colação ficam nulos. `_links.reemitir` por certificado. Sem `novaSolicitacao`.
- `GET /egressos/me/certificados/{id}/reemissao` devolve o PDF já gravado. O `hash_sha256` e a assinatura não mudam. Não insere certificado e não assina de novo. Quem não é o dono recebe 404.
- Quem tem `alumni.view_own` sem caps de aluno ativo toma 403 em `/bff/dashboard/aluno`, `/request-types`, `/formativas`, `/estagios`, `/tccs` e `/events`. Aluno ativo toma 403 em `/egressos/**`. A UI manda `/erro/403`; “Ir ao início” volta para `/egresso/inicio`.
- FirstAccessGate segue valendo com `senhaAlterada = false`.
- Expo não ganhou a tela. `/certificados` (F1.19) continua de aluno ativo (`certificate.view_own`).

**Ainda não (9.4+):** F6.1 (`course.config`), F4.2 (pool COE: atribuir, nunca aprovar em lote), lote CAAF, MinIO, cadastro F5 de estágio, cadastro F5 de TCC, certificado de conclusão de TCC, diploma/colação (F5.11), BFF professor, F7.

**9. Estágio + COE, TCC, egresso, F6.1**

O item 9 da tabela original era um saco. 9.1 abriu só `modules/estagio` e o parecer individual (RF-F3-005). 9.2 abriu só `modules/tcc` e a avaliação individual (RF-F3-006), sem certificado. 9.3 abriu o portal read-only do egresso (RF-F2-001), sem diploma. Parecer COE continua sempre individual. F6.1 (coordenação) segue fechada.

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

# 4. Mobile P0 (Expo Go / emulador)
cd frontend-react-native && npm install && npx expo start
# Aparelho físico: EXPO_PUBLIC_API_URL=http://<IP-LAN>:8080
```

Copie `.env.example` para o shell. Ele já aponta JDBC `:5433` e `FRONTEND_BASE_URL=http://localhost:5174`. O `application.yml` default ainda é JDBC `:5432` e `frontend-base-url` `:5173` — **sobrescreva**.

Proxies Vite → `http://localhost:8080`: `/auth`, `/academico`, `/publico`, `/requests`, `/request-types`, `/bff`, `/events`, `/formativas` (HTML → `index.html`), `/estagios` (HTML → `index.html`), `/tccs` (HTML → `index.html`), `/certificates`, `/.well-known`, `/v3`, `/swagger-ui`, `/actuator`. CORS: `http://localhost:5173` e `http://localhost:5174`. Nativo não passa por CORS. Expo web **não** entrou nesta fatia (sem origem extra e sem `*`).

```bash
cd backend && mvn -q test
cd frontend-react && npm test
cd frontend-react-native && npm test
```

Não é obrigatório rodar a suíte se 5433 + `:8080` + `:5174` já estiverem no ar. Reinicie a API depois de mudar o backend.

---

## Contas de desenvolvimento

Senha de todos: `TroqueEstaSenha1!` (só local; override `IAM_DEV_SEED_PASSWORD`).

**Login aceita só e-mail ou GRR** (`GRR` + 8 dígitos). `professor.dev` ou `aluno.dev` **sem** `@ufpr.br` não são identificadores válidos.

| Usuário | GRR | O que testa |
|---|---|---|
| `aluno.dev@ufpr.br` | `GRR20240001` | Aluno com senha já alterada + cadastro TADS (120 h) + `certificate.view_own` + `internship.view_own` + `tcc.view_own`. Estágio e TCC seed no TADS (não cria outro) |
| `novo.dev@ufpr.br` | `GRR20240002` | Primeiro acesso (`senhaAlterada=false`); também tem cadastro acadêmico. Tem `internship.view_own` e `tcc.view_own` e listas vazias |
| `professor.dev@ufpr.br` | `GRR20240003` | Hospedeiro (`event.manage`, `event.host`), deliberante (`request.deliberate`), orientador do estágio (`internship.review`) e da banca do TCC (`tcc.review`). Sem `formative.*` — `GET /formativas?canReview=true` é 403. Sem `internship.view_own` / `tcc.view_own` — `GET /estagios?aluno=me` e `GET /tccs?aluno=me` são 403. Seed acadêmico o coloca como **coordenador do TADS** (`CursoEscopoPort`); inofensivo hoje porque não tem `course.manage` |
| `caaf.dev@ufpr.br` | `GRR20240004` | Revisor CAAF (`formative.review`). Sem `event.manage` / `request.deliberate` |
| `secretaria.dev@ufpr.br` | `GRR20240005` | CRUD TADS (`course.manage`, `subject.manage`, `user.manage_students`, `calendar.manage`) + `request.view_curso` + `request.triage` + `request.deliberate`. Sem `formative.review` / `event.manage`. Não recebe deep-link. Nav **sem** o item “Solicitações” do aluno (só Deliberar) |

O seed IAM (`@Profile("dev")`, `iam.seed.enabled`) **substitui** o conjunto de authorities (revoga extras) e registra no log. O seed acadêmico respeita o mesmo flag e **não** apaga secretários extras de TADS (só adiciona `secretaria.dev` se ausente). O PUT de curso **não** é `replaceAll` da lista enviada: re-adiciona o `usuarioId` de quem edita (anti-lockout). Transferir o curso só com o payload depende do picker da F7.1.

A oficina seed `"Oficina Proof of Stay (dev)"` pode já estar `COMPLETA` para `aluno.dev`. O PIN `123456` (`EVENT_DEV_PIN`) vale só para essa oficina e some se a API reiniciar (store em memória). Para repetir o circuito, crie um evento novo em `/professor/eventos` — PIN ou token QR aparece **somente** na host-session.

---

## Smoke (um usuário por papel)

1. **Aluno** — `aluno.dev@ufpr.br` / `GRR20240001` → `/inicio`: saudação, período ou alerta, solicitações, eventos. Horas `N / 120` após CAAF (sem aprovação → `0 / 120`). `/certificados` baixa se `_links.download`. `/eventos` → presença (oficina seed = `SECRET_SINGLE`).
2. **Primeiro acesso** — `novo.dev@ufpr.br` → `/primeiro-acesso`. O resto do sistema responde 403 no gate.
3. **Professor** — `professor.dev@ufpr.br` → `/inicio` 403 honesto (não “Olá, aluno”). Nav **sem** Cursos. `/professor/eventos` hospeda os 4 modos. Encerrar → `CONCLUIDO` sem PDF. `GET /formativas?canReview=true` → 403. Fila `/solicitacoes?to=me` **sem** filtro `view_curso`. Nav tem **Revisão de estágios** (`/estagios?to=me`) e **Revisão de TCCs** (`/tccs?to=me`) e não tem os itens Estágios/TCCs do aluno.
4. **Anônimo** — `/login`, `/contato`, protocolo 200. `/bff`, `/events` e **`/academico/**`** → 401. Número `PROT-…` sai de `/solicitacoes/nova`.
5. **Aluno sem cap acadêmica** — `GET /academico/cursos` → 403. Nav **sem** Cursos / Eventos prof. / Revisão CAAF. Nav **com** Estágios e TCCs e **sem** as filas de revisão. `GET /events?mine=true` e host-session → 403.
6. **Secretaria** — `secretaria.dev@ufpr.br` → CRUD TADS 200/201; curso de outro secretário → 404. Nav **sem** Revisão CAAF / Eventos prof. / item Solicitações do aluno (só Deliberar). Fila `view_curso` não lista aluno de outro curso.
7. **Formativas** — presença **COMPLETA** → `PENDENTE_CONFIRMACAO` → aluno confirma → `AGUARDANDO_CAAF`. Em DUAL não nasce na entrada. F5 em `/formativas` recarrega a SPA, não a API.
8. **Deliberação** — aluno abre `DECLARACAO_SIMPLES`. Mailpit (`:8025`) e-mail ao professor com `?token=`. Sem sessão: banner + login. Deferir consome o JTI. Secretaria não recebe deep-link. Sem `request.deliberate` → 403 na transição.
9. **Recuperar senha** — `/recuperar-senha` com e-mail válido ou inexistente: mesmo 202. Só o cadastrado chega no Mailpit (`/nova-senha?token=`).
10. **CAAF** — `caaf.dev@ufpr.br` nav **sem** Eventos prof. / Cursos / Estágios. Aprova em `/formativas?to=me` → certificado na mesma TX. Aluno: horas `N / 120` e KPI ≥ 1. F0.7 verifica o hash. Sem `formative.review` / `certificate.view_own` → 403.
11. **Estágio** — `aluno.dev` em `/estagios` vê o seed (sem “Novo estágio”), abre o detalhe e envia o PDF se `_links.upload`. `professor.dev` em `/estagios?to=me` emite parecer no documento e arquiva quando todos os obrigatórios estão `APROVADO`. Sem lote. Anônimo → 401. Outro aluno no id → 404. Secretaria não ganha item de menu de estágio.
12. **TCC** — `aluno.dev` em `/tccs` vê o seed (sem “Novo TCC”), abre o detalhe e envia o PDF se `_links.upload-final`. `professor.dev` em `/tccs?to=me` só vê o TCC depois do envio e registra nota + parecer um a um. Sem lote e sem certificado. Anônimo → 401. Outro aluno no id → 404. Secretaria não ganha item de menu de TCC.

F0.7 não aceita upload de PDF (CA-04). Encerrar evento continua sem PDF. Egresso não acessa `/formativas` nem `/certificados`.

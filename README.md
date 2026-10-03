# Secretaria Online 2 (SO2)

Plataforma digital da secretaria acadêmica do **SEPT/UFPR**. Este é o repositório oficial do TCC.

O SO2 não substitui o juízo de docentes, comissões ou secretaria. Ele garante trilha de auditoria, integridade de dados e automação de trâmites repetitivos.

**Onde estamos:** P0 fechado na web e no Expo (itens 1–**8**). **Item 9 neste recorte:** 9.1–9.4 no ar e **9.5 pool COE** (só atribuição). Fatias **10–22** no ar (cadastro F5 de estágio, F7.1 usuários/picker, cadastro F5 de TCC, BFF professor F3.1, pool/lote CAAF F4.1, MinIO + `modules/arquivos`, diploma/colação F5.11, F6.2 relatórios da coordenação, Mobile P2 no Expo, F5.18 estatísticas da secretaria, F5.2+F5.5 fila central e atrasados, F5.1 dashboard da secretaria, F1.3–F1.5 perfil). Esta fatia **não** é o portal admin F7 completo (sem F7.2–F7.9).

Circuito demonstrável: login → primeiro acesso (senha + LGPD) → `/inicio` do aluno → solicitação + deep-link ao professor → presença **QR\|SECRET × SINGLE\|DUAL** → formativa (`PENDENTE_CONFIRMACAO` → aluno confirma → `AGUARDANDO_CAAF`) → CAAF aprova → certificado oficial (PDF + hash + ED25519) → `/certificados` + F0.7. Secretaria (`secretaria.dev`) opera o CRUD de TADS, estágios e TCCs; aluno/professor/CAAF não veem Cursos. Egresso (`egresso.dev`) entra em `/egresso/inicio` e reemite o PDF com o mesmo hash; `aluno.dev` não entra em `/egresso/**`.

### CAAF e COE (o que são)

São **comissões acadêmicas do SEPT**, não “papéis de tela”. O SO2 não substitui o juízo delas: só registra o parecer, a trilha e (quando a regra manda) emite o certificado. Fonte: glossário e RF-F3-004 / RF-F4-001 / RF-F3-005 / RF-F4-002 em [`docs/tcc-docs.md`](docs/tcc-docs.md).

| Sigla | Nome | O que decide | Neste repo |
|---|---|---|---|
| **CAAF** | Comissão de Atividades Acadêmicas Formativas | Avalia e **valida horas** de atividades formativas (oficina, evento, comprovante). Aprovar dispara certificado oficial. Indeferir exige parecer. | Fila individual **feita** (`caaf.dev`, `/formativas?to=me`, cap `formative.review`). Pool/lote F4.1 **feito** (`/comissoes/caaf`). Lote só aprova item cuja **presença já foi validada** pelo sistema. |
| **COE** | Comissão de Estágios | Acompanha estágio curricular: **atribui orientador** e emite parecer **por documento**. | Pool F4.2 **feito** (`/comissoes/coe`, só atribuição). Parecer continua **sempre individual** (9.1). Sem “aprovar estágio em lote”. |

**CAAF ≠ COE.** Formativa/horas/certificado é CAAF. Estágio/TCE/relatório/orientador é COE. Um professor pode estar nas duas (caps diferentes); a UI não decide isso por `user.role`.

No fluxo do aluno, `AGUARDANDO_CAAF` significa: a formativa já existe (em geral depois de presença **COMPLETA** + confirmação) e espera o parecer da comissão. Sem aprovação da CAAF não há hora validada nem PDF oficial.

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
├── frontend-react-native/   Expo aluno/egresso (P0 + P2: formativas, estágio, TCC, certificados)
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
| `iam` | Login, refresh, logout, primeiro acesso + LGPD, recuperação via Outbox despachada. JWT RS256 15 min; cookie `so2_refresh`; senha só Argon2id. JWT de deliberação (72 h). Autogestão `GET`/`PATCH /me`, senha, sessões e preferências (V021) |
| `solicitacoes` | Motor `RequestType` + `form_schema` + `workflow_json`. Seed `DECLARACAO_SIMPLES`. Protocolo público. Fila e transições autenticadas (`request.*`) |
| `presenca` | Evento + Proof of Stay **QR\|SECRET × SINGLE\|DUAL**. Segredo em claro só na host-session. Sem geofence |
| `bff` | `GET /bff/dashboard/aluno`, `/professor` e `/secretary` (agrega; degrada por bloco). Escopo da secretaria em `CursoEscopoPort` |
| `formativas` | Gatilho na mesma TX da presença **COMPLETA**. Aluno confirma; CAAF aprova/indefere (individual) + pool/lote F4.1. Aprovar emite certificado |
| `certificados` | Emissão só pelo sistema na aprovação CAAF. PDF no MinIO (`storage_key`). SHA-256 + ED25519 + F0.7. Sem upload oficial |
| `comunicacao` | Dispatcher Outbox → SMTP (Mailpit). Sem hub F1.6. `estagio.*`, `tcc.submitted` e `tcc.reviewed` fecham SENT sem e-mail (no-op, como `certificado.emitido`) |
| `estagio` | Estágio do aluno + parecer **individual** do orientador (`internship.view_own` / `internship.review`) + pool COE (só atribuição). PDF no MinIO. Sem lote de parecer |
| `tcc` | TCC do aluno + avaliação individual + cadastro F5 (`tcc.manage`). PDF no MinIO. Sem lote, sem certificado de conclusão |
| `coordenacao` | F6.1 `GET`/`PATCH /coordenacao/cursos/{id}/config` (`course.config` + `idCoordenador`). F6.2 via `modules/reports` |
| `egresso` | F2.1 `GET /egressos/me` + reemissão/download pré-assinados (`alumni.view_own`). Diploma/colação preenchidos após F5.11 |
| `diplomas` | F5.11 colação em lote, entrega física e PDF no MinIO (`diploma.register`). Escopo `curso_secretario` no use case: `403` com `cursoId` explícito fora, `404` por id |
| `reports` | F6.2 `GET /reports/coordinator` + F5.18 `GET /reports/secretary` |
| `arquivos` | Porta S3-compatível (MinIO/dev). Upload server-side + download por URL pré-assinada (TTL 15 min). Sem antivírus/versionamento |

Módulos **previstos e ainda sem código**: `auditoria` (módulo dedicado; `audit_log` mora no `iam`).

### Flyway (imutável)

`backend/src/main/resources/db/migration/`. **Não edite** migration já aplicada. **Próxima = V022.**

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
| V009 | Certificados (`bytea` legado; UNIQUE `id_formativa` / `hash_sha256`) |
| V010 | Outbox dispatcher (`last_error`, `processed_at`) |
| V011 | `curso_secretario` (N:N; PK `id_curso`+`id_usuario`) |
| V012 | Estágio (`estagio`, `estagio_documento`, `estagio_parecer`). PDF legado em `bytea` |
| V013 | TCC (`tcc`, `tcc_membro`, `tcc_avaliacao`). PDF legado em `bytea`. Um `ATIVO` por aluno |
| V014 | F6.1 (`curso_configuracao` + `elegibilidade_horas`). Horas mínimas continuam em `curso` |
| V015 | Pool COE (`coe_membro` + `estagio.id_orientador` opcional). Sem tabela genérica de comissão |
| V016 | `usuario.nome` (F7.1) |
| V017 | Pool CAAF (`comissao_membro` genérico + `formativa.id_responsavel`) |
| V018 | `storage_key` em certificado/estágio_documento/tcc; bytea opcional (migração → MinIO) |
| V019 | `diploma` (colação, entrega, `storage_key`; UNIQUE por aluno e por número) |
| V020 | `solicitacao.deliberador_id` (atribuição da fila F5.2) |
| V021 | Perfil (`nome_social`, `telefone`, `identidade_genero`, `foto_storage_key`), `refresh_token.user_agent`, `notificacao_preferencia` |

### Frontend — pastas

`frontend-react/src/`

| Pasta | Uso |
|---|---|
| `pages/secretaria/` | CRUD F5.6–F5.9 + cadastro F5 de estágio/TCC + diplomas, estatísticas e atrasados |
| `pages/aluno/`, `pages/inicio/`, `pages/professor/` | Aluno, BFF `/inicio`, hospedeiro |
| `pages/perfil/` | F1.3–F1.5 perfil, segurança e notificações |
| `pages/solicitacoes/`, `pages/formativas/`, `pages/estagios/`, `pages/tccs/`, `pages/publico/` | Fila/deliberar, CAAF, estágio, TCC, F0 |
| `pages/coordenacao/` | F6.1 `/coordenacao/cursos/:id/configurar` |
| `pages/comissoes/` | F4.2 `/comissoes/coe` (só atribuição) + F4.1 `/comissoes/caaf` (atribuir + lote presença) |
| `layouts/` | `AuthLayout` (F0) e `AppLayout`. Nav: `useActions(links)` |
| `hooks/useActions.ts` | Botão só se existir `_links` |
| `api/` | Access token **só em memória** (nunca `localStorage`) |

Rotas planas `/cursos` → `/secretaria/cursos` (redirect temporário). Em geral a UI **não** repete o prefixo da API. Exceção: `/formativas`, `/estagios`, `/tccs`, `/coordenacao` e `/comissoes` são SPA e API — o proxy devolve `index.html` quando `Accept` inclui `text/html`. `/certificados` (SPA) e `/certificates` (API) não colidem.

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
| `/me` | JWT + `user.update_own_profile` | F1.3–F1.5. `GET`/`PATCH /me` (merge parcial; GRR e e-mail institucional ignorados). `POST /me/foto` multipart (server-side, URL pré-assinada). `PATCH /me/password` (senha atual; 401 não troca o hash). `GET /me/sessions` + `DELETE` só com `_links.encerrar`. `GET`/`PATCH /me/notifications` (e-mail e in-app, DND, digest). Sem cap → 403. Anônimo → 401 |
| `/communications` | JWT + `communication.read` / `communication.publish_class` | F1.6 + F3.8. `GET /communications` (aba, `lido`, `tipo`). `POST /{id}/read` só do destinatário. `GET /communications/audiencias` + `POST /communications` (turma ou curso; Outbox na mesma TX). Sem cap → 403. Audiência fora do escopo → 422. Anônimo → 401. Sem `system.broadcast` |
| `/publico/**` | Anônimo | Contato; protocolo; verificação de certificado |
| `/academico/**` | JWT + capability da tela | CRUD. Anônimo → 401. Sem cap → 403. Fora do escopo (curso) → 404. GRR/e-mail malformado → 422 `validation-error`. `_links.disciplinas` só com `subject.manage`. Períodos: `calendar.manage` all-or-nothing |
| `/request-types`, `/requests/**` | JWT + `request.*` (**não** anônimo) | Motor. Inbox `?canDeliberate=true`. Fila do curso (`request.view_curso`) com `?slaBreached` e `slaStatus`. CSV `?format=csv`. `PATCH /requests/bulk`. `POST /{id}/transitions` |
| `/events` | JWT + `attendance.*` / `event.*` | Presença v4.1 (QR\|SECRET × SINGLE\|DUAL) |
| `/bff/dashboard/aluno` | JWT + `dashboard.view_own` + (`attendance.view_open` **ou** `request.view_own`) | Dashboard agregado (HTTP 200; degrada por bloco) |
| `/bff/dashboard/professor` | JWT + `dashboard.view_self_professor` | Dashboard agregado do professor (HTTP 200; degrada por bloco; CAAF só com `formative.review`) |
| `/bff/dashboard/secretary` | JWT + `dashboard.view_secretary` | F5.1 da secretaria. KPIs, fila ≤ 10, SLA e agenda do dia, só cursos de `curso_secretario`. Sem curso → 403. Sem cap → 403. QuickTiles só por `_links` (sem importações) |
| `/formativas` | JWT + `formative.view_own` / `confirm_own` / `submit` / `review` | Lista do aluno. `POST` multipart (`formative.submit`) nasce `AGUARDANDO_CAAF` origem `COMPROVANTE`. `_links.nova` só com `formative.submit`. Lote só em `/comissoes/caaf` e só `PRESENCA_VALIDADA`. Aprovar emite certificado |
| `/certificates` | JWT + `certificate.view_own` | Lista/download do dono. Sem POST. Outro aluno → 404 |
| `/estagios` | JWT + `internship.view_own` / `internship.review` / `internship.manage` | `?aluno=me`, fila `?canReview=true`, escopo `?escopo=cursos` + `POST`/`PUT` (`internship.manage`). Upload PDF, parecer individual, `POST /{id}/encerrar`. Sem lote. Outro aluno, orientador alheio ou fora do escopo → 404 |
| `/tccs` | JWT + `tcc.view_own` / `tcc.review` / `tcc.manage` | `?aluno=me`, fila `?canReview=true`, escopo `?escopo=cursos` + `POST`/`PUT` (`tcc.manage`). Upload PDF, avaliação individual. Sem lote e sem certificado. Outro aluno ou banca alheia → 404 |
| `/diplomas` | JWT + `diploma.register` | Colação em lote, entrega física e PDF. `cursoId` explícito fora do escopo, ou ator sem curso vinculado → 403. Diploma por id fora do escopo ou inexistente → 404 (a mesma resposta). Sem cap → 403 |
| `/coordenacao/cursos/{id}/config` | JWT + `course.config` **e** `idCoordenador = usuarioId` | GET/PATCH F6.1. Sem `course.manage`. Outro curso → 403. Anônimo → 401. Sem `/courses/{id}/config` |
| `/reports/coordinator` | JWT + `report.view_coordinator` | F6.2 do coordenador. Outro curso ou nenhum curso coordenado → 403. Sem cap → 403 |
| `/reports/secretary` | JWT + `report.view_secretary` | F5.18 da secretaria (`curso_secretario`). Fora do escopo ou sem curso vinculado → 403. Sem cap → 403 |
| `/admin/usuarios` | JWT + `user.manage_all` | F7.1 CRUD. `POST /{id}/reset-senha` exige `user.reset_password`. Sem cap → 403 |
| `/iam/usuarios` | JWT + `user.manage_all` **ou** `course.manage` | Picker (nome, e-mail, GRR) para vínculos da F5.7. Sem essas caps → 403 |
| `/comissoes/coe` | JWT + `internship.review` | Pool F4.2: KPIs + não atribuídos + “comigo”. `POST /comissoes/coe/atribuicoes` `{ estagioId, assigneeId }`. Sem cap → 403. Estágio fora do curso da comissão → 404. Sem parecer em lote |
| `/comissoes/caaf` | JWT + `formative.review` | Pool F4.1: KPIs + não atribuídas + “comigo”. `POST /atribuicoes` `{ formativaId, assigneeId }`. `POST /lote` `{ ids, decisao: APROVADA }` só `PRESENCA_VALIDADA`. Sem cap → 403. Cross-curso → 404 |
| `/.well-known/jwks.json` | Anônimo | Chave pública ED25519 (par separado do RSA do JWT) |

JSON camelCase. Datas ISO-8601 UTC. Listagens `Pageable` (20 / máx. 100) com `_links`.

---

## Fatias (1–36)

Ordem de dependência real — um bounded context por fatia. O mapa F0–F8 do Figma descreve o produto; **não é cronograma**. Detalhe das fatias feitas está nas seções numeradas abaixo da tabela.

| # | Fatia | Depende de | Entrega / estado | Fora desta fatia |
|---|---|---|---|---|
| 1 | Formativas via presença | P0 | **Feito** — V007; gatilho na TX de presença `COMPLETA`; aluno confirma/cancela; BFF soma `APROVADA` | CAAF/certificado (3, 5), lote |
| 2 | Deliberação do motor | P0 | **Feito** — `POST /requests/{id}/transitions` + fila `?canDeliberate=true`; UI `/solicitacoes?to=me` | Lote, F3.1, FORWARD, novo RequestType |
| 3 | CAAF individual | 1 | **Feito** — `AGUARDANDO_CAAF` → `APROVADA`/`INDEFERIDA`; seed `formative.review`; fila `?canReview=true` | Lote/F4.1 (14), filtro por comissão, COE, comprovante |
| 4 | Presença v4.1 (QR / `SECRET_DUAL`) | P0 | **Feito** — quatro modos no mesmo motor; janelas ao vivo 15 min | Janelas pré-agendadas, lista de inelegíveis, câmera Expo |
| 5 | Certificados oficiais + F0.7 | 3 | **Feito** — V009; emite na aprovação CAAF; PDF + SHA-256 + ED25519 + JWKS + F0.7 | MinIO (15), CA-04, revogação; reemissão egresso no 9.3 |
| 6 | Dispatcher do Outbox + SMTP + deep-link | 2 | **Feito** — at-least-once + Mailpit; `?token=` exige sessão; JTI na TX | Hub F1.6 (23), F3.8, F7.5, push |
| 7 | FGAC acadêmico + nav | P0 | **Feito** — fecha `/academico/**` e a nav; **não** é o portal admin F7 | F7 completo, claim `cursoIds`, F5.2 (20) |
| 8 | Cliente Expo (React Native) | P0 | **Feito** — P0 aluno na mesma API; web intacta | FCM, Expo web; Mobile P2 no 18 |
| 9.1 | Estágio + parecer individual | 8 | **Feito** — seed, upload PDF, parecer um a um, arquivar | Pool COE (9.5), lote, cadastro F5 (10) |
| 9.2 | TCC + avaliação individual | 8 | **Feito** — seed, upload PDF, parecer um a um | Certificado de conclusão, lote, cadastro F5 (12) |
| 9.3 | Egresso (F2) | 5 | **Feito** — painel read-only e reemissão do mesmo PDF | Diploma/colação (16), lista egressos (26) |
| 9.4 | F6.1 configuração do curso | 7 | **Feito** — horas, calendário 15/18, banca e regimento | F6.2 (17) |
| 9.5 | Pool COE (F4.2) | 9.1 | **Feito** — atribuir (self/colega/lote); parecer continua individual no 9.1 | Lote CAAF (14), parecer em lote (proibido) |
| 10 | Cadastro F5 de estágio (RF-F1-007) | 9.1, 9.5 | **Feito** — `POST`/`PUT /estagios` (`internship.manage`), aluno via `GET /academico/alunos`, `id_orientador` nulo, UI `/secretaria/estagios` | Parecer/encerramento (9.1), lote, MinIO, TCC |
| 11 | F7.1 + F7.8 usuários e picker | 10 | **Feito** — CRUD `/admin/usuarios`, reset-senha, picker `GET /iam/usuarios`, seed `admin.dev`, V016 `usuario.nome` | Perfis/matriz FGAC (32), jobs, auditoria, `cursoIds` no JWT |
| 12 | Cadastro F5 de TCC (RF-F1-008) | 11 | **Feito** — `POST`/`PUT /tccs` (`tcc.manage`), banca via picker, UI `/secretaria/tccs` | Certificado de conclusão, lote, MinIO |
| 13 | F3.1 BFF do professor | 9.1, 9.2 | **Feito** — `GET /bff/dashboard/professor`; `/inicio` deixa de ser 403 para professor puro | Dashboard secretaria (21), KPI de SLA, Expo |
| 14 | F4.1 pool + lote CAAF | 3, 9.5 | **Feito** — `GET`/`POST /comissoes/caaf`; lote só `PRESENCA_VALIDADA`; V017 | Indeferir em lote, comprovante manual (24) |
| 15 | MinIO + `modules/arquivos` | 10, 12 | **Feito** — MinIO + `modules/arquivos`, PDF com `storage_key` (V018), URL pré-assinada | Antivírus, versionamento, export assíncrona (29) |
| 16 | F5.11 diploma e colação | 15, 11 | **Feito** — V019, wizard `/secretaria/diplomas`, colação em lote, F2.1 preenchido, escopo por curso (`403` no `cursoId` explícito, `404` por id) | Lista/exportação de egressos (26) |
| 17 | F6.2 relatórios da coordenação | 9.4, 14, 16 | **Feito** — `GET /reports/coordinator` + `/coordenacao/relatorios` | Comparativo entre cursos, export, F5.18 (19) |
| 18 | Mobile P2 (Expo) | 9.1, 9.2, 9.3 | **Feito** — formativas, estágio, TCC, certificados e egresso no app; menu via `_links` | Deliberação/CAAF/F5 no app; FCM; Expo web |
| 19 | F5.18 estatísticas da secretaria | 17 | **Feito** — `GET /reports/secretary` + `/secretaria/estatisticas` | Export, materialização/cache de métrica |
| 20 | F5.2 + F5.5 fila central e atrasados | 2, 7 | **Feito** — `GET /requests` (`view_curso`, `slaBreached`, `slaStatus`), `PATCH /requests/bulk`, CSV, menu `fila-solicitacoes` + `atrasados`, V020 | Solicitação interna (28), editor RequestType (33), FORWARD |
| 21 | F5.1 dashboard da secretaria | 20, 13 | **Feito** — `GET /bff/dashboard/secretary` (`dashboard.view_secretary`); `/inicio` via rel `painel`; KPIs, fila, SLA e agenda | F6.2 já feito, kanban (P3), Redis, importações (29) |
| 22 | F1.3–F1.5 perfil, segurança e notificações | 11, 15 | **Feito** — `GET`/`PATCH /me` (`user.update_own_profile`), senha, sessões e preferência de canal/DND/digest | Push, hub (23), job de digest, SSO, Expo destas telas |
| 23 | F1.6 + F3.8 hub de comunicação | 6 | **Feito** — `GET`/`POST /communications` (inbox + comunicado Markdown via Outbox; V022) | Templates (34), push/FCM, digest |
| 24 | F1.11 formativa manual com comprovante | 15, 14 | **Feito** — `POST /formativas` (`formative.submit`) com upload; nasce `AGUARDANDO_CAAF` origem `COMPROVANTE` | OCR, validação automática de CH |
| 25 | F5.13 + F1.20 atendimentos | 11 | Módulo `atendimentos`: registro imutável + ciência do aluno | Fila/SLA de atendimento, agendamento |
| 26 | F5.10 egressos (lista + CSV) | 16 | `/secretaria/egressos` (`alumni.list`) com filtros e CSV síncrono | Exportação assíncrona (29), colação (já na 16) |
| 27 | F5.14 + F5.15 eventos da secretaria | 15 | `/secretaria/eventos` + operação sobre o motor v4.1; `PATCH`/`DELETE /events/{id}` | Janelas pré-agendadas, lista ao vivo de inelegíveis |
| 28 | F5.3 + F5.12 solicitação interna e autorização de imagem | 20, 15 | `POST /requests { onBehalfOf }` + fila `AUTORIZACAO_IMAGEM` com lote | RequestType pelo editor (33), FORWARD |
| 29 | F5.16 + F5.17 importações e exportações | 15, 11 | Wizard CSV/XLSX + export assíncrona com job e download pré-assinado | RabbitMQ, agendamento recorrente |
| 30 | F7.7 audit-log + módulo `auditoria` | 11 | Extrai `audit_log` para `modules/auditoria`; busca imutável (`audit.read`) | Retenção/export; DELETE na trilha (proibido) |
| 31 | F7.6 Outbox e jobs | 30 | `/admin/jobs` (`system.observe`): reentrega e alerta de latência | Grafana (P3), fila externa |
| 32 | F7.2 + F7.3 perfis e matriz FGAC | 11, 30 | `role` + `role_authority`, CRUD de perfil e matriz | Editor de workflow (33), claim `cursoIds` |
| 33 | F7.4 editor de RequestType | 32 | Editor de `form_schema` + `workflow_json` com versionamento e publicação | Migrar solicitação em voo, FORWARD automático |
| 34 | F7.5 templates de comunicação | 23, 33 | CRUD de template Markdown versionado; dispatcher renderiza template | Push, A/B de mensagem |
| 35 | F8.1 busca global | 11, 30 | `GET /search?q=` filtrada por capabilities + paleta `Ctrl+K` | Índice externo, ranking semântico |
| 36 | F8.2 suporte e FAQ | 33 | FAQ + ticket via `RequestType=SUPORTE_TECNICO` | Chat, base de conhecimento editável |

Duas observações sobre as fatias 10 e 12: a spec só diz que estágio e TCC são “registrados pela secretaria” (RF-F1-007 / RF-F1-008) e o mapa F0–F8 **não tem tela F5 para isso** — as rotas `/secretaria/estagios` e `/secretaria/tccs` são derivadas. São dois bounded contexts, por isso duas fatias; entre elas entra a 11 (banca de TCC precisa do picker de usuários).

### Detalhe das fatias feitas

O item 9 da tabela original era um saco. 9.1–9.5 fecharam o recorte: estágio com parecer individual (RF-F3-005), pool COE só de atribuição (RF-F4-002), TCC com avaliação individual (RF-F3-006, sem certificado), portal read-only do egresso (RF-F2-001, sem diploma) e F6.1 da coordenação (RF-F6-001). Parecer COE continua sempre individual. F6.2 entrou na fatia 17; F7 completo segue fora.

Dívida consciente: tabela **ainda aberta** em [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md). Destaques: encerrar evento sem PDF; CA-04; F7; claim `cursoIds`; HostPin em memória; ArchUnit; rate limit em memória. **`/academico/**`, menu de atalho de dev, BFF professor, filtro CAAF por comissão (V017), MinIO/`modules/arquivos` (V018), diploma/colação (V019), F6.2/`GET /reports/coordinator`, F5.18/`GET /reports/secretary` e F5.2/F5.5 fila+atrasados já não são dívida.**

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
- `request.view_curso` filtra a fila; professor só com `request.deliberate` **não** herda o filtro. Secretaria tem triage+deliberate e **não** recebe deep-link. Rel de menu `solicitacoes` exige `request.view_own`; a fila central F5.2 usa `fila-solicitacoes` (fatia 20).
- Menu: `MenuLinks` é o único ponto que olha caps; UI `useActions(links)`.
- Seed `secretaria.dev` / `GRR20240005` (four manage + triage/deliberate/`view_curso`). As quatro `*.manage` andam juntas no seed — combo Alunos/Disciplinas reusa `GET /academico/cursos` (`course.manage`). Form F5.7 pede UUID cru (picker = F7.1).

**Não entrou no item 7:** F7, F6.1, `cursoIds` no JWT, Expo. Fila F5.2 entrou na **20**.

**8. Expo** — feito

Cliente `frontend-react-native/` (Expo Router 57 + NativeWind + TanStack Query) no **aluno**. NativeWind **neste** pacote não autoriza Tailwind em `frontend-react/`.

- Login `{ identificador, senha }` (o esqueleto mentia `{ login, senha }` e CPF). Primeiro acesso bloqueia o resto. `/inicio` = `GET /bff/dashboard/aluno` (horas `N / 120` ou `null`, sem mock). Nova solicitação = motor `GET /request-types` + `form_schema` (sem tela `DECLARACAO_SIMPLES`). Presença SECRET (PIN) e QR (`expo-camera`, fallback colar token).
- Menu só com `_links` de `GET /auth/me` + `useActions`. Aluno não vê Cursos / Eventos prof. / Revisão CAAF. Sem `authorities.includes` na nav.
- Refresh **(A)**: cookie `so2_refresh` da web intacto. Nativo não persiste httpOnly; manda `X-SO2-Client: native` no login (JSON com `refreshToken`) e `POST /auth/refresh` / `/logout` com `{ refreshToken }` no body. Valor no Keychain/Keystore (`expo-secure-store`), nunca AsyncStorage. Access token em memória. `deviceUuid` estável no SecureStore.
- Recuperar senha no app (202); o Mailpit continua abrindo a **web** `/nova-senha?token=`. Sem deep-link de deliberação no app.
- Base URL: `EXPO_PUBLIC_API_URL`. Default emulador Android `10.0.2.2:8080`, iOS `localhost:8080`. Aparelho: IP LAN. Detalhe: [`frontend-react-native/README.md`](frontend-react-native/README.md).

**Fora do item 8:** F7, F6.1, COE, lote CAAF, MinIO, BFF professor, FCM, Expo web, CRUD F5 no app. Formativas/certificados/estágio/TCC/egresso no Expo entraram no **18**. O parecer individual de estágio (API/web) entrou no **9.1**.

**9.1 Estágio + parecer individual** — feito

V012. Seed dev: estágio `ATIVO` do `aluno.dev` no TADS, empresa fictícia, orientador `professor.dev`, documentos TCE e Relatório final em estado que emite `_links.upload`. Secretaria **não** cadastra estágio nesta fatia (CRUD F5 fica para depois).

- Aluno (`internship.view_own`): `GET /estagios?aluno=me` e UI `/estagios` (F1.13). Sem `_links.novo` — não há “Novo estágio”. Detalhe `/estagios/:id` (F1.14) traz documentos e pareceres numa resposta. `POST /estagios/{id}/documentos` (multipart PDF, até 5 MB, `bytea`) → `AGUARDANDO_PARECER`. Outbox `estagio.documento_enviado` na mesma TX.
- Orientador (`internship.review`): fila `GET /estagios?canReview=true` e UI `/estagios?to=me` (F3.6), só estágios em que `id_orientador` é o usuário. `POST .../documentos/{docId}/parecer` `{ acao, parecer }`. Reprovar/indeferir exige parecer ≥ 20 caracteres. `_links.arquivar` só com todos os obrigatórios `APROVADO`; `POST /estagios/{id}/encerrar` → `CONCLUIDO` + Outbox `estagio.encerrado`. Pendência → 422. `CONCLUIDO` imutável.
- HATEOAS `upload` / `revisar` / `aprovar` / `reprovar` / `arquivar` só com capability **e** estado. Menu: rel `estagios` ou `estagios-revisao`. UI `useActions`. Sem `internship.approve_batch`. O pool COE (atribuir, nunca aprovar em lote) entrou no **9.5**.
- Fora do vínculo (outro aluno ou orientador que não é o do estágio) → 404. Sem cap → 403. Anônimo → 401.
- Dispatcher: `estagio.documento_enviado`, `estagio.parecer_emitido` e `estagio.encerrado` fecham `SENT` sem SMTP (no-op, como `certificado.emitido`).
- PDF em `bytea`. MinIO e `modules/arquivos` não nasceram. Expo F1.13/F1.14 entrou no **18** (nesta fatia o menu nativo ainda era whitelist P0).

**9.2 TCC + avaliação individual** — feito

V013. Seed dev: TCC `ATIVO` / `EM_ELABORACAO` do `aluno.dev` no TADS, título fictício, orientador `professor.dev` como único membro da banca (papel `ORIENTADOR`). Secretaria **não** cadastra TCC nesta fatia (CRUD F5 fica para depois).

- Aluno (`tcc.view_own`): `GET /tccs?aluno=me` e UI `/tccs` (F1.15). Sem `_links.novo` — não há “Novo TCC”. Detalhe `/tccs/:id` (F1.16) traz banca, datas e avaliações numa resposta. `POST /tccs/{id}/versao-final` (multipart PDF, até 5 MB, `bytea`) só com `_links.upload-final` (`EM_ELABORACAO` ou `CORRECOES_SOLICITADAS`) → `SUBMETIDO`. Outbox `tcc.submitted` na mesma TX.
- Orientador/banca (`tcc.review`): fila `GET /tccs?canReview=true` (padrão `SUBMETIDO`) e UI `/tccs?to=me` (F3.7), só TCCs em que o usuário é membro. `POST /tccs/{id}/avaliacoes` `{ acao, nota, parecer }`. Indeferir e solicitar correções exigem parecer ≥ 20 caracteres. Aprovar exige parecer não vazio e nota de 0 a 10 (uma casa). A capability da tela nos docs é `tcc.supervise`; esta fatia usa `tcc.review`, no mesmo critério de `internship.review`.
- Download `GET /tccs/{id}/arquivo` com `_links.download` serve o `bytea`. Sem URL pré-assinada.
- HATEOAS `upload-final` / `avaliar` / `download` só com capability **e** estado. Menu: rel `tccs` ou `tccs-revisao`. UI `useActions`. Sem lote, sem checkbox.
- Fora do vínculo (outro aluno ou revisor que não é da banca) → 404. Sem cap → 403. Anônimo → 401.
- Dispatcher: `tcc.submitted` e `tcc.reviewed` fecham `SENT` sem SMTP (no-op).
- **Certificado de conclusão não é emitido.** RF-F3-006 manda emitir quando aprovado e elegível; a elegibilidade (nota mínima e regras de banca da F6.1, consolidação “a definir”, colação F5.11) está ambígua. Dívida na auditoria.
- Expo F1.15/F1.16 entrou no **18** (nesta fatia o menu nativo ainda era whitelist P0).

**9.3 Egresso read-only (F2)** — feito

Sem migration nova: reusa `aluno.situacao = EGRESSO` e a tabela `certificado`. Diploma e colação **não** nasceram (F5.11).

- Seed `egresso.dev` / `GRR20240006`, senha padrão do dev, situação EGRESSO, só `alumni.view_own`. Sem `request.open`, `attendance.*`, `formative.*`, `internship.*` nem `tcc.view_own`.
- Menu: rel `egresso-inicio` → `/egresso/inicio`. Sem início do aluno, solicitações, formativas, estágios, TCC ou presença.
- `GET /egressos/me` (`alumni.view_own`). Painel read-only: curso, data de conclusão indisponível, horas formativas validadas, certificados. Diploma e colação ficam nulos. `_links.reemitir` por certificado. Sem `novaSolicitacao`.
- `GET /egressos/me/certificados/{id}/reemissao` devolve o PDF já gravado. O `hash_sha256` e a assinatura não mudam. Não insere certificado e não assina de novo. Quem não é o dono recebe 404.
- Quem tem `alumni.view_own` sem caps de aluno ativo toma 403 em `/bff/dashboard/aluno`, `/request-types`, `/formativas`, `/estagios`, `/tccs` e `/events`. Aluno ativo toma 403 em `/egressos/**`. A UI manda `/erro/403`; “Ir ao início” volta para `/egresso/inicio`.
- FirstAccessGate segue valendo com `senhaAlterada = false`.
- Expo F2.1 entrou no **18**. `/certificados` (F1.19) continua de aluno ativo (`certificate.view_own`); no app nativo também na fatia **18**.

**9.4 F6.1 configurar curso (coordenação)** — feito

V014. `professor.dev` ganha `course.config` **sem** `course.manage` e continua coordenador do TADS. Secretaria não ganha a tela.

- `GET`/`PATCH /coordenacao/cursos/{id}/config`. Um contrato só (kebab do repo). Não publica `/courses/{id}/config`.
- Campos: `horasFormativasMinimas` (reusa `curso`), `duracaoCalendario` (15/18), `bancaMembrosExternos` (1|2), `bancaModalidade` (PRESENCIAL/REMOTO/HIBRIDO), `regimento` (máx. 10.000).
- `_links.update` só com `course.config` no curso em que `idCoordenador = usuarioId`. Outro curso ou curso inexistente → **403** (não vaza dados). Sem cap → 403. Anônimo → 401.
- UI só `/coordenacao/cursos/:id/configurar`. Menu rel `configurar-curso`. `useActions`. Dirty/cancelar só no frontend. Horas 0–1000.
- `audit_log` append-only na mutação (`curso.config.atualizada`, um registro por campo). Sem e-mail síncrono.
- Não-retroatividade: quem já tinha horas validadas ≥ limiar antigo ganha linha em `elegibilidade_horas` e o BFF continua mostrando esse limiar. Cálculo novo usa o valor atual.
- Sem F6.2, sem desvincular coordenador, sem PATCH `ativo` da secretaria.

**9.5 Pool COE (só atribuição)** — feito

V015. `professor.dev` é membro COE do TADS. Seed extra: estágio `ATIVO` sem orientador (`Pool COE SEPT`). `coe.dev` só entra se o usuário existir. Parecer em lote **não** nasceu.

- `GET /comissoes/coe` (`internship.review`): KPIs + lista (não atribuídos + “comigo”). `CONCLUIDO` fica fora. `_links.assign-member` no item sem orientador ou atribuído ao próprio usuário.
- `POST /comissoes/coe/atribuicoes` `{ estagioId, assigneeId }`. Self-assign e atribuição a colega membro do mesmo curso. Outbox + `audit_log` individuais na mesma TX (`estagio.orientador_atribuido`). Sem auto-notificar o ator. Dispatcher no-op (SENT sem SMTP), como os demais `estagio.*`.
- Lote só na UI: N POSTs ao mesmo endpoint, mesmo orientador. Zero POST de parecer em massa.
- Menu: rel `comissoes-coe` só com `internship.review`. UI `useActions`. BulkActionBar só “Atribuir selecionados”.
- Sem cap → 403. Estágio de outro curso da comissão ou inexistente → 404 (não enumera). Anônimo → 401.
- Expo não ganhou a tela. Filtro CAAF por comissão continua dívida (`coe_membro` não é tabela genérica).

**10. Cadastro F5 de estágio** — feito

Sem migration: o cadastro cabe nas colunas da V012/V015 (`id_orientador` já é nulo). `internship.manage` entra só no seed de `secretaria.dev`. Aluno continua `internship.view_own`. Professor/COE continua `internship.review`.

- `POST /estagios` e `PUT /estagios/{id}` (`internship.manage`). Corpo `alunoId`, `empresa`, `supervisor`, `inicio`, `fim`. Nasce `ATIVO` via `Estagio.abrir`, com `idOrientador` nulo e documentos `TCE` e `RELATORIO_FINAL` em `DocumentoEstagio.pendente`. O aluno sai de `GET /academico/alunos` e precisa estar num curso de `curso_secretario` do chamador (`CursoEscopoPort`). Fora do escopo ou aluno inexistente → 404 (a mesma resposta). Sem cap → 403. Anônimo → 401.
- `PUT` só em estágio `ATIVO` do escopo. `CONCLUIDO` permanece imutável (409). O aluno não é trocado e o orientador não é preenchido aqui — quem atribui segue o pool COE do 9.5.
- Listagem da secretaria: `GET /estagios?escopo=cursos`, filtrada pelos cursos dela, com `_links.novo`. O `GET /estagios?aluno=me` e `?canReview=true` não mudam de contrato. A lista do aluno continua sem `_links.novo`.
- `audit_log` append-only na mesma TX (`estagio.registrado`, `estagio.atualizado`). Esses tipos fecham `SENT` sem SMTP, no mesmo no-op dos `estagio.*` já existentes. Sem e-mail síncrono.
- UI `/secretaria/estagios` (rota derivada; o mapa F0–F8 não tem tela F5 de estágio). Menu: rel `estagios-secretaria` só com `internship.manage`, montado em `MenuLinks` e consumido por `useActions`. F1.13 continua sem “Novo estágio”.
- O seed `@Profile("dev")` do `aluno.dev` e do pool COE continua. A secretaria deixa de depender dele para alimentar o pool e a revisão F3.6.
- Fora: parecer, reprovar e encerrar (já no 9.1), lote, MinIO / `modules/arquivos`, cadastro de TCC, F7.1, Expo, mudança no pool COE.

**11. F7.1 + F7.8 usuários e picker** — feito

`user.manage_all` / `user.reset_password` no seed `admin.dev`. V016 `usuario.nome`. CRUD `/admin/usuarios` e picker `GET /iam/usuarios` (também com `course.manage` na F5.7). Sem e-mail síncrono (`iam.user_created` → SENT no-op). Perfis/matriz FGAC (32), jobs e módulo `auditoria` ficam fora.

**12. Cadastro F5 de TCC** — feito

Sem migration: o cadastro cabe na V013 (`tcc` + `tcc_membro`). `tcc.manage` entra só no seed de `secretaria.dev`. Aluno continua `tcc.view_own`. Professor/banca continua `tcc.review`.

- `POST /tccs` e `PUT /tccs/{id}` (`tcc.manage`). Corpo `alunoId`, `titulo`, `dataDefesa`, `dataEntrega`, `membros[]` (`idUsuario` + `papel`). Nasce `ATIVO` / `EM_ELABORACAO` via `Tcc.abrir`, com exatamente um `ORIENTADOR`. O aluno sai de `GET /academico/alunos` e precisa estar num curso de `curso_secretario` do chamador (`CursoEscopoPort`). Fora do escopo ou aluno inexistente → 404 (a mesma resposta). Membro da banca inexistente → 422. Segundo `ATIVO` do mesmo aluno → 409 (V013). Sem cap → 403. Anônimo → 401.
- `PUT` só em TCC `ATIVO` do escopo. `CONCLUIDO` permanece imutável (409). O aluno não é trocado.
- Listagem da secretaria: `GET /tccs?escopo=cursos`, filtrada pelos cursos dela, com `_links.novo`. O `GET /tccs?aluno=me` e `?canReview=true` não mudam de contrato. A lista do aluno continua sem `_links.novo`.
- `audit_log` append-only na mesma TX (`tcc.registrado`, `tcc.atualizado`). Esses tipos fecham `SENT` sem SMTP, no mesmo no-op dos `tcc.*` já existentes. Sem e-mail síncrono.
- UI `/secretaria/tccs` (rota derivada; o mapa F0–F8 não tem tela F5 de TCC). Menu: rel `tccs-secretaria` só com `tcc.manage`, montado em `MenuLinks` e consumido por `useActions`. Banca pelo `UsuarioPicker` da fatia 11. F1.15 continua sem “Novo TCC”.
- O seed `@Profile("dev")` do `aluno.dev` continua. A secretaria deixa de depender dele para alimentar a revisão F3.7.
- Fora: avaliação e upload (já no 9.2), certificado de conclusão, lote, MinIO / `modules/arquivos`, F7.2, Expo.

**13. F3.1 BFF do professor** — feito

`GET /bff/dashboard/professor` (`dashboard.view_self_professor`) agrega deliberação, eventos do dia, CAAF (só com `formative.review`), estágios e TCCs; degrada por bloco. `/inicio` deixa de ser 403 para professor puro.

**14. F4.1 pool + lote CAAF** — feito

V017. `comissao_membro` (tipo `CAAF`|`COE`) + `formativa.id_responsavel`. `caaf.dev` e `caaf.colegadev` são membros CAAF do TADS. Seed: duas formativas `AGUARDANDO_CAAF` / `PRESENCA_VALIDADA` sem responsável. `coe_membro` (V015) não foi reutilizado.

- `GET /comissoes/caaf` (`formative.review`): KPIs (pool total, atribuídas a mim, prazo médio, aprovadas no período) + lista (não atribuídas + “comigo”). Escopo por `comissao_membro`. `_links.assign-member` / `batch-approve` via HATEOAS.
- `POST /comissoes/caaf/atribuicoes` `{ formativaId, assigneeId }`. Self-assign e colega membro do mesmo curso. Outbox + `audit_log` (`formativas.assigned`). Sem auto-notificar o ator.
- `POST /comissoes/caaf/lote` `{ ids, decisao: "APROVADA" }` só `PRESENCA_VALIDADA` + `AGUARDANDO_CAAF`. Cada item ganha `formativa.aprovada` + certificado; lote emite `formativas.batch_approved`. Indeferir em lote → 422.
- Menu: rel `comissoes-caaf` só com `formative.review`. UI `useActions`. BulkActionBar: atribuir + “Aprovar selecionados” (desabilitado se a seleção misturar tipos).
- Parecer individual (`/formativas?to=me`, item 3) intacto. Sem cap → 403. Cross-curso → 404. Anônimo → 401.
- Fora: indeferir em lote, comprovante manual (24), MinIO, pool COE, Expo.

**15. MinIO + `modules/arquivos`** — feito

V018. `storage_key` em `certificado`, `estagio_documento` e `tcc`. A coluna de PDF deixa de ser obrigatória. Upload server-side pela porta `ObjectStoragePort`; download por URL pré-assinada (TTL 15 min). No dev, MinIO em `:9000` (console `:9001`), bucket `so2`.

- Certificado, documento de estágio e versão final de TCC passam a gravar a chave. Bytes legados migram no startup. Diploma (fatia 16) já nasce só com `storage_key`, sem `bytea`.
- Sem antivírus, sem versionamento de objeto, sem export assíncrona (29).
- Fora: F5.11 (16), Expo, portal admin.

**16. F5.11 diploma e colação** — feito

V019 (`diploma`, UNIQUE por aluno e por número). `diploma.register` no seed de `secretaria.dev`. O escopo é revalidado no use case (`DiplomaAcesso` + `CursoEscopoPort`), não só no `@PreAuthorize`.

- `GET /diplomas/elegiveis?cursoId&periodoId` lista a elegibilidade (TCC aprovado + horas formativas). `_links.confirm` só com `diploma.register` e ao menos um elegível.
- `POST /diplomas` confirma a colação em lote na mesma transação: nasce `PENDENTE`, o aluno vai para `EGRESSO` e as authorities são substituídas por `alumni.view_own`. Outbox `egressos.graduated` + `audit_log`. A checagem de escopo acontece antes de qualquer escrita. Aluno que não pertence ao curso pedido continua `422`. Segunda colação do mesmo aluno → `409`.
- `GET /diplomas?cursoId`, `GET /diplomas/{id}`, `PATCH /{id}/confirm-delivery` (`PENDENTE` → `ENTREGUE`) e `POST /{id}/pdf` (MinIO). `_links` `confirm-delivery` e `upload-pdf` só com capability e estado.
- `cursoId` explícito fora do escopo → `403` (“Curso fora do escopo da sua secretaria.”). Ator sem curso vinculado → `403` (“Nenhum curso vinculado à sua secretaria foi encontrado.”). Diploma por id fora do escopo ou inexistente → `404` (“Diploma não encontrado.”, a mesma resposta). Sem cap → `403`. Anônimo → `401`.
- UI `/secretaria/diplomas`. Menu: rel `diplomas` só com `diploma.register`, montado em `MenuLinks` e consumido por `useActions`. `403` de escopo é estado de erro na tela. O gating do botão continua por `_links.confirm`.
- `GET /egressos/me` passa a preencher diploma e colação quando existem. O contrato e `alumni.view_own` não mudam.
- Fora: lista/CSV de egressos (`/secretaria/egressos`, `alumni.list`, fatia 26). Não há endpoint para desfazer a colação.

**17. F6.2 relatórios da coordenação** — feito

Sem migration. `report.view_coordinator` entra no seed de `professor.dev`, que já é coordenador do TADS e não ganha `course.manage`.

- `GET /reports/coordinator` (`report.view_coordinator`). Filtros opcionais `periodo` e `curso` (sigla). Curso que não é do coordenador, sigla desconhecida ou nenhum curso coordenado → `403`. Sem cap → `403`. Anônimo → `401`.
- UI `/coordenacao/relatorios`. Menu: rel `relatorios` só com `report.view_coordinator`, via `useActions`.
- Fora: comparativo entre cursos, exportação, estatísticas da secretaria (19).

**18. Mobile P2 (Expo)** — feito

Sem migration. O app `frontend-react-native/` deixa a whitelist P0 e passa a ler o menu em `_links` de `GET /auth/me`.

- Aluno: formativas (F1.10/F1.12), estágio (F1.13/F1.14), TCC (F1.15/F1.16) e certificados (F1.19), na mesma API da web.
- Egresso (`egresso.dev`, `alumni.view_own`) cai em `/egresso/inicio` (F2.1).
- Access token em memória. Refresh no Keychain/Keystore (`expo-secure-store`), caminho (A) com `X-SO2-Client: native`. Sem `AsyncStorage` para token.
- Fora: deliberação, CAAF/COE, CRUD da secretaria, BFF professor, FCM, Expo web.

**19. F5.18 estatísticas da secretaria** — feito

Sem migration. `report.view_secretary` entra no seed de `secretaria.dev`.

- `GET /reports/secretary` (`report.view_secretary`). Escopo = cursos de `curso_secretario`. Filtros opcionais `periodo` e `curso` (sigla). Sem curso vinculado ou sigla fora do escopo → `403`. Sem cap → `403`. Anônimo → `401`.
- UI `/secretaria/estatisticas`. Menu: rel `estatisticas` só com `report.view_secretary`, via `useActions`. `403` é estado de erro.
- Fora: exportação, materialização ou cache de métrica. O dashboard da secretaria entrou na fatia 21.

**20. F5.2 + F5.5 fila central e atrasados** — feito

V020 (`solicitacao.deliberador_id`). A secretaria já tinha `request.view_curso`, `request.triage` e `request.deliberate`. O professor só com `request.deliberate` não herda o filtro de curso.

- `GET /requests` com `request.view_curso` devolve a fila do curso (alunos de `curso_secretario`). `?slaBreached=true` (aceita também `?atraso=true`) restringe ao prazo estourado. Cada item traz `slaStatus`. `?format=csv` devolve `text/csv` dos atrasados. A coleção expõe `_links.bulk`.
- `PATCH /requests/bulk` (`request.triage` ou `request.deliberate`, com `request.view_curso`) atribui deliberador. A trilha desta fatia é `audit_log` na mesma transação.
- UI `/solicitacoes` (fila central) e `/secretaria/atrasados`. Menu: rel `fila-solicitacoes` e `atrasados` só com `request.view_curso`. Atribuição em massa só com `_links` (`bulk` / `bulk_assign`) e `useActions`.
- Aluno de outro curso não entra na fila. Sem cap → `403`. Anônimo → `401`. O item “Solicitações” do aluno continua exigindo `request.view_own` — a secretaria não o recebe.
- Fora: solicitação interna `onBehalfOf` (28), editor de RequestType (33), FORWARD, outbox por item do lote (23).

**21. F5.1 dashboard da secretaria** — feito

Sem migration. `dashboard.view_secretary` entra no seed de `secretaria.dev`. O painel é `GET /bff/dashboard/secretary`, agregado como o do professor: um fetch, degrada por bloco. Sem Redis no servidor. `staleTime` de 60 s no TanStack Query; o botão Atualizar invalida esse cache e mostra skeleton.

- Escopo revalidado no use case com `CursoEscopoPort.cursoIdsDoUsuario`. Sem curso vinculado → 403 (“Nenhum curso vinculado à sua secretaria foi encontrado.”), antes de consultar. Sem a capability → 403. Anônimo → 401.
- KPIs: abertas (`EM_ANALISE`/`EM_AJUSTE`), atrasadas (prazo vencido ainda abertas), concluídas hoje (evento para `CONCLUIDA` no dia) e eventos do dia. Fila priorizada ≤ 10, ordem `prazoEm` ASC, `createdAt` ASC. `slaStatus`: `danger` se o prazo já passou, `warning` se vence em menos de 24 h. Banner com a contagem em breach. Alerta se não houver período letivo vigente (RN-F5-004-13).
- A agenda do dia lista eventos do motor de presença que cruzam o dia. O evento não tem curso; o recorte por curso vale para as solicitações.
- `_links` do payload alimentam os atalhos (`useActions`). Sem `_link`, o tile não aparece. Importações não entram (fatia 29).
- `GET /auth/me` ganha o rel `painel` (`/bff/dashboard/aluno` | `/professor` | `/secretary`). `/inicio` escolhe a variante por esse href. Egresso puro não recebe `painel` e segue em `/egresso/inicio`. Aluno e professor puro continuam nos painéis que já tinham.
- Fora: kanban, Redis, outbox do lote (23), importações (29), Expo.

**22. F1.3–F1.5 perfil, segurança e notificações** — feito

V021 (`nome_social`, `telefone`, `identidade_genero`, `foto_storage_key`, `refresh_token.user_agent`, `notificacao_preferencia`). `user.update_own_profile` entra no seed de todas as contas de desenvolvimento. O menu (`perfil`, `perfil-seguranca`, `perfil-notificacoes`) sai de `MenuLinks` e a UI usa `useActions`.

- `GET`/`PATCH /me`. O PATCH é merge parcial: só nome social, telefone, e-mail pessoal e identidade de gênero. GRR e e-mail institucional são ignorados mesmo se vierem no corpo. `POST /me/foto` grava no storage da fatia 15 (`storage_key`) e a leitura sai por URL pré-assinada. Sem PUT pré-assinado no browser. JPEG, PNG ou WebP, no máximo 2 MB.
- `PATCH /me/password` exige a senha atual (Argon2id). Senha errada → 401 `senha-atual-incorreta`, o hash não muda e a sessão continua. Senha nova forte (a mesma regra do reset) e diferente da atual. Sucesso revoga os outros refresh tokens e mantém o da sessão atual. Access token já emitido expira em 15 min.
- `GET /me/sessions` lista refresh tokens ativos. A sessão atual não recebe `_links.encerrar`. `DELETE /me/sessions/{id}` não encerra a sessão atual nem a de outro usuário.
- `GET`/`PATCH /me/notifications` persiste a matriz prioridade × canal (e-mail e in-app), o horário de DND e o digest (`IMEDIATO` ou `RESUMO`). CRITICAL não pode ser desligado (422). Não dispara push, não roda job de digest e não cria `/communications`.
- UI `/perfil`, `/perfil/seguranca`, `/perfil/notificacoes`. Cancelar descarta o formulário sem PATCH. Encerrar sessão só com `_links.encerrar`.
- Fora: hub F1.6 (23), envio de digest, templates (34), push/FCM, SSO, telas Expo.

**23. F1.6 + F3.8 hub de comunicação** — feito

V022 (`turma`, `turma_aluno`, `comunicacao`, `comunicacao_entrega`). `communication.read` no aluno de desenvolvimento. `communication.read` e `communication.publish_class` no `professor.dev`. O menu (`comunicacao`, `publicar-comunicado`) sai de `MenuLinks` e a UI usa `useActions`. Sem `system.broadcast`.

- `GET /communications` (`communication.read`). Abas Todos | Institucional | Turma | Inbox e filtros `lido` e `tipo` no backend, com badges de não lidas. `_links.marcar-lido` só enquanto `read_at` é nulo e o comunicado não expirou. `POST /communications/{id}/read` atualiza só a entrega do destinatário (idempotente). Outro aluno → 404. Inbox mostra CTA só com `_links.acao`.
- `GET /communications/audiencias` e `POST /communications` (`communication.publish_class`). Corpo `{ titulo, corpo, audiencia: { tipo, id }, prioridade, expiraEm }`. Audiência = turmas do professor e cursos em que ele é coordenador. Curso ou turma de outro, ou tipo que não é `TURMA`/`CURSO`, → 422. A publicação grava `comunicacao` e enfileira `comunicacao.published` na mesma transação. O dispatcher já existente cria a entrega e manda e-mail.
- Canal desta fatia: e-mail e in-app, lendo a preferência da fatia 22. DND e digest `RESUMO` seguram o e-mail (não há job de digest). O hub continua visível. CRITICAL ignora DND, digest e desligamento de canal. Sem push.
- UI `/comunicacao` e `/comunicacao/publicar`. Publicar só habilita com título e corpo. Preview Markdown. A opção “todos os alunos da universidade” não existe. Seed dev: turma `ADS 2026/1` com `aluno.dev` e `novo.dev`, mais um item de inbox com CTA para `/perfil`.
- Fora: templates Markdown versionados (34), push/FCM, job de digest, telas do hub no Expo, perfil/senha/sessões (já na 22).

**24. F1.11 formativa manual com comprovante** — feito

V023 (`formativa.storage_key`, `formativa.id_tipo_atividade`, `tipo_atividade_formativa`). `formative.submit` no `aluno.dev`. O botão “Nova atividade” é `_links.nova` na lista, consumido por `useActions`. O path é `/formativas`, não `/formative-entries`.

- `GET /formativas/tipos` devolve só os tipos ativos do curso do aluno. `POST /formativas` multipart (`tipoId`, `cargaHoraria`, `arquivo`). PDF, JPEG ou PNG, até 5 MB, gravado no storage da fatia 15. Nasce `AGUARDANDO_CAAF`, origem `COMPROVANTE`, sem evento. Outbox `formativas.submitted` na mesma transação; o dispatcher fecha SENT sem SMTP.
- Horas não positivas, arquivo inválido ou tipo de outro curso → 422. Sem cap → 403. Anônimo → 401. Outro aluno no id → 404.
- A fila individual (`/formativas?to=me`) vê o comprovante (`_links.comprovante`) e aprova ou indefere como já fazia. O lote continua só `PRESENCA_VALIDADA`: este id toma 422.
- UI `/formativas/nova`. Enviar só com atividade, horas e comprovante. Seed TADS: Curso de extensão, Publicação, Monitoria.
- Fora: OCR, validação automática da carga horária, resubmissão, Expo desta tela, atendimentos (25).

### Fora do escopo de banca / P3

Não conta como “falta para o TCC fechar”. Entra depois, ou nunca.

| Item | Por que fica fora |
|---|---|
| F7.9 saúde + Grafana | A própria spec marca RF-F7-007 como **P3**. O Actuator já está no ar (`health`, `info`, `metrics`; só `health` é `permitAll`) — falta apenas a tela |
| F5.19 kanban de tarefas | A tela é “P3 opcional”, atrás de feature flag `tasks.enabled`, e RF-F5-012 manda não bloquear o MVP |
| Expo web | RNF-POR-01. Exigiria origem CORS extra e `X-SO2-Client` em `allowedHeaders`; o item 8 cobre Expo Go, emulador e aparelho |
| FCM / push | O hub da fatia 23 entrega in-app e e-mail. Push é canal adicional, não requisito de circuito |
| Redis / Bucket4j distribuído | RNF-SEC-04. Rate limit segue em janela de memória no processo |
| ArchUnit + Testcontainers | As ITs rodam em H2 com `ddl-auto: create-drop` e `flyway.enabled: false`; V015 nunca passa pelo Flyway no perfil `test` |
| Cookie nativo (B) | O item 8 ficou em **(A)** (body + SecureStore). Só reabre se alguém provar jar de cookie nativo ponta a ponta |
| HostPin persistente, claim `cursoIds`, CA-04 / `REVOGADO`, paginação da UI acadêmica, janelas pré-agendadas | Dívida de fundação: nenhuma bloqueia o circuito demonstrável |

Linha a linha, com evidência de arquivo e o “por que não agora” de cada item, continua em [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md). Esta seção é a **ordem**; aquele arquivo é o **detalhe**.

---

## Como subir

Neste ambiente o Postgres do SO2 é o container **`so2_postgres_iam` na porta 5433**, banco `secretaria_dev` / usuário `secretaria`.

O `docker compose` deste repositório sobe **outro** Postgres em **:5432**, com outro histórico Flyway. **Não aponte a API para 5432.** O mesmo compose sobe o **Mailpit** (SMTP `:1025`, UI `:8025`) e o **MinIO** (API `:9000`, console `:9001`, bucket `so2`) — estes **são** do SO2.

Três abas no terminal do Cursor, na raiz do repositório. O `application.yml` já aponta para o banco da **5433**, Mailpit e MinIO. Não precisa exportar variável para o uso local.

```powershell
# Aba 1 — banco, Mailpit e MinIO (volta ao prompt)
.\dev.ps1 up

# Aba 2 — API :8080 (ocupa o terminal; parar com Ctrl+C)
.\dev.ps1 api

# Aba 3 — site :5174 (ocupa o terminal; parar com Ctrl+C)
.\dev.ps1 web

# Parar banco, Mailpit e MinIO
.\dev.ps1 down
```

`npm install` roda sozinho na primeira vez do `web`. Mobile, se for usar: `cd frontend-react-native; npm install; npx expo start`. Aparelho físico: `EXPO_PUBLIC_API_URL=http://<IP-LAN>:8080`.

O Postgres do `docker compose` na **5432** continua sendo outro banco. A API não usa essa porta.

Proxies Vite → `http://localhost:8080`: `/auth`, `/me`, `/academico`, `/publico`, `/requests`, `/request-types`, `/bff`, `/events`, `/formativas` (HTML → `index.html`), `/estagios` (HTML → `index.html`), `/tccs` (HTML → `index.html`), `/comissoes` (HTML → `index.html`), `/certificates`, `/communications`, `/.well-known`, `/v3`, `/swagger-ui`, `/actuator`. CORS: `http://localhost:5173` e `http://localhost:5174`. Nativo não passa por CORS. Expo web **não** entrou nesta fatia (sem origem extra e sem `*`).

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
| `aluno.dev@ufpr.br` | `GRR20240001` | Aluno com senha já alterada + cadastro TADS (120 h) + `certificate.view_own` + `internship.view_own` + `tcc.view_own` + `communication.read` + `formative.submit`. Estágio e TCC seed no TADS (não cria outro). Hub em `/comunicacao`. Nova atividade em `/formativas/nova` |
| `novo.dev@ufpr.br` | `GRR20240002` | Primeiro acesso (`senhaAlterada=false`); também tem cadastro acadêmico. Tem `internship.view_own` e `tcc.view_own` e listas vazias |
| `professor.dev@ufpr.br` | `GRR20240003` | Hospedeiro (`event.manage`, `event.host`), deliberante (`request.deliberate`), orientador do estágio (`internship.review`) e da banca do TCC (`tcc.review`). **Membro COE do TADS** e **coordenador do TADS** com `course.config` e **sem** `course.manage` — configura F6.1 e não vê o CRUD `/secretaria/cursos`. `communication.publish_class` publica em `/comunicacao/publicar` (turma ADS 2026/1 e curso TADS). Sem `formative.*` — `GET /formativas?canReview=true` é 403. Sem `internship.view_own` / `tcc.view_own` — `GET /estagios?aluno=me` e `GET /tccs?aluno=me` são 403 |
| `caaf.dev@ufpr.br` | `GRR20240004` | Revisor CAAF (`formative.review`). Sem `event.manage` / `request.deliberate` |
| `secretaria.dev@ufpr.br` | `GRR20240005` | CRUD TADS (`course.manage`, `subject.manage`, `user.manage_students`, `calendar.manage`) + `internship.manage` + `tcc.manage` + `diploma.register` + `report.view_secretary` + `dashboard.view_secretary` + `request.view_curso` + `request.triage` + `request.deliberate`. Sem `course.config` / `formative.review` / `event.manage` / `internship.view_own` / `internship.review` / `user.manage_all` / `report.view_coordinator`. Não recebe deep-link. Nav **sem** o item “Solicitações” do aluno (só Deliberar), **sem** Configurar curso / Usuários / Relatórios da coordenação e **com** Cadastro de estágios (`/secretaria/estagios`), Cadastro de TCCs (`/secretaria/tccs`), Diplomas (`/secretaria/diplomas`), Estatísticas (`/secretaria/estatisticas`), Fila central e Atrasados. Sem o item Estágios/TCCs do aluno, a fila de revisão e o pool COE |
| `egresso.dev@ufpr.br` | `GRR20240006` | Portal egresso (`alumni.view_own`) |
| `admin.dev@ufpr.br` | `GRR20240007` | F7.1/F7.8 (`user.manage_all`, `user.reset_password`). Nav **com** Usuários (`/admin/usuarios`). Sem caps de secretaria/aluno |

O seed IAM (`@Profile("dev")`, `iam.seed.enabled`) **substitui** o conjunto de authorities (revoga extras) e registra no log. O seed acadêmico respeita o mesmo flag e **não** apaga secretários extras de TADS (só adiciona `secretaria.dev` se ausente). O PUT de curso **não** é `replaceAll` da lista enviada: re-adiciona o `usuarioId` de quem edita (anti-lockout). Coordenador e secretários saem do picker (`GET /iam/usuarios`); id inexistente → 422.

A oficina seed `"Oficina Proof of Stay (dev)"` pode já estar `COMPLETA` para `aluno.dev`. O PIN `123456` (`EVENT_DEV_PIN`) vale só para essa oficina e some se a API reiniciar (store em memória). Para repetir o circuito, crie um evento novo em `/professor/eventos` — PIN ou token QR aparece **somente** na host-session.

---

## Smoke (um usuário por papel)

1. **Aluno** — `aluno.dev@ufpr.br` / `GRR20240001` → `/inicio`: saudação, período ou alerta, solicitações, eventos. Horas `N / 120` após CAAF (sem aprovação → `0 / 120`). `/certificados` baixa se `_links.download`. `/eventos` → presença (oficina seed = `SECRET_SINGLE`). Nav **com** Comunicação (`communication.read`).
2. **Primeiro acesso** — `novo.dev@ufpr.br` → `/primeiro-acesso`. O resto do sistema responde 403 no gate.
3. **Professor** — `professor.dev@ufpr.br` → `/inicio` 403 honesto (não “Olá, aluno”). Nav **sem** Cursos da secretaria e **com** Configurar curso, Comunicação e Publicar comunicado. `/professor/eventos` hospeda os 4 modos. Encerrar → `CONCLUIDO` sem PDF. `GET /formativas?canReview=true` → 403. Fila `/solicitacoes?to=me` **sem** filtro `view_curso`. Nav tem **Revisão de estágios** (`/estagios?to=me`), **Pool COE** (`/comissoes/coe`) e **Revisão de TCCs** (`/tccs?to=me`) e não tem os itens Estágios/TCCs do aluno.
4. **Anônimo** — `/login`, `/contato`, protocolo 200. `/bff`, `/events` e **`/academico/**`** → 401. Número `PROT-…` sai de `/solicitacoes/nova`.
5. **Aluno sem cap acadêmica** — `GET /academico/cursos` → 403. Nav **sem** Cursos / Eventos prof. / Revisão CAAF. Nav **com** Estágios e TCCs e **sem** as filas de revisão. `GET /events?mine=true` e host-session → 403.
6. **Secretaria** — `secretaria.dev@ufpr.br` → CRUD TADS 200/201; curso de outro secretário → 404. Nav **sem** Revisão CAAF / Eventos prof. / item Solicitações do aluno (só Deliberar) / Configurar curso e **com** Cadastro de estágios. `GET /coordenacao/cursos/{id}/config` → 403. Fila `view_curso` não lista aluno de outro curso.
7. **Formativas** — presença **COMPLETA** → `PENDENTE_CONFIRMACAO` → aluno confirma → `AGUARDANDO_CAAF`. Em DUAL não nasce na entrada. F5 em `/formativas` recarrega a SPA, não a API.
8. **Deliberação** — aluno abre `DECLARACAO_SIMPLES`. Mailpit (`:8025`) e-mail ao professor com `?token=`. Sem sessão: banner + login. Deferir consome o JTI. Secretaria não recebe deep-link. Sem `request.deliberate` → 403 na transição.
9. **Recuperar senha** — `/recuperar-senha` com e-mail válido ou inexistente: mesmo 202. Só o cadastrado chega no Mailpit (`/nova-senha?token=`).
10. **CAAF** — `caaf.dev@ufpr.br` nav **sem** Eventos prof. / Cursos / Estágios. Aprova em `/formativas?to=me` → certificado na mesma TX. Aluno: horas `N / 120` e KPI ≥ 1. F0.7 verifica o hash. Sem `formative.review` / `certificate.view_own` → 403.
11. **Estágio** — `aluno.dev` em `/estagios` vê o seed (sem “Novo estágio”), abre o detalhe e envia o PDF se `_links.upload`. `professor.dev` em `/estagios?to=me` emite parecer no documento e arquiva quando todos os obrigatórios estão `APROVADO`. Sem lote de parecer. Anônimo → 401. Outro aluno no id → 404. `secretaria.dev` registra em `/secretaria/estagios` (menu Cadastro de estágios, `internship.manage`) um estágio com orientador vazio; `aluno.dev` e `professor.dev` não veem esse item e tomam 403 no `POST /estagios`.
12. **TCC** — `aluno.dev` em `/tccs` vê o seed (sem “Novo TCC”), abre o detalhe e envia o PDF se `_links.upload-final`. `professor.dev` em `/tccs?to=me` só vê o TCC depois do envio e registra nota + parecer um a um. Sem lote e sem certificado. Anônimo → 401. Outro aluno no id → 404. `secretaria.dev` registra em `/secretaria/tccs` (menu Cadastro de TCCs, `tcc.manage`) com banca pelo picker; `aluno.dev` e `professor.dev` não veem esse item e tomam 403 no `POST /tccs`.
13. **F6.1** — `professor.dev` abre Configurar curso (TADS), altera horas 120→150 e salva. UUID de outro curso → 403. `secretaria.dev` não vê a tela. Quem já era elegível pelo limiar 120 permanece.
14. **Pool COE** — `professor.dev` em `/comissoes/coe` vê o seed sem orientador, **Atribuir a mim**, e o item passa a `/estagios?to=me`. Sem botão “Aprovar selecionados”. Sem `internship.review` → 403. Secretaria/aluno não veem o item.
15. **MinIO** — PDF de certificado, estágio ou TCC grava `storage_key` e o download sai por URL pré-assinada (15 min). Console `:9001`, bucket `so2`. Certificado oficial continua só o gerado pelo sistema.
16. **Diploma** — `secretaria.dev` em `/secretaria/diplomas` (menu Diplomas, `diploma.register`) vê elegíveis do TADS e confirma a colação. Curso de outro secretário: `403` em `/diplomas/elegiveis`, `GET /diplomas` e `POST /diplomas`; diploma por id: `404`. Sem curso vinculado: `403`. Segunda colação do mesmo aluno: `409`. O egresso vê o diploma em `/egressos/me`; `alumni.view_own` não muda.
17. **F6.2** — `professor.dev` abre `/coordenacao/relatorios` (`report.view_coordinator`). Sigla de outro curso → 403. `secretaria.dev` não vê o item.
18. **Expo P2** — `aluno.dev` no app vê formativas, estágio, TCC e certificados pelo menu `_links`. `egresso.dev` cai em `/egresso/inicio`. Sem deliberação, CAAF, F5, FCM ou Expo web.
19. **F5.18** — `secretaria.dev` abre `/secretaria/estatisticas` (`report.view_secretary`). Curso fora do escopo → 403. `professor.dev` não vê o item.
20. **Fila central** — `secretaria.dev` em `/solicitacoes` vê a fila dos cursos dela (`request.view_curso`), com `slaStatus`. `/secretaria/atrasados` usa `?slaBreached=true` e exporta CSV (`?format=csv`). `PATCH /requests/bulk` atribui deliberador quando o `_links` existe. Aluno de outro curso não aparece. Quem não tem `request.view_curso` não ganha o filtro.
21. **Dashboard da secretaria** — `secretaria.dev` em `/inicio` (`dashboard.view_secretary`, rel `painel`) vê KPIs, fila prioritária, banner de SLA e agenda do dia só dos cursos dela. Curso de outro secretário não entra na conta. Sem curso vinculado → 403. Aluno e professor não recebem esse payload (o `painel` deles aponta o BFF próprio; `GET /bff/dashboard/secretary` → 403). Atalhos só com `_links`; Importações não aparece. Atualizar recarrega com skeleton.
22. **Perfil** — `aluno.dev` abre Perfil, Segurança e Notificações (`user.update_own_profile`). Em `/perfil`, telefone sujo habilita Salvar; Cancelar não chama a API; GRR e e-mail institucional não mudam. Em `/perfil/seguranca`, senha atual errada fica em 401 e a sessão continua; senha nova encerra os outros aparelhos. Encerrar só aparece com `_links.encerrar`. Em `/perfil/notificacoes`, CRITICAL fica bloqueado; o restante grava canal, DND e digest. Sem push e sem job de digest.
23. **Comunicação** — `aluno.dev` em `/comunicacao` vê a turma e o inbox (CTA só com `_links.acao`). Filtro vazio mostra “Nenhuma comunicação encontrada.” Marcar lido só dispara se `_links.marcar-lido` existir. `professor.dev` em `/comunicacao/publicar` escolhe turma ou curso (sem “todos os alunos da universidade”), publica com título e corpo e o Mailpit recebe o e-mail. Quem não tem a capability toma 403. Audiência de outro curso toma 422. Outro aluno não marca lido o aviso alheio.
24. **Formativa manual** — `aluno.dev` em `/formativas` vê “Nova atividade” (`formative.submit`, `_links.nova`). Em `/formativas/nova` escolhe um tipo do TADS, informa as horas e anexa PDF, JPEG ou PNG. A entrada nasce `AGUARDANDO_CAAF` / `COMPROVANTE` e aparece na fila de `caaf.dev`. “Aprovar selecionados” no pool não cobre esse item. Sem o link, o botão não existe. Tipo de outro curso ou arquivo inválido → 422.

F0.7 não aceita upload de PDF (CA-04). Encerrar evento continua sem PDF. Egresso não acessa `/formativas` nem `/certificados`.

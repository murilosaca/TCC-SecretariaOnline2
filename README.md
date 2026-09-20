# Secretaria Online 2 (SO2)

Plataforma digital da secretaria acadêmica do **SEPT/UFPR**. Este é o repositório oficial do TCC.

O SO2 não substitui o juízo de docentes, comissões ou secretaria. Ele garante trilha de auditoria, integridade de dados e automação de trâmites repetitivos.

**Estado atual:** P0 fechado e demonstrável em `main`. Itens 1 (formativas via presença), **2 (deliberação do motor)**, **3 (CAAF individual)** e **4 (presença v4.1: QR / SECRET_DUAL)** também já estão entregues.

Circuito demonstrável hoje: login → primeiro acesso/LGPD → dashboard do aluno → nova solicitação → presença **QR\|SECRET × SINGLE\|DUAL** (aluno + hospedeiro professor) → formativa `PENDENTE_CONFIRMACAO` quando a presença fica **COMPLETA** → aluno confirma → `AGUARDANDO_CAAF` → **CAAF aprova/indefere com parecer** → horas no `/inicio` saem de `0 / 120` → professor delibera requerimento → consulta pública de protocolo.

**Próxima fatia: certificados oficiais + F0.7** (item 5). Não abrir lote CAAF, COE, dispatcher SMTP nem Expo neste sprint.

---

## O que é este projeto

Atores previstos no TCC: Público (F0), Aluno (F1), Egresso (F2), Professor (F3), Comissões CAAF/COE (F4), Secretaria (F5), Coordenação (F6), Admin (F7) e transversal (F8).

O P0 do Figma cobre só o núcleo que prova o produto. Formativas **não** entram nesse P0; já estão entregues como item 1 do plano:

| Rota | Quem | Situação |
|---|---|---|
| `/login` (+ recuperar senha, nova senha, primeiro acesso) | Público / todos | Entregue |
| `/inicio` | Aluno (BFF); outros perfis veem 403 honesto | Entregue |
| `/solicitacoes/nova` | Aluno | Entregue (motor genérico, não tela por tipo) |
| `/eventos` + `/eventos/:id/presenca` | Aluno | Entregue (QR\|SECRET × SINGLE\|DUAL) |
| `/professor/eventos` + operação | Professor | Entregue (os quatro modos; PIN/token só na host-session) |
| `/publico/verificar-protocolo` | Anônimo | Entregue (metadados; sem PDF) |
| `/formativas` + `/formativas/:id` | Aluno | Entregue (item 1; confirmação simplificada; parecer visível após CAAF) |
| `/solicitacoes?to=me` + `/solicitacoes/:id/deliberar` | Professor (secretaria reutiliza a mesma tela) | Entregue (item 2; DEFER/INDEFER/REQUEST_ADJUST; sem lote nem deep-link) |
| `/formativas?to=me` + `/formativas/:id/revisar` | CAAF (`formative.review`) | Entregue (item 3; um item por vez; sem lote) |

O resto do mapa F0–F8 (lote CAAF/F4.1, estágio, TCC, certificados, FGAC de menu, dashboard professor/secretaria) **ainda não foi aberto**. Está especificado; não está implementado. Formativas a partir de presença validada (RF-F1-006) **já estão entregues**. A revisão individual CAAF (RF-F3-004, F3.5) **já está entregue** — após `APROVADA`, o BFF soma `cargaHoraria` e o `/inicio` deixa de mostrar `0 / 120`. A deliberação do motor (RF-F3-003) **já está entregue** para o seed `DECLARACAO_SIMPLES`. Certificado na aprovação **não** entra (item 5).

A spec do TCC é **Kotlin + Spring Boot** no back e **React 18 + Vite** na web (mobile: React Native + Expo). **Neste repositório o backend já é Kotlin + JVM 21 e o portal é React 18.** Há um esqueleto em `frontend-react-native/` (Expo Router); ainda não está no circuito P0. Os dois clientes reutilizam a mesma API — não as telas. Preserve domínio, RNFs e contratos. Não reintroduza Lombok, Angular, Java-fonte nem o CRUD didático do legado (`idade` em Aluno, `ddl-auto=update`).

---

## Documentação (fonte de verdade)

| Onde | Para quê |
|---|---|
| [`docs/tcc-docs.md`](docs/tcc-docs.md) | Requisitos (RFs/RNFs), atores, regras de negócio, segurança, qualidade. |
| [`docs/telas-figma.md`](docs/telas-figma.md) | Mapa de rotas F0–F8. Detalhe de cada tela em [`docs/telas/`](docs/telas/). |
| [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md) | O que a fundação e o P0 entregaram, e a lista de dívidas conscientes. |
| [`.cursorrules`](.cursorrules) e [`.cursor/rules/`](.cursor/rules/) | Convenções do time e da IA (nomenclatura, Clean Architecture, proibições). |
| [`.env.example`](.env.example) | Variáveis de ambiente. Não commite `.env` nem chaves JWT. |

Swagger (fora de produção): `http://localhost:8080/swagger-ui`. OpenAPI: `/v3/api-docs`.

---

## Onde estão as coisas

```
TCC-SecretariaOnline2/
├── backend/                 API Kotlin + Spring Boot 3 + JVM 21 (Maven)
│   └── src/main/kotlin/br/ufpr/sept/so2/
│       ├── shared/          Transversal: RFC 7807, Security, CORS, UUID v7, VOs
│       └── modules/         Um bounded context por pasta
├── frontend-react/          Portal oficial (React 18 + Vite + TypeScript)
├── frontend-react-native/   Esqueleto Expo (ainda fora do circuito P0)
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
| Acadêmico | `modules/academico/` | CRUD curso, disciplina, aluno (GRR, sem `idade`), período letivo (sem sobreposição). Ainda `permitAll` — sem FGAC. Profile `dev`: `AcademicoDevDataLoader` semeia TADS (120 h) + `aluno.dev` / `novo.dev` |
| IAM | `modules/iam/` | Login, refresh, logout, primeiro acesso + LGPD, recuperação via Outbox. JWT RS256 15 min; cookie `so2_refresh` (`httpOnly; SameSite=Lax; Path=/auth`; `Secure` na spec — local `IAM_COOKIE_SECURE=false` porque o Vite é HTTP). Senha só Argon2id |
| Solicitações | `modules/solicitacoes/` | Motor `RequestType` + `form_schema` + `workflow_json`. Seed `DECLARACAO_SIMPLES`. Protocolo `PROT-AAAA-NNNNN`. `GET /publico/protocolos/{protocolo}`. Fila `GET /requests?canDeliberate=true`. `POST /requests/{id}/transitions` (parecer + Outbox na mesma TX). HATEOAS `deferir` / `indeferir` / `solicitar-ajustes` só se `request.deliberate` **e** o estado atual do `workflow_json` permitirem |
| Presença | `modules/presenca/` | Evento + Proof of Stay **QR\|SECRET × SINGLE\|DUAL**. Aluno confirma PIN ou token QR (`{ pin \| token, deviceUuid, fase }`). Professor hospeda janela de entrada e, em DUAL, de saída. Segredo em claro só na host-session (`HostPinPort` / memória). Persistido só como Argon2id. QR renovável a cada 5 min (`_links.renovar-qr`). Sem geofence. |
| BFF | `modules/bff/` | `GET /bff/dashboard/aluno` — agrega identidade, período, solicitações, eventos e formativas. Degrada por bloco (HTTP 200). 403 se faltar `dashboard.view_own` **ou** capability de aluno (`attendance.view_open` / `request.view_own`) |
| Formativas | `modules/formativas/` | Tabela `formativa` (V007 + parecer/revisor em V008). Gatilho **na mesma TX** de `ConfirmarPresencaUseCase` quando a presença fica **COMPLETA** (SINGLE+`ENTRADA` ou DUAL+`SAIDA`; UNIQUE `id_evento+id_aluno`). Aluno confirma → `AGUARDANDO_CAAF`. CAAF com `formative.review` lista `GET /formativas?canReview=true` e `POST /formativas/{id}/aprovar` / `indeferir` (INDEFER ≥ 20 caracteres). HATEOAS `aprovar` / `indeferir` só se capability **e** `AGUARDANDO_CAAF`. Horas aprovadas = `formativa.cargaHoraria` (não inventa outro número). Sem lote, sem comprovante, sem certificado. Outbox `formativa.criada` / `formativa.confirmada` / `formativa.aprovada` / `formativa.indeferida` no mesmo TX |

Módulos **previstos e ainda sem código**: `estagio`, `tcc`, `comunicacao`, `certificados`, `auditoria` (módulo dedicado), `arquivos`.

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
| V007 | Formativas (`formativa`, UNIQUE `id_evento+id_aluno`) |
| V008 | Parecer CAAF em `formativa` (`parecer`, `id_revisor`, `reviewed_at`) |

Próxima migration, quando um módulo novo precisar de tabela: **V009**.

### Frontend — onde cada tela mora

`frontend-react/src/`

| Pasta | Uso |
|---|---|
| `pages/publico/` | Login, recuperar/nova senha, contato, erros HTTP, verificar protocolo/certificado |
| `pages/aluno/` | Primeiro acesso, minhas solicitações, eventos, presença, formativas do aluno |
| `pages/solicitacoes/` | Fila de deliberação (`?to=me`) e tela `/solicitacoes/:id/deliberar` (F3.3/F3.4; secretaria reutiliza) |
| `pages/formativas/` | Fila CAAF (`?to=me`) e `/formativas/:id/revisar` (F3.5; sem tela duplicada para secretaria) |
| `pages/inicio/` | Dashboard (`/inicio`) — hoje só consome o BFF do aluno |
| `pages/professor/` | Lista / nova / detalhe / operação de evento |
| `pages/secretaria/` | CRUD acadêmico da fundação (atalho de dev; ainda sem FGAC) |
| `layouts/` | `AuthLayout` (F0) e `AppLayout` (autenticado). Nav ainda é atalho de dev (sem FGAC) |
| `api/` | Cliente HTTP (`client.ts` guarda o access token **só em memória**) |
| `auth/` | Sessão, guards (`mustChangePassword` bloqueia o resto do sistema) |
| `hooks/useActions.ts` | UI cega a perfil: botão só se existir `_links` |
| `components/` | `DynamicForm`, `AttendanceWidget`, `HostActionBar`, `ConfirmacaoFormativaWidget`, `PainelDeliberacao`, `PainelRevisaoFormativa` |

Rotas da UI (`/cursos` redireciona para `/secretaria/cursos`) em geral **não** repetem o prefixo da API, para o proxy do Vite não interceptar a navegação. Exceção desta fatia: `/formativas` é rota SPA e API; o proxy devolve `index.html` quando `Accept` inclui `text/html`.

---

## Stack e regras que não dobram

| Camada | Tecnologia |
|---|---|
| Backend | Kotlin + Spring Boot 3 + JVM 21 + Maven + PostgreSQL 16 + Flyway. Testes: Kotest + MockK (domínio/aplicação); ITs JUnit 5 + MockMvc |
| Web | React 18 + Vite + TypeScript + TanStack Query (`frontend-react/`). CSS próprio (`index.css`). Sem Tailwind |
| Mobile | Spec: React Native + Expo, **mesmo backend**. Esqueleto em `frontend-react-native/` (Expo Router + NativeWind). **Não** levar Tailwind para `frontend-react/` |
| Docker | Só Postgres 16 no `docker-compose.yml`. A spec pede também MinIO, Mailpit e observabilidade — não estão |
| Arquivos (futuro) | API S3-compatível (MinIO no desenvolvimento) |
| IDs | UUID v7. Proibido `Long`/`IDENTITY` em entidade de negócio |
| Senha | Só Argon2id. Proibido MD5, SHA-1, SHA-256 e bcrypt para senha |
| Autorização | Capability `dominio.acao` + `_links` HATEOAS. Nunca `hasRole` / `ROLE_*` |
| Erros | RFC 7807 (`application/problem+json`) |
| Lombok | **Proibido** (entidade JPA com plugin `jpa`; DTO como `data class`) |

Outras invariantes: login aceita `@ufpr.br`, e-mail pessoal ou GRR (`GRR` + 8 dígitos). `senhaAlterada = false` bloqueia tudo até senha forte + aceite LGPD. Solicitações não se duplicam por tipo. Certificado oficial só gerado pelo sistema (módulo ainda inexistente). Presença sem geofence, trust score ou aula SIGA. Access token nunca vai para `localStorage`.

---

## APIs entregues

| Prefixo | Auth | Função |
|---|---|---|
| `/auth/*` | Misto (login anônimo; `me` autenticado) | IAM |
| `/publico/**` | Anônimo | Contato (`modules/publico`); protocolo (`modules/solicitacoes`) |
| `/academico/**` | `permitAll` (dívida) | CRUD da fundação |
| `/request-types`, `/requests` | JWT + `request.*` | Motor de solicitações. `GET /requests?canDeliberate=true` (inbox). `POST /requests/{id}/transitions` `{ action, parecer }` |
| `/events` | JWT + `attendance.*` / `event.*` | Eventos e presença v4.1 (QR\|SECRET × SINGLE\|DUAL). `POST /events` com `attendanceMode`. `POST …/windows/entry` e `…/exit`. `POST …/qr/renew`. `POST …/attendance/confirm` `{ pin \| token, deviceUuid, fase }` |
| `/bff/dashboard/aluno` | JWT + `dashboard.view_own` + (`attendance.view_open` **ou** `request.view_own`) | Dashboard agregado. `horasFormativas`: soma `APROVADA`; com cadastro acadêmico e sem aprovação → `0 / requeridas` do curso; **falha do módulo ou JWT sem `aluno` → `null`**, HTTP 200. `pendenciasFormativas`: até 3; falha/sem cadastro → `null`; vazio → `[]` (bloco some) |
| `/formativas` | JWT + `formative.view_own` (lista própria, detalhe do dono, cancelar) / `formative.confirm_own` (confirmar) / `formative.review` (fila `?canReview=true`, detalhe, `POST …/aprovar` e `…/indeferir`) | Sem `POST /formativas` avulso. Sem `/formative-entries`. Sem lote |

JSON em camelCase. Datas ISO-8601 UTC. Listagens `Pageable` (size 20, máx. 100) com `_links`.

---

## Plano depois do P0

Uma fatia vertical por sprint: API + UI + IT de **um** circuito demonstrável. Não abrir dois bounded contexts juntos. Não começar a fatia N+1 no mesmo PR da N.

### Por que esta ordem (e não a do mapa Figma)

O mapa F0–F8 descreve o produto inteiro; **não é cronograma**. A ordem abaixo privilegia três critérios:

1. **Fechar o que já está no código** antes de inventar modo novo. O motor de solicitações já persiste `workflow_json` e o domínio já sabe transicionar; a formativa já chega em `AGUARDANDO_CAAF`. QR não fecha nenhum circuito que o TCC ainda não prove — `SECRET_SINGLE` já demonstra Proof of Stay.
2. **Valor de banca.** Professor decidir um requerimento e horas formativas saírem de `0 / 120` são teses visíveis. Completar os quatro modos de presença não é.
3. **Dependência real.** Certificado oficial exige formativa `APROVADA` (ou evento validado com emissão). Emissão sem CAAF seria número inventado. COE exige módulo de estágio, que ainda não existe — por isso CAAF e COE **não** andam no mesmo sprint.

QR voltou para o item 4 de propósito. COE saiu do item da CAAF e foi para o item 9, junto com estágio.

### Regras que valem em todas as fatias

Kotlin + React (`frontend-react/`). Sem Lombok, sem Angular, sem Java-fonte, sem Tailwind. Capabilities `dominio.acao` + `_links` HATEOAS; nunca `hasRole`. Flyway imutável (próxima tabela: **V009**). Sem e-mail/push síncrono. Sem geofence, trust score ou aula SIGA. `/academico/**` continua `permitAll` até o item 7. Menu com atalhos de dev (Formativas, Eventos prof., CRUD da secretaria) só some no FGAC.

### Ordem

| # | Fatia | Estado |
|---|---|---|
| 1 | Formativas via presença SECRET_SINGLE (RF-F1-006) | **Feito** |
| 2 | Deliberação do motor de solicitações | **Feito** |
| 3 | CAAF individual (parecer; horas saem de 0) | **Feito** |
| 4 | Presença v4.1 restante (QR / `SECRET_DUAL`) | **Feito** |
| 5 | Certificados oficiais + F0.7 | Depois de 3 (formativa `APROVADA` ou evento validado) |
| 6 | Dispatcher do Outbox + comunicação | Depois de haver eventos que valham e-mail |
| 7 | FGAC (F7) | Fecha `/academico/**` e a nav |
| 8 | Cliente Expo (React Native) | Mesma API; web já provou os contratos |
| 9 | Estágio + COE, TCC, egresso, F6.1 | Módulos novos |

#### 1. Formativas via presença — feito

Tabela `formativa` (V007). Gatilho na mesma TX de `ConfirmarPresencaUseCase` quando a presença está **COMPLETA** (hoje o circuito seed continua SECRET_SINGLE + `ENTRADA`). Aluno confirma (`PENDENTE_CONFIRMACAO` → `AGUARDANDO_CAAF`) ou cancela. Sem `POST /formativas`, sem comprovante, sem parecer CAAF, sem certificado.

BFF: `kpis.horasFormativas` soma `APROVADA` (regra inalterada). Com cadastro acadêmico e formativa aprovada o smoke mostra `N / 120` (`N` = `cargaHoraria` da oficina; seed TADS requer 120 h). Sem cadastro em `aluno`, ou se o módulo falhar → `null` (não inventa `0 / 0`). Até 3 `pendenciasFormativas`; array vazio some o bloco.

#### 2. Deliberação do motor — feito

O P0 só **abria** solicitação. `DECLARACAO_SIMPLES` já tinha `workflow_json` (`EM_ANALISE` → `DEFER`/`INDEFER`/`REQUEST_ADJUST`). `Solicitacao.transicionar` existia.

**Entregue:** seed `request.deliberate` no professor; `POST /requests/{id}/transitions` com ação + parecer fundamentado (INDEFER exige ≥ 20 caracteres); fila `GET /requests?canDeliberate=true` (inbox, não tela por tipo); HATEOAS `deliberar` / `deferir` / `indeferir` / `solicitar-ajustes` só se a capability **e** o `workflow_json` permitirem a ação a partir do estado atual; UI `/solicitacoes?to=me` e `/solicitacoes/:id/deliberar` cega a `_links`. Secretaria reutiliza a mesma tela — não duplicar. Outbox `solicitacao.deliberada` / `solicitacao.ajuste_solicitado` na mesma TX (dispatcher continua no item 6).

**Não entrou:** deep-link JWT de 72 h por e-mail (bloqueado até o item 6); deliberação em lote (o seed não configura lote); BFF completo do professor (F3.1); novo `RequestType`.

#### 3. CAAF individual — feito

**Entregue:** transição `AGUARDANDO_CAAF` → `APROVADA` / `INDEFERIDA` com parecer; seed `formative.review` em `caaf.dev@ufpr.br` (GRR novo; `professor.dev` continua sem a capability e toma 403 em `GET /formativas?canReview=true`); fila `GET /formativas?canReview=true` (inbox, não `/formative-entries`); `POST /formativas/{id}/aprovar` e `/indeferir` (`{ parecer }`; INDEFER ≥ 20 caracteres no cliente e no use case, 422); HATEOAS `revisar` / `aprovar` / `indeferir` só se `formative.review` **e** `AGUARDANDO_CAAF`; UI `/formativas?to=me` e `/formativas/:id/revisar` cega a `_links` (mesmo espírito de FilaDeliberacao + PainelDeliberacao). Secretaria/CAAF não ganhou tela duplicada. Ao aprovar, as horas são as já gravadas em `formativa.cargaHoraria` — o BFF só soma `APROVADA`. Outbox `formativa.aprovada` / `formativa.indeferida` + `audit_log` na mesma TX (dispatcher continua no item 6).

**Não entrou (honesto):** lote CAAF / F4.1 / checkboxes (RF: lote só com presença já validada — fatia curta depois, se precisar); filtro de fila por curso da comissão (escopo N:N entra no FGAC, item 7); COE (item 9); comprovante (`viaComprovante()` continua 409); certificado / PDF (item 5 — aprovar **não** emite); BFF do professor (F3.1).

#### 4. QR / `SECRET_DUAL` — feito

Completa presença v4.1 no **mesmo** motor (`QR`/`SECRET` × `SINGLE`/`DUAL` + janela de saída). Sem geofence. Sem V009: `janela_saida_*` já existia em V005.

**Entregue:** `POST /events` aceita `attendanceMode` ∈ {`SECRET_SINGLE`, `SECRET_DUAL`, `QR_SINGLE`, `QR_DUAL`} (default `SECRET_SINGLE`). Professor abre `_links.abrir-janela-entrada` e, em DUAL depois da entrada, `_links.abrir-janela-saida`. Aluno confirma o mesmo `POST /events/{id}/attendance/confirm` com `pin` (SECRET) ou `token` (QR), `deviceUuid` e `fase`. Sem entrada, `_links.confirmar-saida` não existe e o POST de `SAIDA` é 409. Token/PIN em claro só na `host-session`; persistido Argon2id. QR renovável a cada 5 min (`_links.renovar-qr`) sem resetar a janela. Encerrar **não** emite PDF (item 5).

**Formativa (honesto):** o gatilho continua um só, na mesma TX de `ConfirmarPresencaUseCase`, UNIQUE `id_evento+id_aluno`. Não dispara em DUAL na `ENTRADA` (`PARCIAL`). Dispara quando a presença fica **COMPLETA** (SINGLE+`ENTRADA` ou DUAL+`SAIDA`), alinhado a RN-F1.18-02. `presenca.confirmada` no Outbox + `audit_log` em cada fase confirmada. `viaComprovante()` continua 409.

**Não entrou (honesto):** janelas pré-agendadas no formulário de criação (entrada 19h00–19h15 / saída 21h45–22h00 da CA-02) — nesta fatia as janelas são abertas **ao vivo** (15 min), como o SECRET_SINGLE do P0; lista ao vivo de inelegíveis/nomes (CA-06); leitura por câmera no Expo (item 8); certificado no encerrar (item 5). A oficina seed permanece `SECRET_SINGLE`.

#### 5. Certificados

Só o sistema gera. Evento validado **ou** formativa `APROVADA`. PDF canônico + hash + F0.7 deixa de ser stub. Sem upload externo oficial. Sem MinIO no compose ainda: persistência alinhada ao que existir no sprint (não inventar bucket).

#### 6. Dispatcher do Outbox

Hoje `presenca.confirmada`, `evento.janela_aberta`, `evento.qr_renovado`, `evento.encerrado`, `formativa.criada` / `formativa.confirmada` / `formativa.aprovada` / `formativa.indeferida`, `solicitacao.deliberada` / `solicitacao.ajuste_solicitado` e recuperação de senha nascem `PENDING` e ficam. Sem SMTP síncrono nunca. Este item liga o dispatcher (at-least-once) e, com isso, o deep-link de deliberação por e-mail que o item 2 deixou de fora.

#### 7. FGAC (F7)

Fechar `/academico/**` com `@PreAuthorize`; nav só por `_links` de menu (somem Formativas/Eventos/CRUD como atalho de dev); escopo por curso (tabela N:N de secretários). Sem isso, o portal continua honesto nas telas de dados e frouxo no menu.

#### 8. Expo

React Native + Expo em `frontend-react-native/`, **mesmo** `/auth`, `/bff`, `/events`, `/formativas`, `/requests`. Token em Keychain/Keystore. Não reescrever o back. NativeWind no mobile **não** autoriza Tailwind na web. Esta fatia (item 8) liga o cliente aos contratos já provados — o esqueleto sozinho não fecha o P0.

#### 9. Estágio + COE, TCC, egresso, F6.1

Módulos novos, um por vez quando o requisito entrar. Parecer COE **sempre** individual (nunca lote). Egresso é read-only e não acessa rotas de aluno. F6.1 é da coordenação, não da secretaria.

Dívida consciente (não é P0): tabela em [`docs/auditoria-fundacao.md`](docs/auditoria-fundacao.md). Destaques: BFF professor (F3.1) ausente; certificados `null`; F0.7 stub; `/academico/**` aberto; PIN/token da host-session some se a API reiniciar; sem ArchUnit; rate limit em memória (sem Redis). Horas validadas no `/inicio` saem de 0 depois que a CAAF aprova (item 3). Presença v4.1 (item 4) está no motor; janelas pré-agendadas no create e lista ao vivo de inelegíveis não.

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

# 3. Web — http://localhost:5174 (porta fixa no vite.config.ts; 5173 não é o default deste repo)
cd frontend-react
npm install
npm run dev

# 4. Mobile (Opcional) — Expo Router / NativeWind
cd frontend-react-native
npm install
npx expo start
```

Copie `.env.example` para o shell se for usar variáveis. Ele já aponta JDBC `:5433` e `FRONTEND_BASE_URL=http://localhost:5174`. A URL JDBC **default** do `application.yml` ainda é `:5432` e o `frontend-base-url` default ainda é `:5173` — **sobrescreva** (o `.env.example` faz isso).

Proxies Vite (`/auth`, `/academico`, `/publico`, `/requests`, `/request-types`, `/bff`, `/events`, `/formativas`, `/v3`, `/swagger-ui`, `/actuator`) vão para `http://localhost:8080`. CORS libera `http://localhost:5173` e `http://localhost:5174`. O proxy de `/formativas` devolve `index.html` se `Accept` incluir `text/html` (F5 da SPA).

Testes pontuais:

```bash
cd backend && mvn -q test
cd frontend-react && npm test
```

Não é obrigatório rodar a suíte inteira se 5433 + `:8080` + `:5174` já estiverem no ar. Reinicie a API depois de mudar o backend.

---

## Contas de desenvolvimento

Senha de todos: `TroqueEstaSenha1!` (só local; override `IAM_DEV_SEED_PASSWORD`).

**Login aceita só e-mail ou GRR** (`GRR` + 8 dígitos). `professor.dev` ou `aluno.dev` **sem** `@ufpr.br` não são identificadores válidos.

| Usuário | GRR | O que testa |
|---|---|---|
| `aluno.dev@ufpr.br` | `GRR20240001` | Aluno com senha já alterada + cadastro acadêmico TADS (120 h) |
| `novo.dev@ufpr.br` | `GRR20240002` | Primeiro acesso (`senhaAlterada=false`); também tem cadastro acadêmico |
| `professor.dev@ufpr.br` | `GRR20240003` | Hospedeiro (`event.manage`, `event.host`) e deliberante (`request.deliberate`). Sem `formative.*` — `GET /formativas?canReview=true` é 403 |
| `caaf.dev@ufpr.br` | `GRR20240004` | Revisor CAAF (`formative.review`). Fila `/formativas?to=me`. Sem `event.manage` / `request.deliberate` |

A oficina seed `"Oficina Proof of Stay (dev)"` pode já estar `COMPLETA` para `aluno.dev`. O PIN `123456` (`EVENT_DEV_PIN`) vale só para essa oficina e some se a API reiniciar (store em memória). Para repetir o circuito, crie um evento novo em `/professor/eventos` (qualquer um dos 4 modos) — o PIN ou o token QR novo aparece **somente** na host-session.

---

## Smoke do estado atual (um usuário por papel)

1. **Aluno** — `aluno.dev@ufpr.br` (ou `GRR20240001`) → `/inicio`: saudação, período ou alerta de calendário, solicitações, `eventosHoje` / próximos se houver janela. “Nova solicitação” só se `_links.novaSolicitacao`. Horas `N / 120 h` depois que a CAAF aprova (`N` = carga da oficina; seed TADS = 120). Sem aprovação ainda → `0 / 120`. Certificados “Indisponível”. `/eventos` → presença nos quatro modos (seed da oficina continua SECRET_SINGLE).
2. **Primeiro acesso** — `novo.dev@ufpr.br` → `/primeiro-acesso`. O resto do sistema (dashboard, eventos, host, formativas) responde 403 no gate.
3. **Professor** — `professor.dev@ufpr.br` → `/inicio` **não** mostra “Olá, aluno” (403 honesto). `/professor/eventos` → cria qualquer um dos 4 modos → abre janela → PIN ou QR só no painel → aluno confirma → some `confirmar-entrada`. Em DUAL, abre saída (`_links.abrir-janela-saida`) → aluno confirma `SAIDA` → `COMPLETA`. Encerrar → `CONCLUIDO` (sem PDF). `GET /formativas` e `GET /formativas?canReview=true` → 403. `/solicitacoes?to=me` lista o que está em `EM_ANALISE` → Deliberar → parecer → Deferir → some da fila.
4. **Anônimo** — `/login`, `/contato`, `/publico/verificar-protocolo/{PROT-AAAA-NNNNN}` 200. `/bff` e `/events` 401. O número `PROT-…` sai de `/solicitacoes/nova` (não use `/demo`).
5. **Aluno** em `GET /events?mine=true` e `host-session` → 403.
6. **`/academico/**`** continua 200 (ainda sem FGAC).
7. **Formativas** — professor cria um modo SINGLE (ou completa DUAL com saída) e abre a janela. Aluno confirma → formativa `PENDENTE_CONFIRMACAO` em `/formativas` e CTA no `/inicio` → `_links.confirmar` → `AGUARDANDO_CAAF` (botões somem; CTA some no refetch). Em DUAL, a formativa **não** nasce na entrada. F5 em `/formativas` recarrega a SPA, não a API.
8. **Deliberação** — aluno abre `DECLARACAO_SIMPLES` em `/solicitacoes/nova`. Professor em `/solicitacoes?to=me` vê o protocolo → `/solicitacoes/:id/deliberar` → `_links.deferir` → parecer → estado `DELIBERADA`. Aluno em `/solicitacoes/:id` vê a timeline. Sem `request.deliberate`, `POST /requests/{id}/transitions` é 403. Deep-link por e-mail ainda não existe.
9. **CAAF** — `caaf.dev@ufpr.br` em `/formativas?to=me` vê o item em `AGUARDANDO_CAAF` (`_links.aprovar` presente). Aprova com parecer → `APROVADA`; some da fila; botões somem. Aluno em `/inicio`: horas `N / 120` (`N` = `cargaHoraria` da oficina; seed da oficina de prova é 4 h se ainda estiver `COMPLETA`, senão a do evento novo). Certificados continuam “Indisponível”. `GET /formativas?canReview=true` sem `formative.review` → 403. Outro aluno no detalhe → 404.

F0.7 (verificar certificado) permanece stub. Egresso não acessa `/formativas`.

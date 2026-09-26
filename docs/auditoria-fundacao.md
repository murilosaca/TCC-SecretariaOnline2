# Auditoria de fundação — SO2

Data: 2026-09-20. Fontes: `docs/tcc-docs.md`, `docs/telas-figma.md`, código e `.cursorrules`.

## Correções aplicadas nesta auditoria

- `.cursorrules` e `.cursor/rules/` passam a citar `docs/` como fonte de verdade,
  stack oficial Kotlin+React, rotas Figma, P0, Proof of Stay, BFF e a proibição
  de Lombok (decisão do time; a spec Kotlin não usa Lombok).
- `tcc-docs.md` e o mapa de telas ficam em `docs/`. O README aponta para eles.
- Conteúdo trocado de `F5.9` (calendários) e `F5.10` (egressos) foi corrigido.
- Backend: API de `periodo_letivo` (RF-F5-004-c) com validação de semestre,
  intervalo e sobreposição, mais `GET /academico/periodos/vigente`.
- Frontend React: `AuthLayout` / `AppLayout`, pastas por fluxo e rotas Figma
  (`/login`, `/secretaria/*`, `/inicio`). Rotas planas antigas redirecionam.

## IAM (sprint P0)

Módulo `modules.iam` implementado: `usuario` + `usuario_authority` (`dominio.acao`),
refresh opaco, histórico de senha, JTI blacklist, `outbox_event` mínimo e `audit_log`
append-only. Login JWT RS256 15 min; cookie `so2_refresh` (`httpOnly; SameSite=Lax;
Path=/auth`; `Secure` na spec — local `IAM_COOKIE_SECURE=false` porque o Vite é HTTP);
Argon2id; primeiro acesso (RF-F1-002); recuperação via Outbox (sem e-mail
síncrono). Telas React `/login`, `/recuperar-senha`, `/nova-senha`, `/primeiro-acesso`
ligadas. Access token só em memória.

## Plano — `@PreAuthorize` no CRUD acadêmico

**Feito no item 7** (capabilities corretas: `course.manage`, `subject.manage`,
`user.manage_students`, `calendar.manage` — **não** `curso.manage` / `student.manage`).

1. `anyRequest()` é `authenticated()` (exceto F0 público e `/auth/*` anônimos).
2. Controllers acadêmicos anotados com `hasAuthority('dominio.acao')` — nunca `hasRole`.
3. `_links` HATEOAS; UI cega (`useActions`). Menu em `GET /auth/me`._links.
4. Escopo por curso: tabela `curso_secretario` (V011) + `CursoEscopoPort` no use case.
   Claim JWT `cursoIds[]` **não** entrou. Períodos continuam globais.

## P0 demonstrável (fechamento)

O P0 oficial (`docs/telas-figma.md`) mais o hospedeiro F3.2 (único jeito de provar a presença) está fechado ponta a ponta:

| Fatia | Situação |
|---|---|
| `/login` + IAM | JWT RS256 15 min, refresh `so2_refresh` (`httpOnly; SameSite=Lax; Path=/auth`; `Secure` na spec — local `IAM_COOKIE_SECURE=false` porque o Vite é HTTP), Argon2id, anti-enumeração, primeiro acesso senha+LGPD (`6997339`) |
| `/solicitacoes/nova` | Motor genérico `RequestType` + `form_schema` + `workflow_json` (`0e882d2`) |
| `/inicio` | BFF `GET /bff/dashboard/aluno`, degradação por bloco, HTTP 200, `eventosHoje`/`proximosEventos` via porta no módulo `presenca`, `_links.novaSolicitacao` só com `request.open` |
| Presença aluno | SECRET_SINGLE (`V005`), `/eventos`, Proof of Stay sem geofence |
| Hospedeiro F3.2 | SECRET_SINGLE (`V006`), PIN em claro só na host-session (`HostPinPort` / `HostPinStore` em memória) |
| F0.6 | `GET /publico/protocolos/{protocolo}` + UI loading / not-found / ok (hash truncado, sem PDF) |

Sessão JWT que não é aluno: BFF devolve 403; `/inicio` mostra empty honesto, sem “Olá, aluno”. Deep link: login respeita `state.from` se a rota for interna segura. Refresh falho após 401 limpa o access token e vai para `/erro/401` (CTA “Fazer login”), não para `/inicio` anônimo.

A ordem das fatias **depois** do P0 (deliberação → CAAF individual → QR, com COE só junto de estágio) está no README. Este arquivo lista dívida; não redefine o cronograma.

## Ordem das fatias 10+ — fica no README

A varredura de tudo o que a spec ainda cobra e o código não entrega (66 RFs e 72 telas F0–F8 classificados em feito / parcial / ausente / P3) e a ordem numerada a partir de **10** estão na seção **O que falta (pós-9.5)** do [`README.md`](../README.md). A tabela de dívida **abaixo** continua sendo a fonte do detalhe por item — não duplicar a ordem aqui, e não ler esta tabela como cronograma.

## Já fechado (não reabrir)

Quem ler a tabela de dívida **abaixo** não deve achar que `/academico/**` ainda é `permitAll` nem que o menu ainda é atalho de dev.

| Item | Situação (uma linha) |
|---|---|
| FGAC em `/academico/**` | Item 7: JWT + capability da tela (`course.manage` / `subject.manage` / `user.manage_students` / `calendar.manage`); anônimo 401; sem cap 403; fora do escopo 404. **Não** está `permitAll`. POST inclui o criador na mesma TX; PUT re-adiciona quem edita (anti-lockout). `_links.criar` da coleção é incondicional (o GET já passou no `@PreAuthorize`). |
| Nav HATEOAS | Item 7: `GET /auth/me`._links + `useActions`. `MenuLinks` é o único ponto que olha caps para o menu. |
| Dispatcher SMTP | Item 6: Outbox → Mailpit at-least-once. Hub F1.6 / F3.8 / F7.5 / push / FORWARD continuam na dívida. |
| F0.7 verificação pública | Item 5: `GET /publico/certificados/{hash}/verificacao` + JWKS + SubtleCrypto. CA-04 (upload) e `REVOGADO` continuam dívida. |
| KPIs horas / certificados no `/inicio` | Itens 3 e 5: soma `APROVADA` e contagem do módulo; deixam de ser “Indisponível” após a CAAF. Falha/sem cadastro → `null` (HTTP 200). |
| Presença QR \| SECRET × SINGLE \| DUAL | Item 4: motor v4.1 com os quatro modos. Janelas pré-agendadas e lista ao vivo de inelegíveis continuam dívida. |
| V011 `curso_secretario` | Item 7: N:N + seed TADS + `CursoEscopoPort`. Claim JWT `cursoIds` **não** entrou (dívida abaixo). |
| P0 Expo aluno | Item 8: login / primeiro acesso / BFF `/inicio` / nova solicitação / presença SECRET+QR. Refresh **(A)** body + Keychain. Web cookie intacto. |
| Egresso F2 | Item 9.3: `alumni.view_own`, menu `egresso-inicio`, `GET /egressos/me`, reemissão do PDF já gravado (mesmo `hash_sha256` e mesma assinatura, 404 se não for o dono). Sem migration. Diploma, lista F1.19 e Expo ficaram de fora (linhas abertas). |

## Ainda aberto (dívida consciente — não é P0)

Só o que **ainda** está aberto. Não reabrir FGAC, nav, dispatcher, F0.7, KPIs, QR nem V011.

| Item | Spec | Situação | Ação |
|---|---|---|---|
| F3.1 dashboard professor | BFF próprio | Ausente; `/inicio` 403 honesto | BFF professor |
| HostPin em memória | PIN na host-session | Some no restart da API | Persistência ou reabertura de janela |
| Claim JWT `cursoIds` | spec JwtFilter | Escopo só no use case (`CursoEscopoPort`). Sem `user.manage_all` | Incluir no token sem confiar só no claim |
| JWT vs `/auth/me` | capabilities | Enforcement (`@PreAuthorize`, assembler) lê authorities do JWT (TTL 15 min). `GET /auth/me` monta o menu com `ConsultarSessaoUseCase` → `usuario.authorities` do banco. Depois de conceder/revogar (inclusive `substituirAuthorities` no seed) o menu pode mudar até 15 min antes da API. Não é explorável: o JWT nunca concede mais do que foi assinado | Mesmo assunto do claim `cursoIds` ausente; não encurtar o TTL só por isso |
| Fila F5.2 da secretaria | `/solicitacoes` com `request.view_curso` | Menu não emite o rel `solicitacoes` sem `request.view_own`; secretaria usa `deliberar` (`/solicitacoes?to=me`) | F5.2 é fatia futura — não implementar a fila central nesta correção |
| Relatórios F6.2 | KPIs e gráficos da coordenação | F6.1 entregue em `/coordenacao/cursos/{id}/config`; F6.2 não nasceu | Fatia própria |
| Portal admin F7.1–F7.9 | usuários, papéis, jobs, saúde | Fora desta fatia | Não misturar com o FGAC acadêmico (já fechado) |
| Seletor de usuários em F5.7 | Nome, Sigla, Coordenador, Horas, Secretários (`docs/tcc-docs.md`:3180 — `coordenadorId` e `secretariosIds[]` são usuários existentes) | Form web pede UUID cru de secretário e não envia `idCoordenador` (coordenador do TADS vem do seed). A API também **não** valida existência: os UUIDs entram direto em `Curso.idCoordenador` e em `curso_secretario` (V011 declara "sem FK cross-módulo"). Sem escalada de privilégio — só se grava vínculo em curso já no escopo do chamador | Picker da F7.1 resolve na origem; não adicionar validação de existência nesta fatia |
| `request.triage` | nome `dominio.acao` | Não aparece em `docs/`. É decisão de implementação derivada de RF-F5-002 (triagem da secretaria) para o dispatcher **não** mandar deep-link a quem só faz fila | Não alterar o item 6 (dispatcher/deep-link fechados) |
| `SolicitacaoCursoEscopo` | join aluno ↔ IAM | Duas queries por aluno (`findByIdentificador` + `findByEmail`); importa `IdentificadorLogin` (VO do IAM). Acoplamento por PORT (`CursoEscopoPort`, `UsuarioRepository`) é o padrão aceito do repo — **não** é violação de dependência (é o "só ports" da regra 3) | Escala é limite consciente; não duplicar dados entre módulos |
| ArchUnit / travessias de módulo | regra 2 de dependência | Três famílias, não só o loader de dev: (1) `modules.iam.infrastructure.security.IamPrincipal` é importado em **produção** por `CursoController`, `AlunoController`, `DisciplinaController`, `SolicitacaoController`, `EventoController`, `FormativaController`, `CertificadoController` e `AlunoDashboardController` — padrão aceito do repo hoje; candidato a `shared/` quando o ArchUnit entrar. (2) `IamDevDataLoader` importa `comunicacao.application.EmailMascarado`. (3) `DespacharOutboxUseCase` (application) importa `comunicacao.infrastructure.ComunicacaoProperties` (application → infrastructure). O caso `@Profile("dev")` `AcademicoDevDataLoader` → `IamProperties` continua. `Solicitacoes` via `CursoEscopoPort` + `UsuarioRepository` **não** entra nesta lista (linha acima) | Não mover `IamPrincipal` nesta fatia; extrair porta de seed / properties quando o ArchUnit entrar |
| Nomes de endpoint | `docs/tcc-docs.md` / F5.6 / F5.7 / F5.8 | Spec fala `/students`, `/secretaria/cursos`, `/calendars`, `POST /calendars/periods`, `?slaBreached=true`. Código: `/academico/alunos`, `/academico/cursos`, `/academico/periodos`, parâmetro `atraso`. F5.9 já apontava `/academico/periodos`; F5.6, F5.7 e F5.8 foram alinhados à API real | **Não** renomear a API — quebraria o P0 e o frontend |
| Desativar curso | spec `PATCH /secretaria/cursos/{id} {ativo:false}` + RN-F5-004-04 (histórico preservado) | Código: `DELETE` com guarda de vínculos (`CursoApplicationService` → 409 "Curso possui alunos ou disciplinas vinculados", coberto por IT) e `ativo` só chega via PUT. Sem PATCH. A intenção da RN (não apagar histórico) está no 409 | Não implementar o PATCH nesta fatia |
| Unicidade de GRR/e-mail em `aluno` | V002 UNIQUE global | `POST /academico/alunos` com GRR ou e-mail institucional já usado (mesmo de outro curso) devolve 409. GRR é identificador público da UFPR, não segredo — o 409 não é enumeração de dado sensível | Manter UNIQUE global; não scoped por curso |
| Ports com `Pageable` | domain/application puros | Ports importam Spring Data | Extrair paginações próprias numa fatia seguinte |
| Bucket4j + Redis | RNF-SEC-04 | Janela em memória no processo | Trocar quando houver Redis |
| Cobertura 85/70/75 | RNF de testes | Ampliar por módulo | Continuar nas fatias seguintes |
| Combo Alunos/Disciplinas × `course.manage` | seletor de curso nas telas F5.8 / alunos | As quatro `*.manage` andam juntas no seed; o combo reusa `GET /academico/cursos` (exige `course.manage`). Sem ela o form fica desabilitado | Dívida honesta até F7.1 — sem endpoint novo de busca nesta fatia |
| Flyway nas ITs | migrations imutáveis (última **V015** = pool COE; V014 = F6.1) | `application-test.yml` usa H2 `ddl-auto: create-drop` e `flyway.enabled: false`. Toda IT é `@ActiveProfiles("test")`, então a V015 nunca é exercitada pelo Flyway; divergência DDL × entidade passa verde e só explode no boot contra o Postgres `:5433`. Em dev/prod o `ddl-auto` segue `validate` | Testcontainers + Postgres num perfil `it` numa fatia futura. **Não** implementar Testcontainers nesta correção |
| Paginação da UI acadêmica | F5.7 pede `Pagination footer`; API emite `_links.first/last/next/prev` | `Cursos.tsx`, `Disciplinas.tsx`, `Alunos.tsx` e `Calendarios.tsx` fixam a primeira página e não renderizam controle. Acima de 20 registros os dados ficam invisíveis sem aviso | Componente `Paginacao` dirigido por `_links` numa fatia de limpeza. **Não** implementar a paginação nesta correção |
| Fila CAAF por curso | F4.1 / comissão | Nasceu `coe_membro` (só COE). Sem tabela genérica `commission_member` | Filtro CAAF por comissão continua dívida |
| F4.2 pool COE | RF-F4-002 | Item 9.5: `GET /comissoes/coe` + `POST /comissoes/coe/atribuicoes`. Parecer continua individual no 9.1. Sem “Aprovar selecionados” | Fechado nesta fatia |
| Cadastro F5 de TCC | secretaria registra o TCC | Seed `@Profile("dev")`. Sem CRUD | Fatia F5 futura |
| Certificado de conclusão de TCC | RF-F3-006 / F3.7-D02 | F6.1 grava banca e limiar de horas. A consolidação das avaliações e a colação (F5.11) continuam “a definir”. **Não** houve emissão | Quando a colação ou a consolidação da banca definirem o gatilho |
| TCC (módulo) | RF-F1-008 / RF-F3-006 | Item 9.2 entregou acompanhamento, upload `bytea` e parecer individual em `/tccs`. Sem lote | Certificado e cadastro F5 seguem abertos |
| Diploma e colação | RF-F2-001 / F5.11 | O painel do egresso devolve `diploma`, `colacao`, `concluidoEm` e `kpis.situacaoDiploma` nulos. A UI diz que o registro ainda não está disponível. Não nasceu tabela `diploma` nem wizard de colação | F5.11 |
| Lista F1.19 do egresso | HU 19 critério 5 | `/certificados` segue `certificate.view_own` e `CertificadoAcesso` continua recusando situação EGRESSO. A reemissão do dono é `GET /egressos/me/certificados/{id}/reemissao` | Não abrir a lista de aluno nesta fatia |
| Expo F2 | RF-F2-001 | O app nativo não tem `/egresso/inicio`. O menu ignora o rel `egresso-inicio` (whitelist P0) e o egresso cai no `/inicio` com 403 do BFF | mobile-2 |
| Cadastro F5 de estágio | secretaria registra o estágio | Fatia 10: `POST`/`PUT /estagios` (`internship.manage`), UI `/secretaria/estagios`, orientador nulo. PDF continua `bytea` | MinIO, lote, TCC |
| MinIO / `arquivos` | upload presigned | PDF de estágio, de TCC e de certificado em `bytea`. Sem `modules/arquivos` e sem MinIO no compose. Download do TCC sai da API, sem URL de 15 min | Quando o compose tiver MinIO |
| Períodos por curso / F5.9 tipos | calendário semântico | F6.1 gravou só a duração 15/18 em `curso_configuracao`. `periodo_letivo` continua global | Schema por curso + tipos na F5.9 |
| Janelas pré-agendadas / inelegíveis | CA-02 / CA-06 | Motor v4.1 já tem os quatro modos; janelas desta fatia são ao vivo (15 min) | Pré-agendar e lista ao vivo quando a tela de criação pedir |
| CA-04 / `REVOGADO` | upload na F0.7; revogação | F0.7 verifica hash; sem upload de PDF; sem estado `REVOGADO` | Fora desta fatia |
| Hub / templates / push / FORWARD | F1.6, F3.8, F7.5, F3.4-D04 | Dispatcher SMTP já entrega F0.2 e deep-link | Não reabrir o item 6. FCM/push no Expo continua fora |
| Expo web | RNF-POR-01 | Item 8 cobre Expo Go / emulador / aparelho. Sem origem CORS extra, sem `X-SO2-Client` no `allowedHeaders` | Só se o Expo web for fatia; não usar `*` |
| Cookie nativo (B) | RN-F0.1-03 | Item 8 foi **(A)**: RN não persiste `so2_refresh` httpOnly de forma confiável. Body `{ refreshToken }` + SecureStore. Cookie da web permanece | Não reabrir o cookie da web; (B) só se alguém provar jar nativo ponta a ponta |
| Deliberação / CAAF / F5 no app | F3.4 / F4.1 / F5 | Menu nativo só rels P0 do aluno (`inicio`, `solicitacoes`, `eventos`, `contato`). Professor/secretaria: 403 honesto / item ausente | Fatia mobile-2; não misturar com o item 9 de módulos novos |
| Formativas / certificados no Expo | F1.10 / F1.19 | BFF mostra KPIs e pendências com href web; o app não implementa as telas | mobile-2 |
| Desvincular coordenador | F5.7 / F6.1 | F6.1 não zera `idCoordenador`. `Curso.atualizar` continua tratando `null` como "manter" | Picker da F7.1; não misturar com config curricular |
| Eventos de calendário | tipos semânticos em F5.9 | Só período letivo | Segunda aba quando o schema existir |
| Angular em `frontend/` | React oficial | Não é stack deste repo | Não recriar nem commitar |

Lombok: **conforme**. Zero ocorrências. Proibição registrada nas rules.
Aluno sem `idade`: **conforme** (RF-F5-003).
HATEOAS + `useActions`: **conforme** nas telas de dados **e** na nav (item 7, web e Expo).
IAM / JWT / Argon2id: **entregue**. CRUD acadêmico com FGAC (item 7).
Motor de solicitações: **entregue** (`0e882d2`) — não está mais inexistente.

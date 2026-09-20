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
| `/login` + IAM | JWT RS256 15 min, refresh `so2_refresh` httpOnly Secure SameSite=Lax Path=/auth, Argon2id, anti-enumeração, primeiro acesso senha+LGPD (`6997339`) |
| `/solicitacoes/nova` | Motor genérico `RequestType` + `form_schema` + `workflow_json` (`0e882d2`) |
| `/inicio` | BFF `GET /bff/dashboard/aluno`, degradação por bloco, HTTP 200, `eventosHoje`/`proximosEventos` via porta no módulo `presenca`, `_links.novaSolicitacao` só com `request.open` |
| Presença aluno | SECRET_SINGLE (`V005`), `/eventos`, Proof of Stay sem geofence |
| Hospedeiro F3.2 | SECRET_SINGLE (`V006`), PIN em claro só na host-session (`HostPinPort` / `HostPinStore` em memória) |
| F0.6 | `GET /publico/protocolos/{protocolo}` + UI loading / not-found / ok (hash truncado, sem PDF) |

Sessão JWT que não é aluno: BFF devolve 403; `/inicio` mostra empty honesto, sem “Olá, aluno”. Deep link: login respeita `state.from` se a rota for interna segura. Refresh falho após 401 limpa o access token e vai para `/erro/401` (CTA “Fazer login”), não para `/inicio` anônimo.

A ordem das fatias **depois** do P0 (deliberação → CAAF individual → QR, com COE só junto de estágio) está no README. Este arquivo lista dívida; não redefine o cronograma.

## Discrepâncias que permanecem (dívida consciente — não é P0)

| Item | Spec | Situação | Ação |
|---|---|---|---|
| Horas formativas no `/inicio` | KPI F1.1 | Query do módulo `formativas` (soma `APROVADA`); com cadastro acadêmico, `N / requeridas` após parecer CAAF. Falha do módulo **ou JWT sem `aluno`** → `null`, HTTP 200 (não inventa `0/0`) | Feito no item 3 |
| Certificados no `/inicio` | KPI F1.1 | Porta no módulo `certificados`; cadastro e zero → `0`; falha/sem cadastro → `null` | Feito no item 5 |
| F0.7 certificado público | Verificação de PDF/hash | `GET /publico/certificados/{hash}/verificacao` + JWKS + SubtleCrypto | Feito no item 5; CA-04 (upload) e `REVOGADO` ficam dívida |
| F3.1 dashboard professor | BFF próprio | Ausente; `/inicio` 403 honesto | BFF professor |
| QR / SECRET_DUAL / janela de saída | Presença v4.1 completa | Motor com os quatro modos (item 4) | Janelas pré-agendadas e lista ao vivo de inelegíveis continuam de fora |
| FGAC em `/academico/**` | `@PreAuthorize` | Fechado (item 7); capability da tela; 401/403 RFC 7807 | — |
| Nav HATEOAS | UI cega a perfil | `GET /auth/me`._links + `useActions` (item 7) | — |
| Dispatcher de e-mail | Outbox → SMTP | Dispatcher at-least-once + Mailpit (item 6). Hub F1.6, F3.8, F7.5, push e FORWARD ficam dívida | Templates / push / hub |
| HostPin em memória | PIN na host-session | Some no restart da API | Persistência ou reabertura de janela |
| Secretários do curso | RF-F5-004-a | V011 `curso_secretario` + seed TADS (item 7) | Claim JWT `cursoIds` e admin global (`user.manage_all`) ficam de fora |
| Fila CAAF por curso | F4.1 / comissão | Sem tabela `commission_member` | Filtro por comissão continua dívida |
| Períodos por curso / F5.9 tipos | calendário semântico | `periodo_letivo` global; `calendar.manage` all-or-nothing | Schema por curso + tipos quando F6.1/F5.9 abrirem |
| Portal admin F7.1–F7.9 | usuários, papéis, jobs, saúde | Fora desta fatia | Não misturar com o FGAC acadêmico |
| Seletor de usuários em F5.7 | Nome, Sigla, Coordenador, Horas, Secretários | Form web pede UUID cru de secretário e não envia `idCoordenador` (coordenador do TADS vem do seed) | Picker de usuário depende de F7.1; não implementar nesta fatia |
| `request.triage` | nome `dominio.acao` | Não aparece em `docs/`. É decisão de implementação derivada de RF-F5-002 (triagem da secretaria) para o dispatcher **não** mandar deep-link a quem só faz fila | Não alterar o item 6 (dispatcher/deep-link fechados) |
| `SolicitacaoCursoEscopo` | join aluno ↔ IAM | Duas queries por aluno (`findByIdentificador` + `findByEmail`); importa `IdentificadorLogin` (VO do IAM). Acoplamento por PORT (`CursoEscopoPort`, `UsuarioRepository`) é o padrão aceito do repo | Escala é limite consciente; não duplicar dados entre módulos |
| Fila F5.2 da secretaria | `/solicitacoes` com `request.view_curso` | Menu não emite mais o rel `solicitacoes` sem `request.view_own`; secretaria usa `deliberar` (`/solicitacoes?to=me`) | F5.2 é fatia futura — não implementar a fila central nesta correção |
| Claim JWT `cursoIds` | spec JwtFilter | Escopo só no use case | Incluir no token sem confiar só no claim |
| JWT vs `/auth/me` | capabilities | Enforcement (`@PreAuthorize`, assembler) lê authorities do JWT (TTL 15 min). `GET /auth/me` monta o menu com `ConsultarSessaoUseCase` → `usuario.authorities` do banco. Depois de conceder/revogar (inclusive `substituirAuthorities` no seed) o menu pode mudar até 15 min antes da API. Não é explorável: o JWT nunca concede mais do que foi assinado | Mesmo assunto do claim `cursoIds` ausente; não encurtar o TTL só por isso |
| ArchUnit / `AcademicoDevDataLoader` | regra 2 de dependência | `@Profile("dev")` em `academico.infrastructure` importa `iam.infrastructure.IamProperties` (e `UsuarioRepository`). Não roda em produção; será o primeiro alarme quando o ArchUnit entrar | Extrair porta de seed ou mover o loader |
| Nomes de endpoint | `docs/tcc-docs.md` / F5.6 / F5.8 | Spec fala `/students`, `/calendars`, `POST /calendars/periods`, `?slaBreached=true`. Código: `/academico/alunos`, `/academico/periodos`, parâmetro `atraso`. F5.9 já apontava `/academico/periodos`; F5.6 e F5.8 foram alinhados | **Não** renomear a API — quebraria o P0 e o frontend |
| Desvincular coordenador | F5.7 / F6.1 | `Curso.atualizar` trata `idCoordenador == null` como "manter". Não há operação que zere o coordenador | F6.1 vai precisar; picker de F5.7 continua dependente de F7.1 |
| Unicidade de GRR/e-mail em `aluno` | V002 UNIQUE global | `POST /academico/alunos` com GRR ou e-mail institucional já usado (mesmo de outro curso) devolve 409. GRR é identificador público da UFPR, não segredo — o 409 não é enumeração de dado sensível | Manter UNIQUE global; não scoped por curso |
| Config F6.1 | calendário, banca, regimento | Fora do CRUD de secretaria | Módulo coordenação |
| Eventos de calendário | tipos semânticos em F5.9 | Só período letivo | Segunda aba quando o schema existir |
| ArchUnit | regras de dependência | Não há teste | Adicionar no próximo módulo |
| Ports com `Pageable` | domain/application puros | Ports importam Spring Data | Extrair paginações próprias numa fatia seguinte |
| Angular em `frontend/` | React oficial | Não é stack deste repo | Não recriar nem commitar |
| Bucket4j + Redis | RNF-SEC-04 | Janela em memória no processo | Trocar quando houver Redis |
| Cobertura 85/70/75 | RNF de testes | Ampliar por módulo | Continuar nas fatias seguintes |

Lombok: **conforme**. Zero ocorrências. Proibição registrada nas rules.
Aluno sem `idade`: **conforme** (RF-F5-003).
HATEOAS + `useActions`: **conforme** nas telas de dados **e** na nav (item 7).
IAM / JWT / Argon2id: **entregue**. CRUD acadêmico com FGAC (item 7).
Motor de solicitações: **entregue** (`0e882d2`) — não está mais inexistente.

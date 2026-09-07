# Auditoria de fundação — SO2

Data: 2026-09-06. Fontes: `docs/tcc-docs.md`, `docs/telas-figma.md`, código e `.cursorrules`.

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
append-only. Login JWT RS256 15 min; cookie `so2_refresh` (`httpOnly; Secure; SameSite=Lax;
Path=/auth`); Argon2id; primeiro acesso (RF-F1-002); recuperação via Outbox (sem e-mail
síncrono). Telas React `/login`, `/recuperar-senha`, `/nova-senha`, `/primeiro-acesso`
ligadas. Access token só em memória.

## Plano — `@PreAuthorize` no CRUD acadêmico

O CRUD `/academico/**` permanece `permitAll` (sem `@PreAuthorize`) para não quebrar a
fundação: ITs, telas de secretaria e bootstrap ainda não têm matriz FGAC (F7) nem
vínculo usuário↔curso. Quando F7 existir:

1. Fechar `anyRequest()` para `authenticated()` (exceto F0 público e `/auth/*` anônimos).
2. Anotar comandos acadêmicos com capabilities (`curso.manage`, `student.manage`,
   `calendar.manage`) — nunca `hasRole`.
3. Continuar emitindo `_links` HATEOAS; a UI segue cega a perfil (`useActions`).
4. Escopo por curso (coordenação/secretaria) entra com a tabela N:N de secretários.

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
| Horas formativas no `/inicio` | KPI F1.1 | Query do módulo `formativas` (soma `APROVADA`); com cadastro acadêmico, validadas ainda 0 até CAAF. Falha do módulo **ou JWT sem `aluno`** → `null`, HTTP 200 (não inventa `0/0`) | CAAF individual (item 3 do plano no README) |
| Certificados no `/inicio` | KPI F1.1 | `null` (módulo inexistente) | Não fingir número; módulo certificados |
| F0.7 certificado público | Verificação de PDF/hash | `TelaPendente` | Módulo certificados |
| F3.1 dashboard professor | BFF próprio | Ausente; `/inicio` 403 honesto | BFF professor |
| QR / SECRET_DUAL / janela de saída | Presença v4.1 completa | Só SECRET_SINGLE | Item 4 do plano (depois de deliberação e CAAF individual) |
| FGAC em `/academico/**` | `@PreAuthorize` | `permitAll` | Depois da matriz F7 |
| Nav HATEOAS | UI cega a perfil | Atalhos de dev (Eventos prof., CRUD secretaria) | Esconder quando houver `_links` de menu |
| Dispatcher de e-mail | Outbox → SMTP | Eventos ficam `PENDING` | Módulo comunicação / Outbox |
| HostPin em memória | PIN na host-session | Some no restart da API | Persistência ou reabertura de janela |
| Secretários do curso | RF-F5-004-a | Só `idCoordenador` + horas | Tabela N:N |
| Config F6.1 | calendário, banca, regimento | Fora do CRUD de secretaria | Módulo coordenação |
| Eventos de calendário | tipos semânticos em F5.9 | Só período letivo | Segunda aba quando o schema existir |
| ArchUnit | regras de dependência | Não há teste | Adicionar no próximo módulo |
| Ports com `Pageable` | domain/application puros | Ports importam Spring Data | Extrair paginações próprias numa fatia seguinte |
| Angular em `frontend/` | React oficial | Não é stack deste repo | Não recriar nem commitar |
| Bucket4j + Redis | RNF-SEC-04 | Janela em memória no processo | Trocar quando houver Redis |
| Cobertura 85/70/75 | RNF de testes | Ampliar por módulo | Continuar nas fatias seguintes |

Lombok: **conforme**. Zero ocorrências. Proibição registrada nas rules.
Aluno sem `idade`: **conforme** (RF-F5-003).
HATEOAS + `useActions`: **conforme** nas telas de dados (atalhos de nav de dev são dívida).
IAM / JWT / Argon2id: **entregue** (CRUD acadêmico ainda sem FGAC).
Motor de solicitações: **entregue** (`0e882d2`) — não está mais inexistente.

# Auditoria de fundação — SO2

Data: 2026-10-03. Fontes: `docs/tcc-docs.md`, `docs/telas-figma.md`, código e `.cursorrules`.

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

Sessão sem rel `painel`: `/inicio` mostra empty honesto, sem “Olá, aluno”. Professor com `dashboard.view_self_professor` carrega `GET /bff/dashboard/professor`. Deep link: login respeita `state.from` se a rota for interna segura. Refresh falho após 401 limpa o access token e vai para `/erro/401` (CTA “Fazer login”), não para `/inicio` anônimo.

A ordem das fatias **depois** do P0 (deliberação → CAAF individual → QR, com COE só junto de estágio) está no README. Este arquivo lista dívida; não redefine o cronograma.

## Ordem das fatias 10+ — fica no README

A varredura de tudo o que a spec ainda cobra e o código não entrega (66 RFs e 72 telas F0–F8 classificados em feito / parcial / ausente / P3) e a ordem numerada a partir de **10** estão na seção **O que falta (pós-9.5)** do [`README.md`](../README.md). A tabela de dívida **abaixo** continua sendo a fonte do detalhe por item — não duplicar a ordem aqui, e não ler esta tabela como cronograma.

## Já fechado (não reabrir)

Quem ler a tabela de dívida **abaixo** não deve achar que `/academico/**` ainda é `permitAll` nem que o menu ainda é atalho de dev.

| Item | Situação (uma linha) |
|---|---|
| FGAC em `/academico/**` | Item 7: JWT + capability da tela (`course.manage` / `subject.manage` / `user.manage_students` / `calendar.manage`); anônimo 401; sem cap 403; fora do escopo 404. **Não** está `permitAll`. POST inclui o criador na mesma TX; PUT re-adiciona quem edita (anti-lockout). `_links.criar` da coleção é incondicional (o GET já passou no `@PreAuthorize`). |
| Nav HATEOAS | Item 7: `GET /auth/me`._links + `useActions`. `MenuLinks` é o único ponto que olha caps para o menu. |
| Dispatcher SMTP | Item 6: Outbox → Mailpit at-least-once. Hub F1.6 / F3.8 e templates F7.5 estão nas linhas abaixo. Push/FCM continua fora. |
| F0.7 verificação pública | Item 5: `GET /publico/certificados/{hash}/verificacao` + JWKS + SubtleCrypto. CA-04 (upload) e `REVOGADO` continuam dívida. |
| KPIs horas / certificados no `/inicio` | Itens 3 e 5: soma `APROVADA` e contagem do módulo; deixam de ser “Indisponível” após a CAAF. Falha/sem cadastro → `null` (HTTP 200). |
| Presença QR \| SECRET × SINGLE \| DUAL | Item 4: motor v4.1 com os quatro modos. Janelas pré-agendadas e lista ao vivo de inelegíveis continuam dívida. |
| V011 `curso_secretario` | Item 7: N:N + seed TADS + `CursoEscopoPort`. Claim JWT `cursoIds` **não** entrou (dívida abaixo). |
| P0 Expo aluno | Item 8: login / primeiro acesso / BFF `/inicio` / nova solicitação / presença SECRET+QR. Refresh **(A)** body + Keychain. Web cookie intacto. |
| Egresso F2 | Item 9.3: `alumni.view_own`, menu `egresso-inicio`, `GET /egressos/me`, reemissão do PDF já gravado (mesmo `hash_sha256` e mesma assinatura, 404 se não for o dono). Sem migration. Diploma e a whitelist Expo estão nas linhas abaixo. A lista F1.19 do aluno continua fora. |
| F3.1 dashboard professor | `GET /bff/dashboard/professor` (`dashboard.view_self_professor` em `CatalogoPapeis.professor`; `MenuLinks.painel` aponta o rel). `/inicio` do professor carrega esse BFF; não é 403. |
| F5.2 fila central | Fila em `/solicitacoes` com `request.view_curso`, CSV de atrasados e V020. |
| F6.2 relatórios | `GET /reports/coordinator`. |
| F7.1–F7.8 admin | `/admin/usuarios`, `/admin/perfis`, `/admin/autoridades`, `/admin/tipos-solicitacao`, `/admin/templates-comunicacao`, `/admin/jobs`, `/admin/audit-log`. F7.9 continua fora. |
| Picker F5.7 | `GET /iam/usuarios`. Id inexistente → 422 em `CursoApplicationService.exigirUsuariosExistentes`. |
| Hub F1.6 / F3.8 e templates F7.5 | `/communications` e `/admin/templates-comunicacao`. Push/FCM continua fora. |
| Diploma F5.11 | V019 e `/diplomas`. |
| Cadastro F5 de TCC | `POST`/`PUT /tccs` e `/secretaria/tccs`. |
| V017 `comissao_membro` | Pool CAAF. Lote só com presença já validada. |
| Expo whitelist | `frontend-react-native/src/lib/navMenu.ts` inclui `egresso-inicio`, formativas, certificados, estágios e TCCs. Deliberação, CAAF, COE, F5, F6 e F7 no app continuam de fora de propósito. |
| F4.2 pool COE | `GET /comissoes/coe` e `POST /comissoes/coe/atribuicoes`. Parecer continua individual. |
| Cadastro F5 de estágio | `POST`/`PUT /estagios` e `/secretaria/estagios`. |
| MinIO / arquivos | Compose, `modules/arquivos`, V018 `storage_key`, URL pré-assinada. Antivírus e versionamento continuam fora. |

## Ainda aberto (dívida consciente — não é P0)

Só o que **ainda** está aberto, como decisão — não como esquecimento. Não reabrir o que está em “Já fechado”.

| Item | Spec | Situação | Ação |
|---|---|---|---|
| F7.9 saúde + Grafana | RF-F7-007, P3 na spec | Actuator no ar (`health`, `info`, `metrics`; só `health` é `permitAll`). Sem a tela | Decisão: fora. Não implementar Grafana |
| Kanban F5.19 | tela P3 opcional, flag `tasks.enabled` | RF-F5-012 não bloqueia o MVP | Decisão: fora. Não implementar kanban |
| HostPin em memória | PIN na host-session | `HostPinStore` em memória; some no restart da API | Decisão: sem persistência |
| Claim JWT `cursoIds` | spec JwtFilter | Escopo só no use case (`CursoEscopoPort`). O token não leva `cursoIds` | Decisão: não incluir o claim |
| JWT vs `/auth/me` | capabilities | Enforcement (`@PreAuthorize`, assembler) lê authorities do JWT (TTL 15 min). `GET /auth/me` monta o menu com `ConsultarSessaoUseCase` → `usuario.authorities` do banco. Depois de conceder/revogar (inclusive `substituirAuthorities` no seed) o menu pode mudar até 15 min antes da API. Não é explorável: o JWT nunca concede mais do que foi assinado | Mesmo assunto do claim `cursoIds` ausente; não encurtar o TTL só por isso |
| `request.triage` | nome `dominio.acao` | Não aparece em `docs/`. Decisão de implementação (RF-F5-002): o dispatcher não manda deep-link a quem só faz fila | Não alterar o dispatcher |
| `SolicitacaoCursoEscopo` | join aluno ↔ IAM | Duas queries por aluno (`findByIdentificador` + `findByEmail`); importa `IdentificadorLogin` (VO do IAM). Acoplamento por PORT (`CursoEscopoPort`, `UsuarioRepository`) é o padrão aceito do repo — **não** é violação de dependência (é o "só ports" da regra 3) | Escala é limite consciente; não duplicar dados entre módulos |
| ArchUnit / Testcontainers | regra 2 e ITs | Travessias esperam o ArchUnit: `IamPrincipal` importado por controllers de outros módulos; `IamDevDataLoader` importa `EmailMascarado`; `DespacharOutboxUseCase` importa `ComunicacaoProperties`. `Solicitacoes` via port não entra. Testcontainers não entra | Decisão: não mover `IamPrincipal` e não subir Testcontainers |
| Nomes de endpoint | `docs/tcc-docs.md` / F5.6 / F5.7 / F5.8 | Spec fala `/students`, `/secretaria/cursos`, `/calendars`, `POST /calendars/periods`. Código: `/academico/alunos`, `/academico/cursos`, `/academico/periodos`. Atrasados usa `slaBreached`; a fila também aceita `atraso`. F5.9 já apontava `/academico/periodos`; F5.6, F5.7 e F5.8 foram alinhados à API real | **Não** renomear a API — quebraria o P0 e o frontend |
| Desativar curso | spec `PATCH /secretaria/cursos/{id} {ativo:false}` + RN-F5-004-04 (histórico preservado) | Código: `DELETE` com guarda de vínculos (`CursoApplicationService` → 409 "Curso possui alunos ou disciplinas vinculados", coberto por IT) e `ativo` só chega via PUT. Sem PATCH. A intenção da RN (não apagar histórico) está no 409 | Não implementar o PATCH nesta fatia |
| Unicidade de GRR/e-mail em `aluno` | V002 UNIQUE global | `POST /academico/alunos` com GRR ou e-mail institucional já usado (mesmo de outro curso) devolve 409. GRR é identificador público da UFPR, não segredo — o 409 não é enumeração de dado sensível | Manter UNIQUE global; não scoped por curso |
| Ports com `Pageable` | domain/application puros | Ports importam Spring Data | Extrair paginações próprias numa fatia seguinte |
| Rate limit em memória | RNF-SEC-04 | Janela em memória no processo. Sem Bucket4j e sem Redis | Decisão: não trocar nesta etapa |
| Cobertura 85/70/75 | RNF de testes | Ampliar por módulo | Continuar nas fatias seguintes |
| Combo Alunos/Disciplinas × `course.manage` | seletor de curso nas telas F5.8 / alunos | O combo reusa `GET /academico/cursos` (exige `course.manage`). Sem ela o form fica desabilitado | Decisão: sem endpoint novo de busca |
| H2 sem Flyway | migrations imutáveis; a última no código é a **V034** | `application-test.yml`: H2, `ddl-auto: create-drop`, `flyway.enabled: false`. Nenhuma migration passa pelo Flyway no perfil `test`. `mvn test` não executa classes `*IT` (Surefire sem `includes`). Em dev/prod o `ddl-auto` segue `validate` | Decisão. Não mudar o `pom` para rodar `*IT` e não subir Testcontainers |
| Paginação da UI acadêmica | F5.7 pede `Pagination footer`; API emite `_links.first/last/next/prev` | `Cursos.tsx`, `Disciplinas.tsx`, `Alunos.tsx` e `Calendarios.tsx` ficam na primeira página, sem controle. Acima de 20 registros os dados ficam invisíveis sem aviso | Decisão: não implementar agora |
| Certificado de conclusão de TCC | RF-F3-006 / F3.7-D02 | Acompanhamento e parecer existem em `/tccs`. Não há emissão de certificado de conclusão | Decisão: fora até a banca definir o gatilho |
| Lista F1.19 do egresso | HU 19 critério 5 | `/certificados` segue `certificate.view_own` e `CertificadoAcesso.exigirAlunoAtivo` recusa situação EGRESSO. A reemissão do dono é `GET /egressos/me/certificados/{id}/reemissao` | Não abrir a lista de aluno |
| Períodos por curso / F5.9 tipos | calendário semântico | F6.1 gravou só a duração 15/18 em `curso_configuracao`. `periodo_letivo` continua global | Schema por curso fica fora |
| Janelas pré-agendadas / inelegíveis | CA-02 / CA-06 | Motor v4.1 já tem os quatro modos; janelas desta fatia são ao vivo (15 min) | Pré-agendar e lista ao vivo quando a tela de criação pedir |
| CA-04 / `REVOGADO` | upload na F0.7; revogação | F0.7 verifica hash. Sem upload de PDF e sem estado `REVOGADO` | Decisão: fora |
| FCM / push | canal adicional | O hub entrega in-app e e-mail. Sem FCM no Expo | Decisão: fora |
| Expo web | RNF-POR-01 | O item 8 cobre Expo Go, emulador e aparelho. Sem origem CORS extra e sem `X-SO2-Client` em `allowedHeaders` | Decisão: fora. Sem `*` |
| Cookie nativo (B) | RN-F0.1-03 | O item 8 ficou em **(A)**: body `{ refreshToken }` + SecureStore. O cookie da web permanece | Decisão: (B) só se alguém provar jar nativo |
| `Curso.atualizar` e coordenador nulo | F5.7 / F6.1 | `Curso.atualizar` trata `idCoordenador` nulo como manter | Decisão: não zerar o coordenador por omissão |
| FORWARD | F3.4-D04 | O motor não tem transição FORWARD | Decisão: fora |
| Eventos de calendário | tipos semânticos em F5.9 | Só período letivo | Segunda aba quando o schema existir |
| Angular em `frontend/` | React oficial | Não é stack deste repo | Não recriar nem commitar |

Lombok: **conforme**. Zero ocorrências. Proibição registrada nas rules.
Aluno sem `idade`: **conforme** (RF-F5-003).
HATEOAS + `useActions`: **conforme** nas telas de dados **e** na nav (item 7, web e Expo).
IAM / JWT / Argon2id: **entregue**. CRUD acadêmico com FGAC (item 7).
Motor de solicitações: **entregue** (`0e882d2`) — não está mais inexistente.

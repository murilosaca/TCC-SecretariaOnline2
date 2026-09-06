# Mapeamento de interface — Telas Figma (SO2)

Índice oficial das especificações de tela do TCC. O detalhe de cada tela
está em [`docs/telas/`](telas/). Convenções globais: [`docs/telas/00-CONVENCOES.md`](telas/00-CONVENCOES.md).
Índice legado por arquivo: [`docs/telas/00-INDICE.md`](telas/00-INDICE.md).

Fonte de verdade visual e de rotas da UI. A API e as regras de negócio
seguem [`docs/tcc-docs.md`](tcc-docs.md). O frontend oficial é **React 18 + Vite**
(RNF-POR-01), não Angular.

Regras imutáveis das convenções:

- Dashboard do aluno (`F1.1`) é o blueprint das telas autenticadas.
- Ações de UI só via `_links` HATEOAS (`useActions`).
- Shells: `AuthLayout` (público), `AppLayout` (autenticado).
- P0 (MVP): `/login`, `/inicio`, `/solicitacoes/nova`, presença em evento.

Pastas no repositório: `docs/telas/telas0` … `docs/telas/telas8`.

## F0 — Público (`AuthLayout`)

| Tela | Arquivo | Rota | RF |
|---|---|---|---|
| Login | [F0.1](telas/telas0/F0.1-login.md) | `/login` | RF-F0-001 |
| Recuperar senha | [F0.2](telas/telas0/F0.2-recuperar-senha.md) | `/recuperar-senha` | RF-F0-002 |
| Nova senha | [F0.3](telas/telas0/F0.3-nova-senha.md) | `/nova-senha?token=` | RF-F0-003 |
| Contato | [F0.4](telas/telas0/F0.4-contato.md) | `/contato` | RF-F0-004 |
| Erro HTTP | [F0.5](telas/telas0/F0.5-erro.md) | `/erro/:codigo` | RF-F0-005 |
| Verificar protocolo | [F0.6](telas/telas0/F0.6-verificar-protocolo.md) | `/publico/verificar-protocolo/:id` | RF-F0-006 |
| Verificar certificado | [F0.7](telas/telas0/F0.7-verificar-certificado.md) | `/publico/verificar-certificado/:hash` | RF-F0-007 |

## F1 — Aluno

| Tela | Rota | RF |
|---|---|---|
| Dashboard | `/inicio` | RF-F1-001 |
| Primeiro acesso | `/primeiro-acesso` | RF-F1-002 |
| Perfil / segurança / notificações | `/perfil`, `/perfil/seguranca`, `/perfil/notificacoes` | RF-F1-003 |
| Comunicação | `/comunicacao` | RF-F1-004 |
| Solicitações | `/solicitacoes`, `/solicitacoes/nova`, `/solicitacoes/:id` | RF-F1-005 |
| Formativas | `/formativas`, `/formativas/nova`, `/formativas/:id` | RF-F1-006 |
| Estágios / TCC | `/estagios`, `/tccs` | RF-F1-007/008 |
| Eventos / presença | `/eventos`, `/eventos/:id/presenca` | RF-F1-009 |
| Certificados / atendimentos | `/certificados`, `/meus-atendimentos` | RF-F1-010/011 |

## F2 — Egresso

| Tela | Rota | RF |
|---|---|---|
| Dashboard read-only | `/egresso/inicio` | RF-F2-001 |

## F3 — Professor

| Tela | Rota | RF |
|---|---|---|
| Dashboard | `/inicio` | RF-F3-001 |
| Eventos | `/professor/eventos`, `.../:id`, `.../:id/operacao` | RF-F3-002 |
| Deliberação | `/solicitacoes?to=me`, `/solicitacoes/:id/deliberar` | RF-F3-003 |
| Formativas / estágios / TCC | `/formativas?to=me`, `/estagios?to=me`, `/tccs?to=me` | RF-F3-004..006 |
| Publicar comunicado | `/comunicacao/publicar` | RF-F3-007 |

## F4 — Comissões

| Tela | Rota | RF |
|---|---|---|
| Pool CAAF | `/comissoes/caaf` | RF-F4-001 |
| Pool COE | `/comissoes/coe` | RF-F4-002 |

## F5 — Secretaria (`AppLayout`)

| Tela | Arquivo | Rota | RF |
|---|---|---|---|
| Dashboard | [F5.1](telas/telas5/F5.1-inicio-secretaria.md) | `/inicio` | RF-F5-001 |
| Alunos | [F5.6](telas/telas5/F5.6-secretaria-alunos.md) | `/secretaria/alunos` | RF-F5-003 |
| Cursos | [F5.7](telas/telas5/F5.7-secretaria-cursos.md) | `/secretaria/cursos` | RF-F5-004-a |
| Disciplinas | [F5.8](telas/telas5/F5.8-secretaria-disciplinas.md) | `/secretaria/disciplinas` | RF-F5-004-b |
| Calendários | [F5.9](telas/telas5/F5.9-secretaria-calendarios.md) | `/secretaria/calendarios` | RF-F5-004-c |
| Egressos | [F5.10](telas/telas5/F5.10-secretaria-egressos.md) | `/secretaria/egressos` | RF-F5-005 |

Demais telas F5 (fila, atrasados, diplomas, eventos, import/export, estatísticas,
tarefas) estão em `docs/telas/telas5/`.

## F6 — Coordenação

| Tela | Rota | RF |
|---|---|---|
| Configurar curso | `/coordenacao/cursos/:id/configurar` | RF-F6-001 |
| Relatórios | `/coordenacao/relatorios` | RF-F6-002 |

## F7 — Admin

Rotas sob `/admin/*` (usuários, perfis, autoridades, tipos de solicitação,
templates, jobs, audit-log, saúde). RF-F7-001..007.

## F8 — Transversal

| Tela | Rota | RF |
|---|---|---|
| Busca global | `/buscar?q=` | RF-F8-001 |
| Suporte / FAQ | `/suporte` | RF-F8-002 |

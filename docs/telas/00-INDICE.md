# Índice — Especificações Figma por Tela

**Projeto:** SecretariaOnline2 (UFPR SEPT)  
**Versão:** v4.1 (presença configurável em eventos)  
**Total de telas:** 72 arquivos + este índice  
**Blueprint visual imutável:** DashboardA (Versão A) — ver `F1.1-inicio-aluno.md`

---

## Como usar estes arquivos

Cada `.md` é uma **instrução de design** para o agente Figma (`figma-generate-design`, `figma-use`). Antes de criar qualquer frame:

1. Ler `00-CONVENCOES.md` (tokens, shells, checklist global).
2. Ler `prompts/00-FIGMA-PAGE-CONTEXT.md` (regra de página MCP — **obrigatório** antes de `use_figma`).
3. Colar o **prompt da fase** (`prompts/0promptTelas.md` … `prompts/8promptTelas.md`) no Cursor com `@agents/ux-ui-specialist.md`.
4. Abrir o arquivo da rota específica (`F*.md`).
5. Reutilizar componentes `DS/*` do inventário (`designSystem/inventario-design-system.md`).
6. **Nunca** usar hex/px soltos — apenas Variables Figma vinculadas.

### Prompts por fase (Figma)

| Fluxo | Prompt | Telas |
|-------|--------|-------|
| F0 Público | [0promptTelas.md](./prompts/0promptTelas.md) | 7 |
| F1 Aluno | [1promptTelas.md](./prompts/1promptTelas.md) | 20 |
| F2 Egresso | [2promptTelas.md](./prompts/2promptTelas.md) | 1 |
| F3 Professor | [3promptTelas.md](./prompts/3promptTelas.md) | 10 |
| F4 Comissões | [4promptTelas.md](./prompts/4promptTelas.md) | 2 |
| F5 Secretaria | [5promptTelas.md](./prompts/5promptTelas.md) | 19 |
| F6 Coordenação | [6promptTelas.md](./prompts/6promptTelas.md) | 2 |
| F7 Admin | [7promptTelas.md](./prompts/7promptTelas.md) | 9 |
| F8 Cross-cutting | [8promptTelas.md](./prompts/8promptTelas.md) | 2 |

**Regra MCP (todas as fases):** [00-FIGMA-PAGE-CONTEXT.md](./prompts/00-FIGMA-PAGE-CONTEXT.md)

**Design System:** https://www.figma.com/design/gF97YuhDuYr5Sy9wagZLoJ/designSystem-v2  
**Arquivo telas:** https://www.figma.com/design/y1ZC44ThrXH0CIpEWZITh6/secretariaOnline2

---

## F0 — Público / Não autenticado

| Código | Arquivo | Rota |
|--------|---------|------|
| F0.1 | [F0.1-login.md](./F0.1-login.md) | `/login` |
| F0.2 | [F0.2-recuperar-senha.md](./F0.2-recuperar-senha.md) | `/recuperar-senha` |
| F0.3 | [F0.3-nova-senha.md](./F0.3-nova-senha.md) | `/nova-senha?token=` |
| F0.4 | [F0.4-contato.md](./F0.4-contato.md) | `/contato` |
| F0.5 | [F0.5-erro.md](./F0.5-erro.md) | `/erro/:codigo` |
| F0.6 | [F0.6-verificar-protocolo.md](./F0.6-verificar-protocolo.md) | `/publico/verificar-protocolo/:id` |
| F0.7 | [F0.7-verificar-certificado.md](./F0.7-verificar-certificado.md) | `/publico/verificar-certificado/:hash` |

## F1 — Aluno

| Código | Arquivo | Rota |
|--------|---------|------|
| F1.1 | [F1.1-inicio-aluno.md](./F1.1-inicio-aluno.md) | `/inicio` |
| F1.2 | [F1.2-primeiro-acesso.md](./F1.2-primeiro-acesso.md) | `/primeiro-acesso` |
| F1.3 | [F1.3-perfil.md](./F1.3-perfil.md) | `/perfil` |
| F1.4 | [F1.4-perfil-seguranca.md](./F1.4-perfil-seguranca.md) | `/perfil/seguranca` |
| F1.5 | [F1.5-perfil-notificacoes.md](./F1.5-perfil-notificacoes.md) | `/perfil/notificacoes` |
| F1.6 | [F1.6-comunicacao.md](./F1.6-comunicacao.md) | `/comunicacao` |
| F1.7 | [F1.7-solicitacoes-lista.md](./F1.7-solicitacoes-lista.md) | `/solicitacoes` |
| F1.8 | [F1.8-solicitacoes-nova.md](./F1.8-solicitacoes-nova.md) | `/solicitacoes/nova` |
| F1.9 | [F1.9-solicitacoes-detalhe.md](./F1.9-solicitacoes-detalhe.md) | `/solicitacoes/:id` |
| F1.10 | [F1.10-formativas-lista.md](./F1.10-formativas-lista.md) | `/formativas` |
| F1.11 | [F1.11-formativas-nova.md](./F1.11-formativas-nova.md) | `/formativas/nova` |
| F1.12 | [F1.12-formativas-detalhe.md](./F1.12-formativas-detalhe.md) | `/formativas/:id` |
| F1.13 | [F1.13-estagios-lista.md](./F1.13-estagios-lista.md) | `/estagios` |
| F1.14 | [F1.14-estagios-detalhe.md](./F1.14-estagios-detalhe.md) | `/estagios/:id` |
| F1.15 | [F1.15-tccs-lista.md](./F1.15-tccs-lista.md) | `/tccs` |
| F1.16 | [F1.16-tccs-detalhe.md](./F1.16-tccs-detalhe.md) | `/tccs/:id` |
| F1.17 | [F1.17-eventos-lista.md](./F1.17-eventos-lista.md) | `/eventos` |
| F1.18 | [F1.18-eventos-presenca.md](./F1.18-eventos-presenca.md) | `/eventos/:id/presenca` |
| F1.19 | [F1.19-certificados.md](./F1.19-certificados.md) | `/certificados` |
| F1.20 | [F1.20-meus-atendimentos.md](./F1.20-meus-atendimentos.md) | `/meus-atendimentos` |

## F2 — Egresso

| Código | Arquivo | Rota |
|--------|---------|------|
| F2.1 | [F2.1-egresso-inicio.md](./F2.1-egresso-inicio.md) | `/egresso/inicio` |

## F3 — Professor

| Código | Arquivo | Rota |
|--------|---------|------|
| F3.1 | [F3.1-inicio-professor.md](./F3.1-inicio-professor.md) | `/inicio` (visão professor) |
| F3.2a | [F3.2-professor-eventos-lista.md](./F3.2-professor-eventos-lista.md) | `/professor/eventos` |
| F3.2b | [F3.2-professor-eventos-detalhe.md](./F3.2-professor-eventos-detalhe.md) | `/professor/eventos/:id` |
| F3.2c | [F3.2-professor-eventos-operacao.md](./F3.2-professor-eventos-operacao.md) | `/professor/eventos/:id/operacao` |
| F3.3 | [F3.3-solicitacoes-deliberar-fila.md](./F3.3-solicitacoes-deliberar-fila.md) | `/solicitacoes?to=me` |
| F3.4 | [F3.4-solicitacoes-deliberar.md](./F3.4-solicitacoes-deliberar.md) | `/solicitacoes/:id/deliberar` |
| F3.5 | [F3.5-formativas-revisao.md](./F3.5-formativas-revisao.md) | `/formativas?to=me` |
| F3.6 | [F3.6-estagios-revisao.md](./F3.6-estagios-revisao.md) | `/estagios?to=me` |
| F3.7 | [F3.7-tccs-revisao.md](./F3.7-tccs-revisao.md) | `/tccs?to=me` |
| F3.8 | [F3.8-comunicacao-publicar.md](./F3.8-comunicacao-publicar.md) | `/comunicacao/publicar` |

## F4 — Comissões

| Código | Arquivo | Rota |
|--------|---------|------|
| F4.1 | [F4.1-comissoes-caaf.md](./F4.1-comissoes-caaf.md) | `/comissoes/caaf` |
| F4.2 | [F4.2-comissoes-coe.md](./F4.2-comissoes-coe.md) | `/comissoes/coe` |

## F5 — Secretaria

| Código | Arquivo | Rota |
|--------|---------|------|
| F5.1 | [F5.1-inicio-secretaria.md](./F5.1-inicio-secretaria.md) | `/inicio` (visão secretaria) |
| F5.2 | [F5.2-solicitacoes-fila.md](./F5.2-solicitacoes-fila.md) | `/solicitacoes` (fila central) |
| F5.3 | [F5.3-solicitacoes-nova-interna.md](./F5.3-solicitacoes-nova-interna.md) | `/solicitacoes/nova` (interna) |
| F5.4 | [F5.4-solicitacoes-deliberar-secretaria.md](./F5.4-solicitacoes-deliberar-secretaria.md) | `/solicitacoes/:id/deliberar` |
| F5.5 | [F5.5-secretaria-atrasados.md](./F5.5-secretaria-atrasados.md) | `/secretaria/atrasados` |
| F5.6 | [F5.6-secretaria-alunos.md](./F5.6-secretaria-alunos.md) | `/secretaria/alunos` |
| F5.7 | [F5.7-secretaria-cursos.md](./F5.7-secretaria-cursos.md) | `/secretaria/cursos` |
| F5.8 | [F5.8-secretaria-disciplinas.md](./F5.8-secretaria-disciplinas.md) | `/secretaria/disciplinas` |
| F5.9 | [F5.9-secretaria-calendarios.md](./F5.9-secretaria-calendarios.md) | `/secretaria/calendarios` |
| F5.10 | [F5.10-secretaria-egressos.md](./F5.10-secretaria-egressos.md) | `/secretaria/egressos` |
| F5.11 | [F5.11-secretaria-diplomas.md](./F5.11-secretaria-diplomas.md) | `/secretaria/diplomas` |
| F5.12 | [F5.12-secretaria-autorizacoes-imagem.md](./F5.12-secretaria-autorizacoes-imagem.md) | `/secretaria/autorizacoes-imagem` |
| F5.13 | [F5.13-secretaria-atendimentos.md](./F5.13-secretaria-atendimentos.md) | `/secretaria/atendimentos` |
| F5.14 | [F5.14-secretaria-eventos-lista.md](./F5.14-secretaria-eventos-lista.md) | `/secretaria/eventos` |
| F5.15 | [F5.15-secretaria-eventos-operacao.md](./F5.15-secretaria-eventos-operacao.md) | `/secretaria/eventos/:id/operacao` |
| F5.16 | [F5.16-secretaria-importacoes.md](./F5.16-secretaria-importacoes.md) | `/secretaria/importacoes` |
| F5.17 | [F5.17-secretaria-exportacoes.md](./F5.17-secretaria-exportacoes.md) | `/secretaria/exportacoes` |
| F5.18 | [F5.18-secretaria-estatisticas.md](./F5.18-secretaria-estatisticas.md) | `/secretaria/estatisticas` |
| F5.19 | [F5.19-secretaria-tarefas.md](./F5.19-secretaria-tarefas.md) | `/secretaria/tarefas` |

## F6 — Coordenação

| Código | Arquivo | Rota |
|--------|---------|------|
| F6.1 | [F6.1-coordenacao-configurar-curso.md](./F6.1-coordenacao-configurar-curso.md) | `/coordenacao/cursos/:id/configurar` |
| F6.2 | [F6.2-coordenacao-relatorios.md](./F6.2-coordenacao-relatorios.md) | `/coordenacao/relatorios` |

## F7 — Admin

| Código | Arquivo | Rota |
|--------|---------|------|
| F7.1 | [F7.1-admin-usuarios.md](./F7.1-admin-usuarios.md) | `/admin/usuarios` |
| F7.2 | [F7.2-admin-perfis.md](./F7.2-admin-perfis.md) | `/admin/perfis` |
| F7.3 | [F7.3-admin-autoridades.md](./F7.3-admin-autoridades.md) | `/admin/autoridades` |
| F7.4 | [F7.4-admin-tipos-solicitacao.md](./F7.4-admin-tipos-solicitacao.md) | `/admin/tipos-solicitacao` |
| F7.5 | [F7.5-admin-templates-comunicacao.md](./F7.5-admin-templates-comunicacao.md) | `/admin/templates-comunicacao` |
| F7.6 | [F7.6-admin-jobs.md](./F7.6-admin-jobs.md) | `/admin/jobs` |
| F7.7 | [F7.7-admin-audit-log.md](./F7.7-admin-audit-log.md) | `/admin/audit-log` |
| F7.8 | [F7.8-admin-reset-senha.md](./F7.8-admin-reset-senha.md) | `/admin/usuarios/:id/reset-senha` |
| F7.9 | [F7.9-admin-sistema-saude.md](./F7.9-admin-sistema-saude.md) | `/admin/sistema/saude` |

## F8 — Cross-cutting

| Código | Arquivo | Rota |
|--------|---------|------|
| F8.1 | [F8.1-buscar.md](./F8.1-buscar.md) | `/buscar?q=` |
| F8.2 | [F8.2-suporte.md](./F8.2-suporte.md) | `/suporte` |

## Convenções compartilhadas

- [00-CONVENCOES.md](./00-CONVENCOES.md) — tokens, shells, grid, checklist Figma
- [prompts/00-FIGMA-PAGE-CONTEXT.md](./prompts/00-FIGMA-PAGE-CONTEXT.md) — contexto de página MCP (`setCurrentPageAsync`, sem órfãos na Page 1)

## Priorização MVP

| Fase | Telas |
|------|-------|
| **MVP v1** | F0.1, F1.2, F1.1 |
| **MVP v2** | F1.7, F1.8, F1.9 |
| **P2** | Demais telas conforme roadmap do TCC |

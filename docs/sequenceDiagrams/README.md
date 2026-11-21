# Diagramas de sequência (SO2)

Fonte de verdade narrativa: `docs/tcc-docs.md` (tópico 8 — Histórias, telas e diagramas por módulo).

Cada arquivo cobre **um** cenário (`F0.1-a`, `F1.8-D01`, …). O PNG/SVG exportado preenche o slot `<!-- image -->` correspondente no `tcc-docs.md`.

## Convenções

| Item | Padrão |
|---|---|
| Lifelines | exatamente os **Atores** do texto da HU |
| Um arquivo | um cenário (happy path **ou** um erro) |
| Self-call | no máximo **1** por diagrama (ex.: Argon2id no UseCase) |
| Mensagens HTTP | verbo + path + status quando relevante |
| Persistência | `SELECT` / `INSERT` / `UPDATE` / `COMMIT` no Postgres |
| Nomes | IDs do doc (`F0.1-a`), não inventar novos |

## Como exportar para a monografia

1. Abra o `.puml` no [PlantUML Online](https://www.plantuml.com/plantuml/uml) ou na extensão PlantUML do VS Code/Cursor.
2. Exporte **PNG** (ou SVG) em alta resolução.
3. Insira a figura no Word/PDF no lugar do `<!-- image -->` da HU, com legenda no padrão: `FIGURA X — Diagrama de sequência F0.1-a (login happy path). Fonte: Elaborado pelos autores (2026).`

Há também um bloco Mermaid no `.md` de cada cenário (preview no GitHub/Cursor).
O preview do Cursor é mais restrito: evite `autonumber`, `{ }`, `( )`, `;`, `=` e acentos nas setas do Mermaid.
O `.puml` continua sendo a fonte para exportar a figura da monografia (lá esses caracteres são ok).

## Pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F0.1-a | `F0/F0.1-a-login-happy-path.*` | Login happy path |
| F0.1-b | `F0/F0.1-b-primeiro-acesso.*` | Login → `/primeiro-acesso` |
| F0.1-c | `F0/F0.1-c-credenciais-invalidas.*` | 401 anti-enumeração |
| F0.1-d | `F0/F0.1-d-rate-limit.*` | 429 rate limit |
| F0.1-e | `F0/F0.1-e-conta-bloqueada.*` | 401 conta bloqueada |
| F0.1-f | `F0/F0.1-f-refresh-reuso.*` | Refresh reusado |
| F0.2-a | `F0/F0.2-a-reset-email-cadastrado.*` | Reset — e-mail cadastrado |
| F0.2-b | `F0/F0.2-b-reset-email-inexistente.*` | Reset — anti-enumeração |
| F0.2-c | `F0/F0.2-c-reset-rate-limit.*` | Reset — 429 |
| F0.3-a | `F0/F0.3-a-redefinir-senha-ok.*` | Nova senha OK |
| F0.3-b | `F0/F0.3-b-token-invalido.*` | Token inválido/expirado/usado |
| F0.3-c | `F0/F0.3-c-senha-reutilizada.*` | 422 senha reutilizada |
| F0.5-a | `F0/F0.5-a-erro-5xx-incidentid.*` | 5xx → `/erro/500` |
| F0.5-b | `F0/F0.5-b-erro-4xx.*` | 4xx → `/erro/:codigo` |
| F0.6-a | `F0/F0.6-a-protocolo-encontrado.*` | Protocolo encontrado |
| F0.6-b | `F0/F0.6-b-hash-local.*` | Hash SHA-256 no browser |
| F0.6-c | `F0/F0.6-c-protocolo-404.*` | Protocolo 404 |
| F0.6-d | `F0/F0.6-d-protocolo-429.*` | Protocolo 429 |
| F0.7-a | `F0/F0.7-a-certificado-valido.*` | Certificado válido |
| F0.7-b | `F0/F0.7-b-certificado-revogado.*` | Certificado revogado |
| F0.7-c | `F0/F0.7-c-certificado-404.*` | Certificado 404 |
| F0.7-d | `F0/F0.7-d-assinatura-invalida.*` | Assinatura ED25519 falha |

## F1 (aluno) — pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F1.1-D01 | `F1/F1.1-D01-dashboard-cache-miss.*` | Dashboard cache MISS |
| F1.1-D02 | `F1/F1.1-D02-dashboard-cache-hit.*` | Dashboard cache HIT |
| F1.1-D03 | `F1/F1.1-D03-degradacao-graciosa.*` | Degradação graciosa |
| F1.1-D04 | `F1/F1.1-D04-pull-to-refresh.*` | Pull-to-refresh mobile |
| F1.2-D01 | `F1/F1.2-D01-primeiro-acesso-ok.*` | Primeiro acesso OK |
| F1.2-D02 | `F1/F1.2-D02-bloqueio-navegacao.*` | Guard mustChangePassword |
| F1.2-D03 | `F1/F1.2-D03-senha-temporaria.*` | 422 senha temporária |
| F1.3-D01 | `F1/F1.3-D01-editar-perfil.*` | Editar perfil |
| F1.3-D02 | `F1/F1.3-D02-upload-foto.*` | Upload foto |
| F1.4-D03 | `F1/F1.4-D03-trocar-senha.*` | Trocar senha |
| F1.4-D04 | `F1/F1.4-D04-senha-atual-incorreta.*` | 401 senha atual |
| F1.4-D05 | `F1/F1.4-D05-sessoes.*` | Sessões + encerrar |
| F1.5-D06 | `F1/F1.5-D06-preferencias-notif.*` | Preferências notif. |
| F1.6-D01 | `F1/F1.6-D01-listar-comunicacoes.*` | Listar comunicações |
| F1.6-D02 | `F1/F1.6-D02-marcar-lido.*` | Marcar como lida |
| F1.6-D03 | `F1/F1.6-D03-filtros-comunicacoes.*` | Filtros comunicações |
| F1.7-D01 | `F1/F1.7-D01-listar-solicitacoes.*` | Listar solicitações |
| F1.8-D02 | `F1/F1.8-D02-tipos-elegiveis.*` | Tipos elegíveis |
| F1.8-D03 | `F1/F1.8-D03-upload-anexo.*` | Upload anexo |
| F1.8-D04 | `F1/F1.8-D04-confirmar-wizard.*` | Confirmar wizard |
| F1.8-D05 | `F1/F1.8-D05-rascunho.*` | Salvar rascunho |
| F1.9-D06 | `F1/F1.9-D06-detalhe-solicitacao.*` | Detalhe + timeline |
| F1.9-D07 | `F1/F1.9-D07-gerar-protocolo.*` | Gerar protocolo PDF |
| F1.9-D08 | `F1/F1.9-D08-download-anexo.*` | Download anexo |

| F1.10-D01 | `F1/F1.10-D01-listar-formativas.*` | Listar formativas |
| F1.11-D02 | `F1/F1.11-D02-submeter-formativa.*` | Submeter formativa |
| F1.11-D03 | `F1/F1.11-D03-confirmar-formativa.*` | Confirmar pré-validada |
| F1.12-D04 | `F1/F1.12-D04-detalhe-formativa.*` | Detalhe aprovada |
| F1.13-D01 | `F1/F1.13-D01-listar-estagios.*` | Listar estágios |
| F1.14-D02 | `F1/F1.14-D02-detalhe-estagio.*` | Detalhe estágio |
| F1.14-D03 | `F1/F1.14-D03-enviar-documento-estagio.*` | Enviar documento |
| F1.15-D01 | `F1/F1.15-D01-listar-tccs.*` | Listar TCCs |
| F1.16-D02 | `F1/F1.16-D02-detalhe-tcc.*` | Detalhe TCC |
| F1.16-D03 | `F1/F1.16-D03-enviar-versao-final.*` | Versão final TCC |

| F1.17-D01 | `F1/F1.17-D01-listar-eventos.*` | Listar eventos |
| F1.17-D02 | `F1/F1.17-D02-sessao-presenca.*` | Sessão / modal presença |
| F1.18-D03 | `F1/F1.18-D03-presenca-single.*` | Presença SINGLE |
| F1.18-D04 | `F1/F1.18-D04-presenca-dual-entrada.*` | DUAL entrada |
| F1.18-D05 | `F1/F1.18-D05-presenca-fora-janela.*` | Fora da janela 403 |
| F1.19-D01 | `F1/F1.19-D01-listar-certificados.*` | Listar certificados |
| F1.19-D02 | `F1/F1.19-D02-download-certificado.*` | Download certificado |
| F1.19-D03 | `F1/F1.19-D03-emissao-automatica.*` | Emissão automática |
| F1.20-D01 | `F1/F1.20-D01-listar-atendimentos.*` | Listar atendimentos |
| F1.20-D02 | `F1/F1.20-D02-dar-ciencia.*` | Dar ciência |

## F2 (egresso) — pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F2.1-D01 | `F2/F2.1-D01-dashboard-egresso.*` | Dashboard read-only |
| F2.1-D02 | `F2/F2.1-D02-download-diploma.*` | Download diploma |
| F2.1-D03 | `F2/F2.1-D03-reemitir-certificado.*` | Reemitir certificado |
| F2.1-D04 | `F2/F2.1-D04-bloqueio-rota-aluno.*` | Bloqueio rota de aluno |

## F3 (professor) — pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F3.1-D01 | `F3/F3.1-D01-dashboard-professor.*` | Dashboard professor |
| F3.1-D02 | `F3/F3.1-D02-degradacao-graciosa.*` | Degradação graciosa |
| F3.2-D01 | `F3/F3.2-D01-criar-evento.*` | Criar evento |
| F3.2-D02 | `F3/F3.2-D02-evento-concluido.*` | Evento CONCLUIDO |
| F3.2-D03 | `F3/F3.2-D03-operacao-qr.*` | Operação QR |
| F3.2-D04 | `F3/F3.2-D04-operacao-pin-saida.*` | Operação PIN saída |
| F3.2-D05 | `F3/F3.2-D05-encerrar-evento.*` | Encerrar evento |
| F3.2-ERRO | `F3/F3.2-ERRO-sem-event-host.*` | 403 sem event.host |
| F3.3-D01 | `F3/F3.3-D01-fila-deliberacao.*` | Fila de deliberação |
| F3.4-D02 | `F3/F3.4-D02-deferir-solicitacao.*` | Deferir solicitação |
| F3.4-D03 | `F3/F3.4-D03-deeplink-sem-sessao.*` | Deep-link sem sessão |
| F3.4-D04 | `F3/F3.4-D04-encaminhar-professor.*` | Encaminhar professor |
| F3.4-ERRO-a | `F3/F3.4-ERRO-a-token-ja-utilizado.*` | Token já utilizado |
| F3.4-ERRO-b | `F3/F3.4-ERRO-b-guard-invalido.*` | Guard inválido 422 |
| F3.5-D01 | `F3/F3.5-D01-fila-caaf.*` | Fila CAAF |
| F3.5-D02 | `F3/F3.5-D02-aprovar-formativa.*` | Aprovar formativa |
| F3.5-D03 | `F3/F3.5-D03-aprovar-lote.*` | Aprovar em lote |
| F3.5-D04 | `F3/F3.5-D04-rejeitar-formativa.*` | Rejeitar formativa |
| F3.5-ERRO | `F3/F3.5-ERRO-sem-formative-review.*` | 403 sem formative.review |
| F3.6-D01 | `F3/F3.6-D01-listar-estagios.*` | Listar estágios |
| F3.6-D02 | `F3/F3.6-D02-parecer-documento.*` | Parecer em documento |
| F3.6-D03 | `F3/F3.6-D03-arquivar-estagio.*` | Arquivar estágio |
| F3.6-ERRO | `F3/F3.6-ERRO-sem-internship-review.*` | 403 sem internship.review |
| F3.7-D01 | `F3/F3.7-D01-listar-tccs.*` | Listar TCCs |
| F3.7-D02 | `F3/F3.7-D02-registrar-avaliacao.*` | Registrar avaliação |
| F3.7-D03 | `F3/F3.7-D03-download-tcc.*` | Download TCC |
| F3.7-ERRO | `F3/F3.7-ERRO-sem-tcc-supervise.*` | 403 sem tcc.supervise |
| F3.8-D01 | `F3/F3.8-D01-opcoes-audiencia.*` | Opções de audiência |
| F3.8-D02 | `F3/F3.8-D02-publicar-comunicado.*` | Publicar comunicado |
| F3.8-ERRO | `F3/F3.8-ERRO-publish-scope.*` | 403 / 422 scope |

## F4 (comissões) — pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F4.1a | `F4/F4.1a-carregar-pool-caaf.*` | Pool CAAF |
| F4.1b | `F4/F4.1b-self-assign.*` | Self-assign CAAF |
| F4.1c | `F4/F4.1c-atribuir-membro.*` | Atribuir membro CAAF |
| F4.1d | `F4/F4.1d-aprovacao-lote.*` | Aprovação em lote |
| F4.1e | `F4/F4.1e-erro-403.*` | 403 CAAF |
| F4.1f | `F4/F4.1f-erro-422-lote.*` | 422 lote misto |
| F4.2a | `F4/F4.2a-carregar-pool-coe.*` | Pool COE |
| F4.2b | `F4/F4.2b-self-assign.*` | Self-assign COE |
| F4.2c | `F4/F4.2c-atribuir-orientador.*` | Atribuir orientador |
| F4.2d | `F4/F4.2d-atribuicao-lote.*` | Atribuição em lote |
| F4.2e | `F4/F4.2e-erro-403.*` | 403 COE |

## F5 (secretaria) — pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F5.1-D01 | `F5/F5.1-D01-dashboard-secretaria.*` | Dashboard secretaria |
| F5.1-D02 | `F5/F5.1-D02-refresh-manual.*` | Refresh manual |
| F5.1-D03 | `F5/F5.1-D03-erro-403.*` | 403 dashboard |
| F5.2-D01 | `F5/F5.2-D01-fila-solicitacoes.*` | Fila de solicitações |
| F5.3-D02 | `F5/F5.3-D02-nova-interna.*` | Nova interna |
| F5.4-D03 | `F5/F5.4-D03-deliberar-sem-deeplink.*` | Deliberar sem deep-link |
| F5.2-D04 | `F5/F5.2-D04-atribuir-deliberador.*` | Atribuir deliberador |
| F5.5-D05 | `F5/F5.5-D05-atrasados-csv.*` | Atrasados + CSV |
| F5.3-ERRO | `F5/F5.3-ERRO-aluno-fora-escopo.*` | 403 aluno fora do escopo |
| F5.4-ERRO | `F5/F5.4-ERRO-senior-secretary.*` | 403 sem senior_secretary |
| F5.6-D01 | `F5/F5.6-D01-busca-alunos.*` | Busca de alunos |
| F5.6-D02 | `F5/F5.6-D02-cadastrar-aluno.*` | Cadastrar aluno |
| F5.6-D03 | `F5/F5.6-D03-editar-aluno.*` | Editar aluno |
| F5.6-D04 | `F5/F5.6-D04-reset-senha.*` | Reset senha admin |
| F5.6-D05 | `F5/F5.6-D05-matricula.*` | Matrícula |
| F5.6-ERRO-01 | `F5/F5.6-ERRO-01-conflito-grr.*` | 409 GRR/CPF |
| F5.6-ERRO-02 | `F5/F5.6-ERRO-02-sem-vagas.*` | 422 sem vagas |
| F5.6-ERRO-03 | `F5/F5.6-ERRO-03-sem-manage-students.*` | 403 manage_students |
| F5.7-D01 | `F5/F5.7-D01-criar-curso.*` | Criar curso |
| F5.8-D02 | `F5/F5.8-D02-criar-disciplina.*` | Criar disciplina |
| F5.8-D03 | `F5/F5.8-D03-desativar-disciplina.*` | Desativar disciplina |
| F5.8-D04-CSV | `F5/F5.8-D04-CSV-exportar-disciplinas.*` | CSV disciplinas |
| F5.9-D05 | `F5/F5.9-D05-criar-periodo.*` | Criar período letivo |
| F5.9-D06 | `F5/F5.9-D06-criar-evento-calendario.*` | Evento de calendário |
| F5.7-ERRO-01 | `F5/F5.7-ERRO-01-sigla-duplicada.*` | 409 sigla |
| F5.9-ERRO-02 | `F5/F5.9-ERRO-02-sobreposicao-periodo.*` | 422 sobreposição |
| F5.10-D01-EGRESSOS | `F5/F5.10-D01-EGRESSOS-listar.*` | Listar egressos |
| F5.11-D02-ELEGIVEIS | `F5/F5.11-D02-ELEGIVEIS-buscar.*` | Elegíveis colação |
| F5.11-D03 | `F5/F5.11-D03-confirmar-colacao.*` | Confirmar colação |
| F5.11-D04 | `F5/F5.11-D04-confirmar-entrega.*` | Entrega diploma |
| F5.11-ERRO-01 | `F5/F5.11-ERRO-01-sem-diploma-register.*` | 403 diploma.register |
| F5.12-D01 | `F5/F5.12-D01-listar-autorizacoes-imagem.*` | Autorizações imagem |
| F5.12-D02 | `F5/F5.12-D02-bulk-deliberate.*` | Bulk deliberate |
| F5.12-ERRO-01 | `F5/F5.12-ERRO-01-bulk-conflict.*` | 409 bulk conflict |
| F5.13-D01 | `F5/F5.13-D01-carregar-formulario-atendimento.*` | Form atendimento |
| F5.13-D02 | `F5/F5.13-D02-registrar-atendimento.*` | Registrar atendimento |
| F5.8-D01 | `F5/F5.8-D01-listar-eventos-secretaria.*` | Listar eventos (sec.) |
| F5.14-D02 | `F5/F5.14-D02-encerrar-evento.*` | Encerrar evento |
| F5.8-ERRO | `F5/F5.8-ERRO-evento-com-presenca.*` | 422 evento c/ presença |
| F5.9-D01 | `F5/F5.9-D01-baixar-modelo-importacao.*` | Modelo importação |
| F5.9-D02 | `F5/F5.9-D02-upload-validacao-importacao.*` | Upload + validação |
| F5.9-D03 | `F5/F5.9-D03-confirmar-importacao.*` | Confirmar importação |
| F5.9-ERRO-D04 | `F5/F5.9-ERRO-D04-parcial.*` | Importação parcial |
| F5.9-ERRO-403 | `F5/F5.9-ERRO-403-sem-import-run.*` | 403 import.run |
| F5.17-D01 | `F5/F5.17-D01-solicitar-exportacao.*` | Solicitar exportação |
| F5.10-D02 | `F5/F5.10-D02-worker-exportacao.*` | Worker exportação |
| F5.10-D03 | `F5/F5.10-D03-polling-download-exportacao.*` | Polling + download |
| F5.10-D04 | `F5/F5.10-D04-scheduler-expira-exportacao.*` | Expira exportação |
| F5.10-ERRO-403 | `F5/F5.10-ERRO-403-sem-export-run.*` | 403 export.run |
| F5.11-D01 | `F5/F5.11-D01-relatorios-secretaria.*` | Relatórios secretaria |
| F5.18-D02 | `F5/F5.18-D02-cache-refresh-relatorios.*` | Cache + refresh |
| F5.11-ERRO-403 | `F5/F5.11-ERRO-403-sem-report-view.*` | 403 report.view |
| F5.19-D01 | `F5/F5.19-D01-carregar-kanban.*` | Carregar kanban |
| F5.19-D02 | `F5/F5.19-D02-criar-tarefa.*` | Criar tarefa |
| F5.19-D03 | `F5/F5.19-D03-atualizar-estado.*` | Atualizar estado |
| F5.19-D04 | `F5/F5.19-D04-excluir-tarefa.*` | Excluir tarefa |
| F5.19-ERRO-01 | `F5/F5.19-ERRO-01-feature-flag.*` | Feature flag 404 |
| F5.19-ERRO-02 | `F5/F5.19-ERRO-02-sem-task-manage.*` | 403 task.manage |
| F5.19-ERRO-03 | `F5/F5.19-ERRO-03-titulo-invalido.*` | 422 título |
| F5.19-ERRO-04 | `F5/F5.19-ERRO-04-delete-concluida.*` | 409 DELETE |

## F6 (coordenação) — pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F6.1-D01 | `F6/F6.1-D01-carregar-config-curso.*` | Carregar config curso |
| F6.1-D02 | `F6/F6.1-D02-salvar-config-curso.*` | Salvar config |
| F6.1-ERRO | `F6/F6.1-ERRO-curso-alheio.*` | 403 curso alheio |
| F6.2-D01 | `F6/F6.2-D01-relatorios-coordenacao.*` | Relatórios coordenação |

## F7 (admin) — pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F7.1-D01 | `F7/F7.1-D01-listar-usuarios.*` | Listar usuários |
| F7.1-D02 | `F7/F7.1-D02-criar-usuario.*` | Criar usuário |
| F7.1-D03 | `F7/F7.1-D03-desativar-usuario.*` | Desativar usuário |
| F7.8-D04 | `F7/F7.8-D04-reset-senha-admin.*` | Reset senha admin |
| F7.1-ERRO-01 | `F7/F7.1-ERRO-01-sem-manage-all.*` | 403 manage_all |
| F7.2-D01 | `F7/F7.2-D01-listar-perfis.*` | Listar perfis |
| F7.2-D02 | `F7/F7.2-D02-criar-perfil.*` | Criar perfil |
| F7.2-D03 | `F7/F7.2-D03-listar-autoridades.*` | Autoridades + matriz |
| F7.2-D04 | `F7/F7.2-D04-salvar-matriz.*` | Salvar matriz |
| F7.2-D05 | `F7/F7.2-D05-atribuir-roles.*` | Atribuir roles |
| F7.2-ERRO-01 | `F7/F7.2-ERRO-01-perfil-em-uso.*` | 422 perfil em uso |
| F7.2-ERRO-02 | `F7/F7.2-ERRO-02-sem-iam-manage.*` | 403 IAM |
| F7.4-D01 | `F7/F7.4-D01-listar-tipos-solicitacao.*` | Listar RequestTypes |
| F7.4-D02 | `F7/F7.4-D02-criar-request-type.*` | Criar DRAFT |
| F7.4-D03 | `F7/F7.4-D03-salvar-rascunho.*` | Salvar rascunho |
| F7.4-D04 | `F7/F7.4-D04-publicar-versao.*` | Publicar versão |
| F7.4-ERRO-01 | `F7/F7.4-ERRO-01-sem-request-type-manage.*` | 403 request_type |
| F7.4-ERRO-02 | `F7/F7.4-ERRO-02-schema-invalido.*` | 422 schema |
| F7.4-ERRO-03 | `F7/F7.4-ERRO-03-tipo-com-historico.*` | 422 com histórico |
| F7.5-D01 | `F7/F7.5-D01-listar-templates.*` | Listar templates |
| F7.5-D02 | `F7/F7.5-D02-criar-template.*` | Criar template |
| F7.5-D03 | `F7/F7.5-D03-salvar-revisao.*` | Salvar revisão |
| F7.5-D04 | `F7/F7.5-D04-carregar-versao-anterior.*` | Versão anterior |
| F7.5-ERRO-01 | `F7/F7.5-ERRO-01-sem-manage-templates.*` | 403 templates |
| F7.6-D01 | `F7/F7.6-D01-listar-outbox.*` | Listar Outbox |
| F7.6-D02 | `F7/F7.6-D02-reentregar-outbox.*` | Reentregar Outbox |
| F7.6-D03 | `F7/F7.6-D03-listar-jobs-agendados.*` | Jobs agendados |
| F7.6-ERRO-01 | `F7/F7.6-ERRO-01-sem-system-observe.*` | 403 observe |
| F7.7-D01 | `F7/F7.7-D01-listar-audit-log.*` | Audit log |
| F7.7-ERRO-01 | `F7/F7.7-ERRO-01-sem-audit-read.*` | 403 audit.read |
| F7.9-D01 | `F7/F7.9-D01-saude-sistema.*` | Saúde do sistema |
| F7.9-ERRO-01 | `F7/F7.9-ERRO-01-sem-system-admin.*` | 403 system.admin |

## F8 (transversal) — pronto

| ID | Arquivo | Cenário |
|---|---|---|
| F8.1-D01 | `F8/F8.1-D01-busca-global.*` | Busca debounce + fan-out |
| F8.1-D02 | `F8/F8.1-D02-fanout-fgac-aluno.*` | Fan-out FGAC (aluno) |
| F8.1-D03 | `F8/F8.1-D03-sem-resultados.*` | EmptyState |
| F8.1-D04 | `F8/F8.1-D04-timeout-cliente.*` | Timeout 5s cliente |
| F8.2-D01 | `F8/F8.2-D01-carregar-faq.*` | FAQ por perfil |
| F8.2-D02 | `F8/F8.2-D02-abrir-ticket.*` | Abrir ticket |
| F8.2-D03 | `F8/F8.2-D03-rate-limit-ticket.*` | 429 rate limit ticket |

> **F0–F8:** lotes de sequência prontos (F0.4 contato não tem diagrama no doc).

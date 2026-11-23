# Módulo de Comunicação, Operação da Secretaria e Relatórios

## Descrição Geral

O **Módulo de Comunicação, Operação da Secretaria e Relatórios** reúne os painéis consolidados por BFF, o hub transacional de comunicados e templates versionados, além das rotinas volumosas de importação, exportação e análise de indicadores do **Secretaria Online 2 (SO2)**. As entregas de comunicação operam nos canais *in-app* e e-mail (SMTP) por meio do padrão Outbox (sem *push*/FCM na demonstração), enquanto exportações assíncronas utilizam armazenamento MinIO / S3 com *download* por URL pré-assinada e *dashboards* degradam graciosamente por bloco.

## Mapeamento de Atores

* **Aluno (`A2`):** Consome o *dashboard* contextual (`/bff/dashboard/aluno`) e o hub de comunicação (`communication.read`).

* **Professor (`A4`):** Consome o *dashboard* docente (`/bff/dashboard/professor`), lê comunicações e publica comunicados em Markdown para turmas ou cursos (`communication.publish_class`).

* **Secretaria (`A7`):** Consome o *dashboard* operacional (`/bff/dashboard/secretary`), utiliza o hub de comunicação, executa importações em lote (`import.run`), solicita exportações assíncronas (`export.run`) e consulta estatísticas operacionais (`report.view_secretary`).

* **Coordenador (`A8`):** Consome o *dashboard* contextual e o hub de comunicação conforme suas *capabilities* e consulta relatórios analíticos do curso coordenado (`report.view_coordinator`).

* **Administrador da Plataforma (`A9`):** Mantém e versiona os templates de comunicação utilizados pelo *dispatcher* de e-mail (`communication.manage_templates`).

* **Outbox / Sistema de Notificação (`S2`):** Ator secundário que persiste entregas *in-app* e eventos em `outbox_event`, renderiza templates na revisão `CURRENT` e despacha e-mails assíncronos via SMTP.

* **Armazenamento de Objetos — MinIO / S3 (`S5`):** Ator secundário que armazena os arquivos gerados pelos *jobs* de exportação assíncrona, entregues via URL pré-assinada (TTL 15 min).

## Especificação dos Casos de Uso

### `UC-DASH-01` — Visualizar dashboard contextual (BFF)

Permite ao usuário autenticado carregar em uma única chamada HTTP (`GET /bff/dashboard/{aluno|professor|secretary}`, direcionada pelo `rel` `painel` de `GET /auth/me`) KPIs, pendências, alertas de SLA, agenda e atalhos `_links`, com degradação graciosa caso algum submódulo falhe.

### `UC-COM-01` — Consumir hub de comunicação

Permite ao usuário com `communication.read` listar comunicados institucionais, avisos de turma e itens de ação (`GET /communications`) com filtros por aba, tipo e status de leitura, além de marcar itens como lidos (`POST /communications/{id}/read`).

### `UC-COM-02` — Publicar comunicado (turma ou curso)

Permite ao **Professor** (`communication.publish_class`) selecionar audiências elegíveis (`GET /communications/audiencias`), redigir comunicados em Markdown definindo prioridade e expiração e publicá-los (`POST /communications`, tela `/comunicacao/publicar`), enfileirando entregas *in-app* e e-mail via Outbox na mesma transação.

### `UC-COM-03` — Administrar templates de comunicação

Permite ao **Administrador** (`communication.manage_templates`) manter templates de e-mail em Markdown (`/admin/templates-comunicacao`) com *placeholders* dinâmicos, *preview* de variáveis e versionamento imutável por revisão (`POST /{id}/revisoes`, revisão `CURRENT`), sem templates de *push*/FCM.

### `UC-ADM-06` — Importar planilhas em lote

Permite à **Secretaria** (`import.run`) baixar modelos CSV (`GET /importacoes/modelos/{kind}`), enviar planilhas CSV/XLSX de até $20\text{ MB}$ (`POST /importacoes/{kind}`), inspecionar a validação assíncrona linha a linha (`GET /importacoes/{id}`) e confirmar a gravação transacional (`POST /importacoes/{id}/confirmar`) somente quando `errorCount = 0`.

### `UC-ADM-07` — Solicitar exportações assíncronas

Permite à **Secretaria** (`export.run`) disparar exportações volumosas (`POST /exportacoes/{kind}`, retornando `HTTP 202` com status `PROCESSANDO`), acompanhar o histórico de *jobs* (`GET /exportacoes`) e baixar o arquivo gerado no MinIO/S3 via URL pré-assinada (`GET /exportacoes/{id}/download`), com notificação por e-mail via Outbox.

### `UC-ADM-08` — Visualizar estatísticas da secretaria

Permite à **Secretaria** (`report.view_secretary`) consultar painéis quantitativos e gráficos operacionais (`GET /reports/secretary`, tela `/secretaria/estatisticas`) filtrados por período e restritos aos cursos vinculados ao operador.

### `UC-ADM-09` — Visualizar relatórios da coordenação

Permite ao **Coordenador** (`report.view_coordinator`) consultar *dashboards* analíticos do curso sob sua gestão (`GET /reports/coordinator`, tela `/coordenacao/relatorios`), exibindo KPIs, séries históricas de formativas, pendências e atalhos HATEOAS.
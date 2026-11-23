# Módulo de Administração da Plataforma e Transversais

## Descrição Geral

O **Módulo de Administração da Plataforma e Transversais** concentra a governança global de acessos (`FGAC`), a configuração dinâmica do motor de solicitações, a observabilidade do Outbox e da trilha de auditoria, além das ferramentas transversais de busca global e suporte/FAQ do **Secretaria Online 2 (SO2)**. A administração opera sem alterações de código para perfis, *authorities* e fluxos (`tipo_solicitacao`), enquanto busca (`pg_trgm`) e suporte (reutilizando o motor de solicitações com `SUPORTE_TECNICO`) atendem todos os usuários autenticados (a tela `F7.9`/Grafana e o *kanban* `F5.19` ficam fora da demonstração).

## Mapeamento de Atores

* **Administrador da Plataforma (`A9`):** Gerencia globalmente usuários, perfis, matriz `FGAC` de *authorities*, editor de tipos de solicitação, monitoramento/reentrega do Outbox e consulta à trilha de auditoria.

* **Usuário Autenticado (`A2`, `A4`, `A7`, `A8`, `A9`):** Generalização dos perfis autenticados que utilizam a busca global (`Ctrl+K`) e o portal de suporte/FAQ com abertura de *tickets*.

* **Serviço de Autenticação / IAM (`S1`):** Ator secundário que mantém contas, vínculos de perfis/*authorities*, geração de links de *reset* administrativo (JWT 24 h) e gravação *append-only* em `audit_log`.

* **Motor de Workflow (`S3`):** Ator secundário que valida e publica versões imutáveis de `tipo_solicitacao` (`form_schema` + `workflow_json`) e processa a abertura de *tickets* de suporte (`SUPORTE_TECNICO`, protocolo `PROT-…`).

* **Outbox / Sistema de Notificação (`S2`):** Ator secundário que gerencia a fila `outbox_event` (`PENDING`/`SENT`/`FAILED`/`DEAD`), despacha links de *reset* administrativo e executa reentregas manuais.

## Especificação dos Casos de Uso

### `UC-ADM-01` — CRUD usuários global e reset de senha

Permite ao **Administrador** (`user.manage_all`, `user.reset_password`) listar, criar, editar e desativar usuários (`/admin/usuarios`, `POST /{id}/desativar`) e disparar link de redefinição de senha JWT de uso único com validade de 24 h (`POST /{id}/reset-senha`), sem exibir ou definir senhas em texto claro.

### `UC-ADM-02` — Configurar perfis e matriz FGAC de autoridades

Permite ao **Administrador** (`iam.manage_roles`, `iam.manage_authorities`) manter perfis (`/admin/perfis`, protegendo perfis de sistema e em uso), vinculá-los a usuários (`GET/PUT /admin/usuarios/{id}/perfis`) e editar descrições e a matriz perfil $\times$ *authority* (`/admin/autoridades`, `PUT /admin/autoridades/matriz`) em transação única, sem alteração de código.

### `UC-ADM-03` — Configurar tipos de solicitação (editor de workflow)

Permite ao **Administrador** (`request_type.manage`) criar e editar `form_schema` (JSON Schema) e `workflow_json` em rascunho (`DRAFT`) em `/admin/tipos-solicitacao`, validar o JSON antes da transação, excluir rascunhos sem histórico e publicar versões imutáveis (`POST /{id}/publicar`, status `PUBLISHED`).

### `UC-ADM-04` — Monitorar Outbox e reentregar eventos

Permite ao **Administrador** (`system.observe`) inspecionar eventos da tabela `outbox_event` (`GET /admin/outbox`, tela `/admin/jobs`) filtrando por status e tipo, e disparar reentrega manual de eventos com falha (`POST /admin/outbox/{id}/reentrega`, retornando `409` se já `SENT`).

### `UC-ADM-05` — Pesquisar trilha de auditoria (`audit_log`)

Permite ao **Administrador** (`audit.read`) realizar consultas imutáveis na trilha de auditoria (`GET /audit-log`, tela `/admin/audit-log`, 50 registros por página) filtrando por ator, ação e período, com visualização de *diff* JSON (`payloadAntes`/`payloadDepois`) e sem *endpoints* de edição ou exclusão.

### `UC-ADM-10` — Realizar busca global (`Ctrl+K`)

Permite a qualquer **Usuário Autenticado** acionar a paleta de comandos (`GET /search?q=`) para localizar alunos, solicitações, eventos e usuários via `pg_trgm` no PostgreSQL, com resultados recortados estritamente pelas *capabilities* do JWT.

### `UC-ADM-11` — Consultar FAQ e abrir ticket de suporte (`SUPORTE_TECNICO`)

Permite a qualquer **Usuário Autenticado** consultar a lista única de perguntas frequentes (`GET /suporte/faq`, sem filtro por papel) na tela `/suporte` e, caso não encontre resposta, estender para `UC-SOL-01` abrindo *ticket* em `POST /requests` com `tipoCodigo=SUPORTE_TECNICO` e protocolo `PROT-…` (limite de 3 *tickets*/hora).
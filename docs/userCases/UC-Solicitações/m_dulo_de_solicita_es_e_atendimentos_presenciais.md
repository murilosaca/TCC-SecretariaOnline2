# Módulo de Solicitações e Atendimentos Presenciais

## Descrição Geral

O **Módulo de Solicitações e Atendimentos Presenciais** centraliza a abertura, tramitação e deliberação de requerimentos acadêmicos, além do registro auditável de atendimentos presenciais da secretaria. Em vez de telas estáticas por requerimento, utiliza um motor genérico de *workflow* orientado a metadados (`tipo_solicitacao`, com `form_schema` em JSON Schema e `workflow_json`), controle de SLA, ações expostas via `_links` HATEOAS, anexos em armazenamento MinIO / S3 e notificações assíncronas via Outbox.

## Mapeamento de Atores

* **Aluno (`A2`):** Abre solicitações, acompanha tramitações, reenvia ajustes, gera protocolos PDF, consulta atendimentos presenciais e registra ciência (*ack*).

* **Professor (`A4`):** Acompanha e delibera solicitações atribuídas à sua fila (inclusive via *deep-link* JWT de uso único com validade de 72 h).

* **Secretaria (`A7`):** Opera a fila central e atrasados no escopo de seus cursos (`CursoEscopoPort`), abre solicitações internas em nome de alunos, delibera demandas (sem *deep-link*), revisa autorizações de imagem em lote e registra atendimentos presenciais.

* **Motor de Workflow (`S3`):** Ator secundário que interpreta `form_schema` e `workflow_json`, valida transições (`DEFER`, `INDEFER`, `REQUEST_ADJUST`, `RESUBMIT`, `CLOSE` — sem `FORWARD`), calcula SLAs e monta os `_links` disponíveis.

* **Outbox / Sistema de Notificação (`S2`):** Ator secundário que enfileira eventos na mesma transação das mutações para entrega *in-app* e disparo assíncrono de e-mails (incluindo *deep-links* ao professor e avisos de atendimento).

* **Armazenamento de Objetos — MinIO / S3 (`S5`):** Ator secundário responsável por armazenar anexos de solicitações e de atendimentos presenciais, servidos via URL pré-assinada.

## Especificação dos Casos de Uso

### `UC-SOL-01` — Abrir solicitação (wizard dinâmico)

Permite ao **Aluno** abrir qualquer tipo de solicitação elegível em um *wizard* de 3 passos renderizado a partir de `form_schema` (`GET /request-types/{codigo}`), com envio opcional de anexos ao MinIO/S3. Ao confirmar (`POST /requests`), gera protocolo `PROT-…`, registra auditoria e enfileira notificação via Outbox.

### `UC-SOL-02` — Abrir solicitação interna em nome do aluno

Permite à **Secretaria** (`request.internal_open`) reutilizar o *wizard* dinâmico para abrir solicitações em nome de alunos dos cursos sob sua competência (`onBehalfOf`), mantendo as mesmas validações de esquema, anexos e geração de protocolo.

### `UC-SOL-03` — Acompanhar solicitação e reenviar ajustes

Permite ao **Aluno**, **Professor** e **Secretaria** consultar detalhes, *timeline* imutável de eventos, indicadores de SLA e ações disponíveis via `_links`. Quando a solicitação entra em `EM_AJUSTE`, estende o fluxo permitindo ao **Aluno** editar os dados (`PATCH /requests/{id}`) e reenviar para análise (`POST /requests/{id}/reenviar`).

### `UC-SOL-04` — Deliberar solicitação

Permite ao **Professor** e à **Secretaria** (`request.deliberate`) deferir, indeferir ou solicitar ajustes com parecer fundamentado (`POST /requests/{id}/transitions` ou em lote via `PATCH /requests/deliberacao-lote`), sem transição `FORWARD`. O **Professor** também pode acessar a deliberação por *deep-link* JWT de uso único (72 h, `JTI` em *blacklist*); a **Secretaria** delibera apenas pela fila.

### `UC-SOL-05` — Gerar protocolo PDF

Permite ao **Aluno** gerar e baixar o comprovante PDF da solicitação (`POST /requests/{id}/protocolo`) contendo QR Code e hash `SHA-256`, habilitando a verificação pública de autenticidade (`UC-CRT-02`).

### `UC-SOL-06` — Triar fila central e monitorar atrasados

Permite à **Secretaria** (`request.view_curso`, `request.triage`) consultar e filtrar a fila central dos cursos vinculados, atribuir deliberadores em massa (`PATCH /requests/bulk`), monitorar demandas com SLA vencido (`slaBreached`) e exportar CSV (até os 100 primeiros registros do filtro).

### `UC-SOL-07` — Revisar autorização de imagem em lote

Permite à **Secretaria** (`image_authorization.review`) consultar a fila filtrada de solicitações `AUTORIZACAO_IMAGEM` com miniaturas e executar aprovação ou rejeição transacional em lote (`PATCH /requests/bulk-deliberate`).

### `UC-ATD-01` — Registrar atendimento presencial

Permite à **Secretaria** (`service_record.create`) criar um registro imutável de atendimento presencial (`POST /atendimentos`) informando aluno, categoria, resposta e anexo PDF opcional ($\le 10\text{ MB}$ no MinIO/S3), notificando o aluno via Outbox.

### `UC-ATD-02` — Consultar atendimentos e dar ciência (ack)

Permite ao **Aluno** (`service_record.view_own`) visualizar atendimentos registrados pela secretaria, baixar anexos e confirmar ciência dos registros pendentes (`POST /atendimentos/{id}/acknowledge`), gravando confirmação auditável.
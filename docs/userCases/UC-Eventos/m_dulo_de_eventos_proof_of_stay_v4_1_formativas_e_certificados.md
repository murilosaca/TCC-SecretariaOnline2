# Módulo de Eventos (Proof of Stay v4.1), Formativas e Certificados

## Descrição Geral

O **Módulo de Eventos, Formativas e Certificados** integra o circuito principal de validação de atividades complementares do **Secretaria Online 2 (SO2)**. A partir do protocolo determinístico **Proof of Stay v4.1** (modos `QR_SINGLE`, `QR_DUAL`, `SECRET_SINGLE` e `SECRET_DUAL`, sem *geofence*/GPS e sem chamada de aula regular), o sistema vincula a presença validada em eventos à criação automática de atividades formativas, que passam pela confirmação do aluno e deliberação da CAAF, culminando na emissão exclusiva de certificados PDF com hash `SHA-256` e assinatura digital `ED25519`.

## Mapeamento de Atores

* **Aluno (`A2`):** Consulta eventos elegíveis, confirma presença (PIN ou QR + `deviceUuid`), confirma/cancela formativas originadas de eventos, submete comprovantes manuais, acompanha pareceres e baixa certificados próprios.

* **Professor (`A4`):** Cadastra e gerencia eventos formativos sob sua organização (`event.manage`), opera chamadas ao vivo (`event.host`) e encerra eventos.

* **Secretaria (`A7`):** Cadastra e gerencia eventos institucionais no escopo dos cursos vinculados (`event.manage`, `event.view_curso`), opera chamadas ao vivo (`event.host`) e encerra eventos.

* **Membro CAAF (`A5`):** Avalia formativas individualmente (`formative.review`) e opera o *pool* coletivo da comissão (`/comissoes/caaf`) para atribuição interna e aprovação em lote de itens com presença validada.

* **Emissor de Certificados (`S4`):** Ator secundário que gera PDFs canônicos com hash `SHA-256`, assinatura `ED25519` e QR de verificação pública exclusivamente na aprovação da CAAF (nunca via *upload* externo).

* **Outbox / Sistema de Notificação (`S2`):** Ator secundário que enfileira eventos de encerramento de evento, pareceres da CAAF e emissão de certificados para entrega assíncrona.

* **Armazenamento de Objetos — MinIO / S3 (`S5`):** Ator secundário que armazena comprovantes de formativas e PDFs de certificados (`storage_key`), servidos via URL pré-assinada (TTL 15 min).

## Especificação dos Casos de Uso

### `UC-PRE-01` — Cadastrar e gerenciar eventos formativos

Permite ao **Professor** e à **Secretaria** (`event.manage`) criar, editar e excluir eventos (`POST/PATCH/DELETE /events`), definindo modo de presença (`QR | SECRET × SINGLE | DUAL`), carga horária e curso-alvo, respeitando imutabilidade para eventos `CONCLUIDO` ou com presenças registradas.

### `UC-PRE-02` — Operar chamadas ao vivo (PIN / QR)

Permite ao anfitrião (`event.host`) abrir janelas de entrada e saída (padrão de 15 min), exibir PIN ou QR renovável (TTL de 5 min em `POST /events/{id}/attendance/qr/renew`) na *host-session* e acompanhar contadores de presentes e inelegíveis em tempo real.

### `UC-PRE-03` — Confirmar presença (Proof of Stay v4.1)

Permite ao **Aluno** (`attendance.view_open`, `attendance.check_in`) confirmar entrada — e saída nos modos `DUAL`, via `_links` — informando PIN ou token QR com `deviceUuid` (`POST /events/{id}/attendance/confirm`). Quando a presença atinge `COMPLETA`, cria-se automaticamente uma formativa `PENDENTE_CONFIRMACAO` na mesma transação.

### `UC-PRE-04` — Encerrar evento

Permite ao anfitrião (`event.host`) encerrar o evento em `POST /events/{id}/encerrar`, transitando para `CONCLUIDO`, limpando o segredo da sessão e registrando auditoria/Outbox — sem emitir PDF, pois o certificado oficial nasce somente na aprovação da CAAF.

### `UC-FOR-01` — Submeter ou confirmar atividade formativa

Permite ao **Aluno** enviar comprovante manual em `POST /formativas` (`formative.submit`, nascendo em `AGUARDANDO_CAAF` com origem `COMPROVANTE`) ou confirmar/cancelar uma formativa pré-criada por presença validada (`POST /formativas/{id}/confirmar` ou `/cancelar`, `formative.confirm_own`).

### `UC-FOR-02` — Acompanhar formativas

Permite ao **Aluno** (`formative.view_own`) listar e detalhar suas atividades formativas (`GET /formativas`, `GET /formativas/{id}`), acompanhando status, comprovante anexado, horas validadas e parecer da CAAF.

### `UC-FOR-03` — Revisar / aprovar formativa (individual)

Permite ao **Membro CAAF** (`formative.review`) aprovar ou indeferir submissões com parecer fundamentado (`POST /formativas/{id}/aprovar` ou `/indeferir`). A aprovação valida as horas e aciona o **Emissor de Certificados (`S4`)** na mesma transação.

### `UC-FOR-04` — Gerenciar pool CAAF (atribuir e aprovar em lote)

Permite ao **Membro CAAF** (`GET /comissoes/caaf`) visualizar o *pool* do curso, realizar *self-assign* ou delegação a colegas (`POST /comissoes/caaf/atribuicoes`) e aprovar em lote (`POST /comissoes/caaf/lote`) exclusivamente atividades com origem `PRESENCA_VALIDADA`, emitindo os certificados correspondentes.

### `UC-CRT-01` — Listar e baixar certificados próprios

Permite ao **Aluno** ativo (`certificate.view_own`) consultar seus certificados emitidos pelo sistema (`GET /certificates`) e baixar o PDF assinado com QR público via URL pré-assinada no MinIO/S3 (egressos utilizam o portal `/egressos/me`).
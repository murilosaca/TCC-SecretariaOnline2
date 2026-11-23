# Módulo de Cadastro Acadêmico, Coordenação e Egresso/Diploma

## Descrição Geral

O **Módulo de Cadastro Acadêmico, Coordenação e Egresso/Diploma** administra a estrutura curricular e o ciclo de vida completo do estudante no **Secretaria Online 2 (SO2)**, desde a matrícula até a colação de grau. A Secretaria mantém cursos, disciplinas, alunos, matrículas, períodos letivos globais e diplomas no escopo de seus cursos (`CursoEscopoPort`), a Coordenação define parâmetros curriculares próprios sem herdar o CRUD da secretaria, e o Egresso acessa um portal estritamente *read-only* para consulta de histórico, *download* de diploma e reemissão de certificados.

## Mapeamento de Atores

* **Secretaria (`A7`):** Opera os cadastros de alunos, matrículas, cursos, disciplinas e períodos letivos globais, conduz a colação de grau em lote, registra a entrega de diplomas e consulta/exporta a listagem de egressos.

* **Coordenador (`A8`):** Configura exclusivamente os parâmetros curriculares dos cursos em que está designado como coordenador (`idCoordenador` + `course.config`).

* **Egresso (`A3`):** Ex-aluno com acesso *read-only* ao portal `/egresso/inicio` (`alumni.view_own`) para consultar histórico resumido, baixar diploma e reemitir certificados.

* **Serviço de Autenticação / IAM (`S1`):** Ator secundário que provê o seletor de usuários (`GET /iam/usuarios`), executa o *reset* de senha de alunos e suporta a transição de acesso `ALUNO` $\rightarrow$ `EGRESSO`.

* **Outbox / Sistema de Notificação (`S2`):** Ator secundário que enfileira notificações de *reset* de senha, colação registrada e disponibilidade de diploma para disparo assíncrono.

* **Armazenamento de Objetos — MinIO / S3 (`S5`):** Ator secundário que armazena os PDFs de diplomas e certificados já emitidos, entregues via URL pré-assinada.

## Especificação dos Casos de Uso

### `UC-CAD-01` — CRUD alunos, reset de senha e matrículas

Permite à **Secretaria** (`user.manage_students`) buscar, cadastrar, editar e remover alunos no escopo do curso (`/academico/alunos`), disparar *reset* de senha (`POST /{id}/reset-senha`) e gerenciar matrículas em disciplinas (`POST /{id}/matriculas`), mantendo unicidade global de GRR e e-mail.

### `UC-CAD-02` — CRUD cursos e vínculo de secretários/coordenador

Permite à **Secretaria** (`course.manage`) manter cursos (`/academico/cursos`), vinculando coordenador e secretários via `GET /iam/usuarios`, com proteção *anti-lockout* na edição, preservação de `idCoordenador` quando omitido e bloqueio de exclusão (`409`) se houver alunos ou disciplinas vinculados.

### `UC-CAD-03` — CRUD disciplinas

Permite à **Secretaria** (`subject.manage`) cadastrar, editar, ativar/inativar e remover disciplinas vinculadas aos cursos de sua competência (`/academico/disciplinas`), garantindo código único por curso e registro de carga horária.

### `UC-CAD-04` — CRUD períodos letivos (calendário global)

Permite à **Secretaria** (`calendar.manage`) manter períodos letivos globais (`/academico/periodos`) e consultar o período vigente (`GET /academico/periodos/vigente`), impedindo sobreposição de datas e alertando ausência de período ativo (eventos semânticos de calendário ficam fora da demonstração).

### `UC-CAD-05` — Configurar parâmetros curriculares do curso

Permite ao **Coordenador** designado (`course.config` e `idCoordenador = usuarioId`) ajustar horas formativas mínimas, duração do calendário (15 ou 18 semanas), regras de banca de TCC e texto de regimento (`GET/PATCH /coordenacao/cursos/{id}/config`), com auditoria e sem retroagir elegibilidade já conquistada.

### `UC-EGR-01` — Dashboard read-only, diploma e reemissão de certificados

Permite ao **Egresso** (`alumni.view_own`) acessar o painel `/egresso/inicio` (`GET /egressos/me`) com histórico resumido e dados de colação, baixar o diploma (`GET /egressos/me/diploma`) e reemitir certificados já gravados (`GET /egressos/me/certificados/{id}/reemissao`, preservando o mesmo `hash_sha256` e assinatura `ED25519`), com rotas de aluno bloqueadas.

### `UC-EGR-02` — Registrar colação em lote e entrega de diploma

Permite à **Secretaria** (`diploma.register`) listar alunos elegíveis (`GET /diplomas/elegiveis`), executar o *wizard* de colação em lote (`POST /diplomas`) promovendo `ALUNO` $\rightarrow$ `EGRESSO`, anexar o PDF do diploma ao MinIO/S3 (`POST /diplomas/{id}/pdf`), registrar a entrega física (`PATCH /diplomas/{id}/confirm-delivery`) e notificar o egresso via Outbox.

### `UC-EGR-03` — Listar e exportar egressos

Permite à **Secretaria** (`alumni.list`) consultar egressos paginados de 20 em 20 (`GET /egressos`, tela `/secretaria/egressos`) com filtros por curso, ano e situação do diploma — sem criação manual — e exportar CSV síncrono (até os 100 primeiros registros do filtro).
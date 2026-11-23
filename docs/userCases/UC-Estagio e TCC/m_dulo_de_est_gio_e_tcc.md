# Módulo de Estágio e TCC

## Descrição Geral

O **Módulo de Estágio e TCC** gerencia o acompanhamento documental e avaliativo de estágios curriculares e Trabalhos de Conclusão de Curso no **Secretaria Online 2 (SO2)**. A Secretaria realiza o cadastro formal dos vínculos e bancas no escopo de seus cursos, a COE organiza o *pool* de atribuição de orientadores de estágio, os alunos enviam documentos e versões finais em PDF para armazenamento MinIO / S3, e docentes emitem pareceres e avaliações estritamente individuais com rastreabilidade em auditoria.

## Mapeamento de Atores

* **Aluno (`A2`):** Consulta seus estágios e TCCs registrados pela secretaria, acompanha banca/datas e envia documentos obrigatórios de estágio e a versão final do TCC em PDF.

* **Professor (`A4`):** Atua como orientador de estágio (`internship.review`) emitindo pareceres individuais por documento e encerrando processos concluídos, e como orientador ou membro de banca de TCC (`tcc.review`) registrando nota e parecer individuais.

* **Membro COE (`A6`):** Docente integrante da Comissão de Estágios (`internship.review` + `coe_membro`) que gerencia o *pool* coletivo (`/comissoes/coe`) exclusivamente para atribuição de orientadores, além de emitir pareceres individuais (sem aprovação em lote).

* **Secretaria (`A7`):** Cadastra e atualiza estágios (`internship.manage`) e TCCs com composição de banca (`tcc.manage`) no escopo dos cursos vinculados.

* **Armazenamento de Objetos — MinIO / S3 (`S5`):** Ator secundário que armazena os PDFs de estágio e a versão final do TCC (`storage_key`), servidos via URL pré-assinada (TTL 15 min).

* **Outbox / Sistema de Notificação (`S2`):** Ator secundário que enfileira eventos de atribuição, entrega documental, pareceres e avaliações para notificação assíncrona.

## Especificação dos Casos de Uso

### `UC-EST-01` — Acompanhar estágio e enviar documentos

Permite ao **Aluno** (`internship.view_own`) consultar estágios cadastrados pela secretaria (`GET /estagios`, `GET /estagios/{id}`), acompanhar pareceres e enviar documentos exigidos (TCE e relatórios) em PDF ao MinIO/S3 via `POST /estagios/{id}/documentos`.

### `UC-EST-02` — Emitir parecer individual e encerrar estágio

Permite ao **Professor** orientador ou **Membro COE** (`internship.review`) baixar os PDFs enviados, registrar parecer individual por documento (`POST /estagios/{id}/documentos/{documentoId}/parecer`) e arquivar/encerrar o estágio concluído (`POST /estagios/{id}/encerrar`), sem opção de aprovação em lote.

### `UC-EST-03` — Gerenciar pool COE (atribuir orientador)

Permite ao **Membro COE** (`GET /comissoes/coe`) visualizar estágios sem orientador no escopo da comissão e realizar *self-assign* ou alocação a colegas com visão de carga (`POST /comissoes/coe/atribuicoes`), sem decisão em lote sobre pareceres.

### `UC-EST-04` — Cadastrar e gerenciar estágios

Permite à **Secretaria** (`internship.manage`) listar, registrar e editar estágios no escopo dos cursos vinculados (`GET /estagios?escopo=cursos`, `POST/PUT /estagios`, tela `/secretaria/estagios`), podendo deixar o orientador em aberto para encaminhamento ao *pool* da COE.

### `UC-TCC-01` — Acompanhar TCC e enviar versão final

Permite ao **Aluno** (`tcc.view_own`) consultar status, banca examinadora e datas-chave do TCC (`GET /tccs`, `GET /tccs/{id}`) e fazer *upload* da versão final em PDF (`POST /tccs/{id}/versao-final`) para o MinIO/S3 quando autorizado via `_links`.

### `UC-TCC-02` — Avaliar TCC (orientador / banca)

Permite ao **Professor** orientador ou membro de banca (`tcc.review`) consultar TCCs sob sua responsabilidade, baixar o arquivo final (`GET /tccs/{id}/arquivo`) e registrar nota e parecer individuais (`POST /tccs/{id}/avaliacoes`), sem avaliação em lote e sem emissão de certificado de conclusão na demonstração.

### `UC-TCC-03` — Cadastrar e gerenciar TCCs e banca

Permite à **Secretaria** (`tcc.manage`) listar, cadastrar e atualizar TCCs e a composição de bancas (`GET /tccs?escopo=cursos`, `POST/PUT /tccs`, tela `/secretaria/tccs`) utilizando o seletor de usuários (`GET /iam/usuarios`), respeitando o limite de um TCC `ATIVO` por aluno.
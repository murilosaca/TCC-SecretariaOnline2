# Módulo de Acesso Público e Identidade (IAM)

## Descrição Geral

O **Módulo de Acesso Público e Identidade (IAM)** é a camada de entrada e segurança do **Secretaria Online 2 (SO2)**, responsável pelo acesso público, autenticação *stateless* e autogestão de conta. Adota senhas em `Argon2id`, tokens `JWT RS256` (15 min) com *refresh token* opaco rotativo (7 dias), autorização granular por *capabilities* (`FGAC`) e navegação via `_links` HATEOAS, além de expor rotas institucionais e verificação pública de protocolos e certificados.

---

## Mapeamento de Atores

* **Visitante (`A1`):** Usuário não autenticado que acessa login, recuperação/redefinição de senha, contato institucional, páginas de erro e verificação pública.
* **Verificador Externo (`S6`):** Terceiro sem credenciais que valida publicamente protocolos e certificados emitidos pelo SO2.
* **Usuário Autenticado (`A2`, `A4`, `A7`, `A8`, `A9`):** Generalização dos perfis com permissão de autogestão de perfil, senha, sessões e notificações.
* **Aluno (`A2`):** Especialização destacada no fluxo obrigatório de primeiro acesso (`senha_alterada = false`).
* **Serviço de Autenticação / IAM (`S1`):** Ator secundário que valida credenciais, emite/rotaciona tokens, aplica *rate limit*, *blacklist* de `JTI` e auditoria.
* **Outbox / Serviço de E-mail — SMTP (`S2`):** Ator secundário que enfileira e envia e-mails assíncronos (como links de recuperação de senha).
* **Armazenamento de Objetos — MinIO / S3 (`S5`):** Ator secundário que armazena a foto de perfil do usuário com acesso via URL pré-assinada.

---

## Especificação dos Casos de Uso

### `UC-AUT-01` — Autenticar-se
Valida e-mail (`@ufpr.br` ou pessoal) ou GRR e senha via `Argon2id` com defesa anti-enumeração, *rate limit* (5 tentativas/min) e bloqueio temporário (15 min após 10 falhas). Emite `JWT RS256` e *refresh token* rotativo, registrando o evento em auditoria e redirecionando conforme `_links`.

### `UC-AUT-02` — Recuperar senha
Permite ao **Visitante** solicitar redefinição de senha informando o e-mail, retornando sempre `HTTP 202` (anti-enumeração, limite de 3 tentativas/hora). Se cadastrado, gera JWT de uso único (24 h) e enfileira o envio na tabela `outbox_event` para despacho via SMTP.

### `UC-AUT-03` — Redefinir senha via token
Permite definir nova senha forte a partir do link de uso único recebido por e-mail. Valida o token e o `JTI`, persiste o novo hash em `Argon2id`, inclui o `JTI` na *blacklist* e encerra todas as sessões anteriores do usuário.

### `UC-AUT-04` — Completar primeiro acesso (senha + LGPD)
Estende `UC-AUT-01` quando `mustChangePassword = true` (`senha_alterada = false`). Bloqueia o acesso às demais rotas até que o usuário cadastre nova senha forte e registre o aceite da LGPD.

### `UC-AUT-05` — Gerenciar perfil, senha e sessões ativas
Permite ao **Usuário Autenticado** editar dados pessoais não acadêmicos (nome social, telefone, e-mail pessoal e foto no MinIO/S3), alterar a senha mediante confirmação da atual e encerrar sessões ativas em outros dispositivos.

### `UC-AUT-06` — Configurar preferências de notificação
Permite configurar canais de notificação (*in-app* e e-mail) por prioridade, horário de "não perturbar" (`DND`) e modo de agrupamento (*digest*).

### `UC-PUB-01` — Consultar contato institucional e páginas de erro
Exibe publicamente endereço, telefones, e-mail e horário de atendimento da secretaria, além de páginas amigáveis para erros HTTP (`401`, `403`, `404` e `500`) sem expor detalhes internos.

### `UC-CRT-02` — Verificar protocolo publicamente
Permite consultar metadados sanitizados e o hash `SHA-256` de um protocolo (`PROT-…`). O verificador pode selecionar o PDF recebido para comparação local do hash no navegador, sem enviar o arquivo ao servidor.

### `UC-CRT-03` — Verificar certificado publicamente
Permite validar a autenticidade de certificados emitidos pelo sistema (via QR Code ou hash), verificando metadados, hash `SHA-256` e assinatura digital `ED25519` com a chave pública exposta em `/.well-known/jwks.json`.
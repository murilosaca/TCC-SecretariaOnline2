# SO2 — cliente Expo (item 8)

React Native + Expo Router (SDK 57) falando com a **mesma API** Kotlin em `:8080`. NativeWind só neste pacote — **não** copia Tailwind para `frontend-react/`.

P0 demonstrável no **aluno** (`aluno.dev` / `novo.dev`): login → primeiro acesso → `/inicio` (BFF real) → nova solicitação (motor `form_schema`) → presença SECRET (PIN) e QR (câmera ou colar token).

## O que este app **não** é

- Portal admin F7, F6.1, COE, lote CAAF, BFF professor, CRUD da secretaria.
- Menu por `user.role` / `authorities.includes`. Nav = `_links` de `GET /auth/me` + `useActions`.
- Persistência de access/refresh em `AsyncStorage` ou `localStorage`. Refresh vai para Keychain/Keystore (`expo-secure-store`). Access token em memória do processo.
- Cookie `so2_refresh` httpOnly (isso é da **web**). Aqui é o caminho **(A)**: `POST /auth/refresh` e `/auth/logout` aceitam `{ refreshToken }` no body; o login nativo manda `X-SO2-Client: native` e recebe `refreshToken` no JSON (sem logar o valor). A web ignora esse campo e continua só com cookie.
- Expo web nesta fatia (CORS da API não ganhou origem extra; header nativo não entra no `allowedHeaders`).
- Deep-link de deliberação, FCM/push, formativas/CAAF/certificados no app.

## Como subir

API em `:8080` apontando para Postgres `so2_postgres_iam` **:5433**. Web pode ficar em `:5174` ao mesmo tempo.

```bash
cd frontend-react-native
npm install
npx expo start
```

| Onde roda | `EXPO_PUBLIC_API_URL` |
|---|---|
| Emulador Android | `http://10.0.2.2:8080` (default se a env estiver vazia) |
| Simulador iOS | `http://localhost:8080` (default) |
| Aparelho físico | `http://<IP-LAN-da-máquina>:8080` — copie `.env.example` para `.env` |

Expo Go lê a env no start. Sem `EXPO_PUBLIC_API_URL`, Android assume o emulador — no aparelho isso aponta para o próprio aparelho e a API não responde.

```bash
npm test
```

## Auth (RN-F0.1-03)

1. `POST /auth/login` `{ identificador, senha }` — não `{ login, senha }`.
2. Header `X-SO2-Client: native` → JSON inclui `refreshToken`; cookie `so2_refresh` **também** é setado (web intacta).
3. Refresh rotacionado: `POST /auth/refresh` `{ refreshToken }` → novo par; o valor antigo é recusado (reuse).
4. Sem sessão, `/inicio` e o grupo autenticado redirecionam para `/login`. 401 tenta refresh; falha → login.
5. `deviceUuid` estável no SecureStore (não muda por sessão).

## Rotas (espírito Figma)

`/login`, `/recuperar-senha`, `/contato`, `/primeiro-acesso`, `/inicio`, `/solicitacoes`, `/solicitacoes/nova`, `/eventos`, `/eventos/:id/presenca`.

Recuperar senha chama `POST /auth/recuperar-senha` (202). O link do Mailpit abre a **web** `/nova-senha?token=`.

Presença QR: `expo-camera`. Se a permissão for recusada, cole o token — o circuito não quebra. SECRET usa PIN. Sem geofence.

## Contas

Senha `TroqueEstaSenha1!`. Identificador = e-mail ou GRR (`GRR` + 8 dígitos). Sem CPF no formulário.

- `aluno.dev@ufpr.br` / `GRR20240001` — circuito P0.
- `novo.dev@ufpr.br` / `GRR20240002` — primeiro acesso bloqueia o resto.

PIN da oficina seed some se a API reiniciar. Circuito de presença: professor abre a janela na **web**; aluno confirma **neste app**.

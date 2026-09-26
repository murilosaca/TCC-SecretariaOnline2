# SO2 — cliente Expo (itens 8 + 18)

React Native + Expo Router (SDK 57) falando com a **mesma API** Kotlin em `:8080`. NativeWind só neste pacote — **não** copia Tailwind para `frontend-react/`.

**P0 (item 8)** no **aluno** (`aluno.dev` / `novo.dev`): login → primeiro acesso → `/inicio` (BFF real) → nova solicitação (motor `form_schema`) → presença SECRET (PIN) e QR (câmera ou colar token).

**P2 (item 18):** formativas (F1.10/F1.12), estágios (F1.13/F1.14), TCCs (F1.15/F1.16), certificados (F1.19) e portal egresso (F2.1). Menu lê `_links` de `GET /auth/me` (sem whitelist P0). Egresso (`egresso.dev`) cai em `/egresso/inicio`.

## O que este app **não** é

- Portal admin F7, F6.1, pool CAAF/COE, deliberação, BFF professor, CRUD da secretaria.
- Menu por `user.role` / `authorities.includes`. Nav = `_links` de `GET /auth/me` + `useActions` / `navItensVisiveis`.
- Persistência de access/refresh em `AsyncStorage` ou `localStorage`. Refresh vai para Keychain/Keystore (`expo-secure-store`). Access token em memória do processo.
- Cookie `so2_refresh` httpOnly (isso é da **web**). Aqui é o caminho **(A)**: `POST /auth/refresh` e `/auth/logout` aceitam `{ refreshToken }` no body; o login nativo manda `X-SO2-Client: native` e recebe `refreshToken` no JSON (sem logar o valor). A web ignora esse campo e continua só com cookie.
- Expo web (CORS da API não ganhou origem extra; header nativo não entra no `allowedHeaders`).
- Deep-link de deliberação, FCM/push.

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
2. Header `X-SO2-Client: native` → JSON com `refreshToken` (além do access JWT).
3. Refresh opaco no SecureStore; access só em memória.
4. `GET /auth/me` alimenta o menu via `_links`.

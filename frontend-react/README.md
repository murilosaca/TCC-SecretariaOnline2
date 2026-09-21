# Frontend — Secretaria Online 2

React 18 + Vite (RNF-POR-01). Tipos futuros via `openapi-typescript` (RNF-CMP-01).

```bash
npm install
npm run dev
```

A aplicação sobe em `http://localhost:5174` (`vite.config.ts`; **5173 não** é o default deste repo). A API libera CORS para `http://localhost:5173` e `http://localhost:5174`.

Proxies para `http://localhost:8080`: `/academico`, `/publico`, `/auth`, `/bff`, `/requests`, `/request-types`, `/events`, `/formativas` (devolve `index.html` se `Accept` incluir `text/html`), `/certificates`, `/.well-known`, `/v3`, `/swagger-ui`, `/actuator`.

Rotas de exemplo — mapa completo em `docs/telas-figma.md` e `src/App.tsx`.

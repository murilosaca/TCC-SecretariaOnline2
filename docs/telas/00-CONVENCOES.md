# Convenções globais — Especificações Figma

**Aplicável a todas as telas em `telasFigma/telas0/` … `telasFigma/telas8/`.**

---

## 1. Regra imutável

> **DashboardA (`telas1/F1.1-inicio-aluno.md`) é o blueprint estrutural e visual de todas as telas autenticadas.**

- Ignorar completamente DashboardB e DashboardC.
- Zero hex/px hardcoded — apenas **Figma Variables**.

---

## 2. Tokens (Variables)

### Spacing
| Token | Valor |
|-------|-------|
| space/xs | 4px |
| space/sm | 8px |
| space/md | 16px |
| space/lg | 24px |
| space/xl | 32px |
| space/2xl | 40px |

### Radius
| Token | Valor |
|-------|-------|
| radius/sm | 4px |
| radius/md | 8px |
| radius/lg | 12px |
| radius/full | 9999px |

### Typography
| Estilo | Tamanho/Peso |
|--------|--------------|
| Heading/H1 | 32px / 700 |
| Heading/H2 | 24px / 600 |
| Heading/H3 | 20px / 600 |
| Body | 16px / 400 |
| Caption | 12px / 400 |

### Cores semânticas (obrigatórias)
- `color/brand/primary`, `color/surface/default`, `color/surface/elevated`
- `color/text/primary`, `color/text/secondary`, `color/text/muted`
- `color/border/default`, `color/border/strong`
- `color/status/success|warning|danger|info` (bg, text, border)

---

## 3. Shells

### AppLayout (autenticado)
```
h-screen flex row
├─ Sidebar w-256 (64 tailwind) — surface/elevated, border-r
│   ├─ Logo h-64 px-24
│   ├─ NavItem list (flex-1 scroll)
│   └─ UserMenu h-64 border-t
└─ Main flex-1 column
    ├─ Topbar h-64 sticky — surface/default, border-b
    │   ├─ PageTitle (H2)
    │   ├─ SearchBar (Ctrl+K)
    │   └─ NotificationBell + Avatar
    └─ PageContent flex-1 scroll p-24
```

### AuthLayout (login, recuperar senha)
- Fundo `surface/auth`, card central max 420px.

### PublicLayout (contato, verificadores)
- Header minimal + conteúdo + footer institucional.

### AdminLayout
- Igual AppLayout com seção nav "Administração" e breadcrumb reforçado.

---

## 4. Grid e breakpoints

| Breakpoint | Largura | Comportamento |
|------------|---------|---------------|
| mobile | 375px | sidebar overlay, stacks |
| sm | 640px | sidebar colapsável |
| lg | 1024px | sidebar fixa |
| xl | 1280px | KpiRow 4 colunas |

---

## 5. Estados obrigatórios (toda lista/card)

1. **Loading** — `DS/Skeleton`
2. **Empty** — `DS/EmptyState` + CTA opcional
3. **Error** — `DS/AlertBanner` (parcial ou total)

---

## 6. HATEOAS

A UI é **cega a perfis**. Botões/links só existem se a API retornar `_links.{rel}`.

No Figma: anotar com sticky notes quais `_links` controlam cada botão.

---

## 7. Checklist antes de publicar frame

- [ ] Variables vinculadas (sem hex solto)
- [ ] Loading + Empty + Error representados
- [ ] Mobile 375px criado ou responsivo documentado
- [ ] Focus ring `border/focus` em interativos
- [ ] Componentes reutilizam `DS/*` existentes
- [ ] Nomenclatura layer: `Shell/*`, `DS/*`, `Main/*`

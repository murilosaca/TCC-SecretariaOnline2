#!/usr/bin/env python3
"""Gera especificações Figma por tela — SecretariaOnline2."""
from pathlib import Path

OUT = Path(__file__).parent

CONVENTIONS_REF = "Ver `telasFigma/00-CONVENCOES.md` para tokens, shells e checklist global."


def screen_out_path(fname: str) -> Path:
    """Resolve `telasFigma/telas{n}/` a partir do prefixo F{n} do arquivo."""
    if not fname.startswith("F") or len(fname) < 3 or not fname[1].isdigit():
        raise ValueError(f"Nome de spec inválido (esperado F<n>.*.md): {fname}")
    phase = fname[1]
    return OUT / f"telas{phase}" / fname

def spec(
    code, route, title, flow, shell, platform, priority, capability, api,
    purpose, layout, sections, components, states, responsive, a11y, hateoas, figma_notes, variants=None
):
    lines = [
        f"# {code} — {title}",
        "",
        f"**Rota:** `{route}`  ",
        f"**Fluxo:** {flow}  ",
        f"**Shell:** {shell}  ",
        f"**Plataforma:** {platform}  ",
        f"**Prioridade:** {priority}  ",
        f"**Capability:** `{capability}`  ",
        f"**API primária:** {api}",
        "",
        "---",
        "",
        "## 1. Propósito",
        "",
        purpose,
        "",
        "## 2. Layout e estrutura (Auto Layout)",
        "",
        layout,
        "",
        "## 3. Seções e conteúdo",
        "",
        sections,
        "",
        "## 4. Componentes DS/* obrigatórios",
        "",
        components,
        "",
        "## 5. Estados da interface",
        "",
        states,
        "",
        "## 6. Responsividade",
        "",
        responsive,
        "",
        "## 7. Acessibilidade (WCAG 2.1 AA)",
        "",
        a11y,
        "",
        "## 8. HATEOAS / ações condicionais",
        "",
        hateoas,
        "",
        "## 9. Instruções Figma",
        "",
        figma_notes,
    ]
    if variants:
        lines.extend(["", "## 10. Variantes / frames adicionais", "", variants])
    lines.extend(["", "---", "", CONVENTIONS_REF, ""])
    return "\n".join(lines)


SCREENS = []

# --- F0 ---
SCREENS.append(spec(
    "F0.1", "/login", "Login",
    "F0 — Público", "AuthLayout (centrado, sem sidebar)", "Web + Mobile", "P0 — MVP v1",
    "pública", "`POST /auth/login`",
    "Tela de autenticação central. Aceita email institucional, email pessoal ou GRR. Substitui `web/logarUsuario.jsp`. Mensagens genéricas anti-enumeração.",
    """```
Frame: 1440×900 (desktop) + 375×812 (mobile)
AuthLayout — fundo `color/surface/auth`, gradiente sutil brand opcional
  └─ Card central (max-w 420px, `surface/elevated`, radius/lg, shadow/md, padding space/xl)
       ├─ Logo UFPR/SEPT (altura 48px, centralizado)
       ├─ H1 "Entrar" (Heading/H2, text/primary)
       ├─ Form (gap space/md, vertical)
       │    ├─ Input identificador (label "Email ou GRR", placeholder)
       │    ├─ Input senha (type password, toggle visibilidade)
       │    ├─ Link ghost "Esqueci minha senha" → /recuperar-senha
       │    └─ Button primary full-width "Entrar"
       ├─ Divider + links secundários (Caption)
       │    ├─ Link "Contato" → /contato
       │    └─ Link "Verificar protocolo ou certificado"
       └─ Footer institucional (Caption, text/muted)
```""",
    """| Elemento | Especificação visual |
|----------|---------------------|
| Card | `surface/elevated`, border `border/default`, padding `space/xl` (32px), gap interno `space/lg` |
| Inputs | Altura 44px, radius `radius/md`, focus ring `border/focus` 2px |
| Botão Entrar | Variant primary, height 44px, full width |
| Erro global | AlertBanner danger abaixo do título, texto genérico: "Credenciais inválidas" |
| Loading submit | Spinner inline no botão, label "Entrando..." |""",
    "- `DS/Input` (identificador, senha)\n- `DS/Button` (primary, ghost)\n- `DS/Card`\n- `DS/AlertBanner` (erro)\n- `DS/Skeleton` (loading inicial se houver redirect check)",
    """| Estado | Comportamento visual |
|--------|---------------------|
| Default | Form vazio, botão habilitado |
| Validação | Borda `border/error` + Caption danger sob campo |
| Submitting | Botão disabled + spinner |
| Erro auth | AlertBanner danger, campos mantidos (senha limpa) |
| Rate limit | AlertBanner warning "Muitas tentativas. Aguarde." |""",
    "- **Desktop:** card centralizado vertical e horizontalmente.\n- **Mobile (375px):** card full-width com margin horizontal `space/md`; teclado não deve cobrir botão (safe area).\n- Sem sidebar.",
    "- Tab order: identificador → senha → esqueci senha → entrar → links.\n- `aria-label` no toggle de senha.\n- Contraste mínimo 4.5:1 em todos os textos.\n- `aria-live=polite` no AlertBanner de erro.",
    "N/A — tela pública. Após login, redireciona conforme `mustChangePassword` (sem botões condicionais).",
    """1. Criar frame `F0.1 — Login` em `07 — Telas referência`.\n2. Usar `AuthLayout` do page `02 — Shells`.\n3. Vincular todas as cores a Variables — zero hex.\n4. Criar variantes: Default, Error, Loading.\n5. Mobile frame espelhado com constraints fill horizontal.\n6. Protótipo: Entrar → F1.2 ou F1.1 conforme fluxo.""",
))

SCREENS.append(spec(
    "F0.2", "/recuperar-senha", "Recuperar senha",
    "F0", "AuthLayout", "Web + Mobile", "P1",
    "pública", "`POST /auth/forgot-password`",
    "Solicita link de redefinição. Sempre retorna mensagem neutra (anti-enumeração).",
    """```
AuthLayout + Card (max-w 420px)
  ├─ Botão voltar ghost + ícone ArrowLeft → /login
  ├─ H1 "Recuperar senha"
  ├─ Body text/secondary — instrução
  ├─ Input email
  ├─ Button primary "Enviar link"
  └─ Estado sucesso: AlertBanner info + ícone Mail
```""",
    "| Após submit | Card mostra apenas mensagem: \"Se este email existir, enviaremos um link válido por 24h\" — sem revelar existência |",
    "`DS/Input`, `DS/Button`, `DS/AlertBanner`, `DS/Card`",
    "Default → Submitting → Success (mensagem fixa) → Error rede",
    "Igual F0.1",
    "Foco no email; mensagem de sucesso com `aria-live`",
    "N/A",
    "Frames: Default, Success, Error. Protótipo: submit → Success.",
))

SCREENS.append(spec(
    "F0.3", "/nova-senha?token=", "Nova senha",
    "F0", "AuthLayout", "Web + Mobile", "P1",
    "pública (JWT token)", "`POST /auth/reset-password`",
    "Define nova senha via token JWT 1-uso do email. Validação: 12+ chars, complexidade, não reutilizar últimas 3.",
    """```
Card com:
  ├─ H1 "Definir nova senha"
  ├─ Input nova senha + medidor de força (barra 4 níveis: fraca→forte)
  ├─ Input confirmar senha
  ├─ Lista requisitos (checkmarks verdes quando OK)
  └─ Button primary "Salvar senha"
```""",
    "| Medidor força | 4 segmentos horizontais, cores danger→warning→success conforme score |",
    "`DS/Input`, `DS/Button`, `DS/Progress` (força senha), `DS/AlertBanner`",
    "Token inválido: EmptyState + link login. Senhas divergentes: erro inline.",
    "Igual F0.1",
    "Requisitos de senha em lista com `aria-describedby`",
    "N/A",
    "Variantes: Valid token, Invalid token, Success redirect login.",
))

SCREENS.append(spec(
    "F0.4", "/contato", "Contato institucional",
    "F0", "PublicLayout", "Web", "P2",
    "pública", "estático",
    "Página institucional: endereço, telefone, mapa, horários da secretaria.",
    """```
PublicLayout (header simples + footer)
  ├─ Hero compacto com título "Contato"
  ├─ Grid 2 colunas (lg): info textual | mapa embed placeholder
  │    ├─ Card com ícones MapPin, Phone, Clock, Mail
  │    └─ Mapa 16:9 radius/lg
  └─ Link "Voltar ao login"
```""",
    "Informações em `DS/Card` com ícones Lucide 20px, gap `space/md`",
    "`DS/Card`, `PublicLayout`, ícones Lucide",
    "Estático",
    "2 colunas ≥1024px; 1 coluna mobile",
    "Mapa com alt text; links telefone `tel:`",
    "N/A",
    "Frame desktop + mobile. Mapa como placeholder cinza com label.",
))

SCREENS.append(spec(
    "F0.5", "/erro/:codigo", "Página de erro",
    "F0", "PublicLayout / minimal", "Web + Mobile", "P1",
    "pública", "n/a",
    "Tela canônica 401/403/404/500 com mensagem amigável e ID de incidente.",
    """```
Centro vertical:
  ├─ Ilustração EmptyState (por código: cadeado, arquivo, raio)
  ├─ H1 código grande (404, 403...)
  ├─ Body mensagem amigável
  ├─ Caption "ID do incidente: INC-2026-XXXX"
  └─ Buttons: primary "Ir ao início" | secondary "Fazer login"
```""",
    "Cores semânticas: 403 danger subtle, 404 neutral, 500 warning",
    "`DS/EmptyState`, `DS/Button`",
    "Variantes por :codigo (401, 403, 404, 500)",
    "Centralizado em todas as larguras",
    "Ilustração com alt text descritivo",
    "Botões conforme auth state via protótipo",
    "Component set ErrorPage com variant property `code`.",
))

SCREENS.append(spec(
    "F0.6", "/publico/verificar-protocolo/:id", "Verificar protocolo",
    "F0", "PublicLayout", "Web", "P2",
    "pública", "`GET /publico/protocolos/{id}/verificacao`",
    "Verificador externo de protocolo PDF. Metadados sanitizados + upload opcional para validar hash.",
    """```
Largura max 720px centralizada
  ├─ H1 "Verificação de protocolo"
  ├─ Card resultado: número, tipo, status Badge, datas, hash truncado
  ├─ Zona drag-drop (border dashed, ícone Upload)
  │    └─ Após upload: ícone Check success + "Hash confere"
  └─ Caption explicativo anti-fraude
```""",
    "Hash em fonte mono, `surface/code`, padding `space/sm`",
    "`DS/Card`, `DS/Badge`, `DS/FileDropzone`, `DS/AlertBanner`",
    "Loading skeleton, Protocolo não encontrado (EmptyState), Hash não confere (danger)",
    "Dropzone full-width mobile",
    "Dropzone acessível via botão 'Selecionar arquivo'",
    "N/A",
    "Frames: Result OK, Upload match, Upload mismatch, Not found.",
))

SCREENS.append(spec(
    "F0.7", "/publico/verificar-certificado/:hash", "Verificar certificado",
    "F0", "PublicLayout", "Web", "P2",
    "pública", "`GET /publico/certificados/{hash}/verificacao`",
    "Verifica certificado emitido pelo sistema: SHA-256 + assinatura ED25519 via JWKS.",
    """```
Similar F0.6 com:
  ├─ Badge grande "Certificado válido" (success) ou "Inválido" (danger)
  ├─ Dados: beneficiário parcial, evento/atividade, data emissão
  ├─ Bloco assinatura digital (ícone Shield)
  └─ Link "Ver chave pública" → /.well-known/jwks.json
```""",
    "Selo visual de validação 64px com ícone ShieldCheck",
    "`DS/Badge`, `DS/Card`, `DS/FileDropzone`",
    "Válido, Inválido, Expirado",
    "Igual F0.6",
    "Status anunciado via `aria-live`",
    "N/A",
    "Destaque visual no selo de validação — componente `DS/VerificationSeal`.",
))

# F1.1 Dashboard - THE blueprint
SCREENS.append(spec(
    "F1.1", "/inicio", "Dashboard do Aluno",
    "F1 — Aluno", "AppLayout", "Web + Mobile", "P0 — MVP v1 — BLUEPRINT",
    "dashboard.view_own", "`GET /bff/dashboard/aluno`",
    "**TELA REFERÊNCIA DO PROJETO (DashboardA).** Visão única do estudante: KPIs, pendências, solicitações, eventos, prazos, atalhos. Toda tela autenticada herda spacing e hierarquia desta.",
    """```
AppLayout (ver 00-CONVENCOES)
PageContent (padding space/lg, gap space/lg, vertical)
  ├─ Saudação: H1 "Olá, {nome}" + Caption curso/período
  ├─ KpiRow: grid 4 colunas iguais, gap space/md
  │    └─ 4× DS/KpiCard (horas formativas c/ progress, solicitações, eventos hoje, certificados)
  └─ MainGrid: horizontal gap space/lg, ratio 2:1
       ├─ Coluna esquerda (flex 2, gap space/lg, vertical):
       │    ├─ AlertBanner (se alertas) — dismissible warning
       │    ├─ Section "Pendências" + PendenciasList (max 3 itens)
       │    ├─ Section "Últimas solicitações" + DataTable compact (5 linhas × 3 cols)
       │    └─ Section "Próximos eventos" + EventosList (3 cards)
       └─ Coluna direita (flex 1, gap space/lg, vertical):
            ├─ Card "Prazos" (lista 3 itens com data)
            ├─ Card "Último parecer" (Badge estado + excerpt)
            └─ AtalhosTiles grid 2×3 QuickTile
```""",
    """| Bloco | Detalhe Figma |
|-------|---------------|
| KpiCard horas | Valor 72/120 + barra progress `brand/primary` + ícone Clock |
| Pendência | Título + Badge status + link CTA da `_links` |
| Tabela solicitações | Colunas: Número, Tipo, Estado (Badge), Prazo (SLA vermelho se breach) |
| EventoRow | Título, data, Badge "Janela aberta" success se aplicável |
| QuickTile | Ícone 24px + label 2 linhas max, hover `surface/subtle` |""",
    "KpiCard, AlertBanner, PendenciaItem, DataTable, EventoRow, QuickTile, Card, Badge, Skeleton, EmptyState",
    """| Estado | Visual |
|--------|--------|
| Loading | Skeleton em cada bloco (KpiRow 4 retângulos, listas 3 linhas) |
| Empty parcial | EmptyState por seção ("Nenhuma pendência") |
| Erro parcial | AlertBanner warning no bloco afetado, resto renderiza |
| Pull refresh mobile | Indicador nativo no topo |""",
    "- **≥1280px:** KpiRow 4 colunas, MainGrid 2:1.\n- **768–1023px:** KpiRow 2×2, MainGrid empilha (esquerda acima).\n- **<768px:** KpiRow 2×2 ou scroll horizontal; sidebar overlay.",
    "Landmarks: `main`, `section` com `aria-labelledby`. QuickTiles como botões com label descritivo.",
    "Renderizar CTAs apenas se `_links` existir (ex.: `novaSolicitacao`). Badge Hub comunicação se `_links.hub` com contagem.",
    """1. Frame master `F1.1 — Dashboard Aluno` 1440×1024.\n2. Duplicar estrutura para Skeleton e Empty states.\n3. Marcar como **Componente de referência** no Figma.\n4. Mobile frame 375px com mesma hierarquia empilhada.\n5. Anotar spacing: page padding 24px, section gap 24px, card padding 16px.\n6. **NÃO** criar variantes B/C de dashboard.""",
))

SCREENS.append(spec(
    "F1.2", "/primeiro-acesso", "Primeiro acesso",
    "F1", "AppLayout simplificado (sem nav)", "Web + Mobile", "P0 — MVP v1",
    "auth.first_access", "`POST /auth/first-access`",
    "Bloqueante: troca senha inicial + aceite LGPD. Sem acesso a outras rotas até concluir.",
    """```
Layout centrado em AppLayout sem sidebar links
  ├─ Card max-w 560px centralizado
  │    ├─ Ícone Shield + H1 "Primeiro acesso"
  │    ├─ Body explicativo
  │    ├─ Input nova senha + confirmar (igual F0.3)
  │    ├─ Checkbox LGPD (obrigatório) + link política
  │    └─ Button primary "Continuar"
  └─ Progress step único (opcional: 1/1)
```""",
    "Checkbox com área de toque 44px; link política abre modal ou nova aba",
    "`DS/Input`, `DS/Checkbox`, `DS/Button`, `DS/Card`",
    "LGPD não aceito: botão disabled. Submitting: spinner.",
    "Card full-width mobile com margin md",
    "Checkbox associado a label clicável; erro com aria-live",
    "N/A — bloqueio total de navegação",
    "Frame bloqueado: sem sidebar items clicáveis no protótipo.",
))

# Continue with more screens - I'll add them in batches in the Python file
# For brevity in the script, I'll add a helper for list/detail patterns

def list_screen(code, route, title, flow, cap, api, purpose, cols, filters, cta):
    return spec(
        code, route, title, flow, "AppLayout", "Web + Mobile", "P2", cap, api, purpose,
        f"""```
PageContent
  ├─ Header row: H1 + Button primary "{cta}"
  ├─ FilterBar (gap space/sm): {filters}
  ├─ DataTable / Card list
  │    Colunas: {cols}
  └─ Pagination footer
```""",
        f"Tabela com zebra `surface/subtle` em hover row. SLA breach: texto `status/danger`.",
        "`DS/DataTable`, `DS/Button`, `DS/Badge`, `DS/Input` (busca), `DS/EmptyState`, `DS/Skeleton`",
        "Loading skeleton rows | Empty | Error AlertBanner | Filtro sem resultados",
        "Tabela → cards empilhados em mobile; filtros em Sheet/drawer",
        "Cabeçalhos `th` scope; ordenação indicada com ícone",
        "Ações por linha via `_links` — ocultar botão se rel ausente",
        f"Usar pattern `Pattern/ListPage`. Criar variantes Loaded e Empty.",
    )

def detail_screen(code, route, title, flow, cap, api, purpose, blocks):
    return spec(
        code, route, title, flow, "AppLayout", "Web + Mobile", "P2", cap, api, purpose,
        f"""```
PageContent max-w 960px
  ├─ Breadcrumb + Header (título + Badge estado + ActionBar)
  ├─ Grid 2 colunas lg: conteúdo | sidebar metadados
  {blocks}
```""",
        "ActionBar: botões alinhados à direita, gap `space/sm`",
        "`DS/Card`, `DS/Badge`, `DS/TimelineItem`, `DS/Button`, `DS/AttachmentList`",
        "Loading skeleton | 404 EmptyState | Ações submitting",
        "Sidebar metadados abaixo do conteúdo em mobile",
        "Timeline com lista ordenada semântica",
        "**Crítico:** ActionBar 100% dirigida por `_links` HATEOAS",
        "Frame com anotações dos `_links` esperados por estado.",
    )

SCREENS.append(list_screen("F1.3", "/perfil", "Perfil", "F1", "user.update_own_profile",
    "`GET /me`, `PATCH /me`", "Dados pessoais: foto, telefone, email pessoal, nome social.",
    "N/A — formulário", "n/a", "Salvar alterações"))

SCREENS[-1] = spec("F1.3", "/perfil", "Perfil", "F1", "AppLayout", "Web + Mobile", "P2",
    "user.update_own_profile", "`GET /me`, `PATCH /me`",
    "Edição de dados pessoais não-acadêmicos.",
    """```
Form 2 colunas lg (avatar | campos)
  ├─ Avatar upload circular 96px + botão "Alterar foto"
  ├─ Inputs: nome social, telefone, email pessoal, identidade gênero (select opcional)
  └─ Footer sticky: Cancelar ghost + Salvar primary
```""",
    "Avatar com fallback iniciais; crop modal ao upload",
    "`DS/Avatar`, `DS/Input`, `DS/Select`, `DS/Button`",
    "Dirty state, Saving, Saved toast",
    "Form 1 coluna mobile",
    "Avatar com alt text; campos com labels visíveis",
    "N/A",
    "Frame Edit + Saving success toast.",
)

for s in [
    ("F1.4", "/perfil/seguranca", "Segurança", "Trocar senha + lista sessões ativas com botão Encerrar por device."),
    ("F1.5", "/perfil/notificacoes", "Notificações", "Matriz prioridade×canal, DND horário, digest email."),
]:
    SCREENS.append(spec(s[0], s[1], s[2], "F1", "AppLayout", "Web + Mobile", "P2",
        "user.*", "REST /me/*", s[3],
        """```
PageContent seções em Cards empilhados (gap space/lg)
  └─ Cada seção: H2 + conteúdo específico
```""",
        "Ver descrição no propósito",
        "`DS/Card`, `DS/Input`, `DS/Switch`, `DS/DataTable` (sessões)",
        "Loading | Saved | Error",
        "Cards full-width mobile",
        "Switches com label; tabela sessões com headers",
        "Encerrar sessão só se `_links.encerrar`",
        "Pattern SettingsPage.",
    ))

SCREENS.append(spec("F1.6", "/comunicacao", "Hub de comunicação", "F1", "AppLayout", "Web + Mobile", "P2",
    "communication.read", "`GET /communications`",
    "Hub unificado: notícias, avisos, inbox de ações pendentes.",
    """```
  ├─ Tabs: Todos | Institucional | Turma | Inbox (badge count)
  ├─ FilterBar: tipo, curso, lido/não lido
  └─ Lista CommunicationRow: ícone tipo, título, data, Badge prioridade, unread dot
```""",
    "Inbox: items com CTA pulsante se requer ação",
    "`DS/Tabs`, `DS/Badge`, `CommunicationRow`, `DS/EmptyState`",
    "Loading, Empty, Filtro vazio",
    "Tabs scrolláveis mobile",
    "Tab selecionada com aria-selected",
    "Marcar lido se `_links.marcar-lido`",
    "Criar componente CommunicationRow com variant unread.",
))

SCREENS.append(list_screen("F1.7", "/solicitacoes", "Minhas solicitações", "F1",
    "request.view_own", "`GET /requests?solicitante=me`",
    "Lista próprias solicitações com filtros e SLA visual.",
    "Número, Tipo, Estado, Prazo, SLA", "estado, tipo, ano, busca", "Nova solicitação"))

SCREENS.append(spec("F1.8", "/solicitacoes/nova", "Nova solicitação (Wizard)", "F1", "AppLayout", "Web + Mobile", "P1 — MVP v2",
    "request.open", "`GET /request-types/{code}`, `POST /requests`",
    "Wizard genérico 3 passos dirigido por form_schema. Coração do DRY.",
    """```
WizardStepper (1. Tipo → 2. Formulário → 3. Revisar)
  Passo 1: grid cards RequestType + select curso/disciplina
  Passo 2: DynamicForm (campos do JSON Schema) + AttachmentUpload
  Passo 3: resumo legível + anexos preview + Confirmar
  Footer: Voltar | Continuar / Confirmar
```""",
    "Stepper horizontal desktop, vertical compact mobile. Campos condicionais com animação fade.",
    "`DS/WizardStepper`, `DS/DynamicForm`, `DS/AttachmentUpload`, `DS/Card`, `DS/Button`",
    "Rascunho local, validação Zod inline, upload progress",
    "Passo 2 scrollável; footer sticky",
    "Stepper com aria-current step",
    "Tipos filtrados por `_links` em GET /request-types",
    "**Prioridade MVP v2.** Criar frames por passo + estados validação.",
))

SCREENS.append(detail_screen("F1.9", "/solicitacoes/:id", "Detalhe solicitação", "F1",
    "request.view_own", "`GET /requests/{id}`",
    "Detalhe com timeline, anexos, ações HATEOAS, gerar protocolo.",
    """  ├─ Card dados estruturados (render JSON amigável)
  ├─ Timeline request_event (mais recente topo)
  ├─ AttachmentList
  └─ Sidebar: prazo, SLA, metadados"""))

SCREENS.append(list_screen("F1.10", "/formativas", "Atividades formativas", "F1",
    "formative.view_own", "`GET /formative-entries?aluno=me`",
    "Resumo horas validadas vs requeridas + lista entradas.",
    "Atividade, Horas, Estado, Data", "estado, tipo", "Nova atividade"))

SCREENS.append(spec("F1.11", "/formativas/nova", "Nova formativa", "F1", "AppLayout", "Web + Mobile", "P2",
    "formative.submit", "`POST /formative-entries`",
    "Submissão de atividade formativa com comprovante.",
    """```
Form: Select atividade → Input horas → FileDropzone PDF/imagem → Observações
```""",
    "Se pré-validada por evento: banner info + campos readonly",
    "`DS/Select`, `DS/Input`, `DS/FileDropzone`",
    "Upload, validação",
    "1 coluna",
    "Dropzone acessível",
    "N/A",
    "Frame normal + pré-preenchido evento.",
))

SCREENS.append(detail_screen("F1.12", "/formativas/:id", "Detalhe formativa", "F1",
    "formative.view_own", "`GET /formative-entries/{id}`",
    "Parecer CAAF, comprovante, certificado vinculado.",
    "  ├─ Status + parecer\n  ├─ Comprovante preview\n  └─ Link certificado se aprovada"))

SCREENS.append(list_screen("F1.13", "/estagios", "Estágios", "F1", "internship.view_own",
    "`GET /internships?aluno=me`", "Lista estágios com empresa, vigência, pendências.",
    "Empresa, Supervisor, Vigência, Situação", "situação", "N/A"))

SCREENS.append(detail_screen("F1.14", "/estagios/:id", "Detalhe estágio", "F1",
    "internship.view_own", "`GET /internships/{id}`",
    "Upload TCE, relatórios, pareceres COE.",
    "  ├─ Tabs Documentos | Pareceres\n  └─ Upload por tipo documento"))

SCREENS.append(list_screen("F1.15", "/tccs", "TCC", "F1", "tcc.view_own",
    "`GET /tccs?aluno=me`", "TCC ativo: equipe, banca, datas.",
    "Título, Orientador, Situação, Defesa", "situação", "N/A"))

SCREENS.append(detail_screen("F1.16", "/tccs/:id", "Detalhe TCC", "F1",
    "tcc.view_own", "`GET /tccs/{id}`",
    "Upload final, agendamento defesa.",
    "  ├─ Equipe banca\n  ├─ Upload arquivo final\n  └─ Datas-chave"))

SCREENS.append(spec("F1.17", "/eventos", "Eventos (aluno)", "F1", "AppLayout", "Web + Mobile", "P2",
    "attendance.view_open", "`GET /events?audience=me`",
    "Tabela eventos read-only + modal detalhe com AttendanceWidget dinâmico.",
    """```
  ├─ DataTable: título, período, estado evento, organizador, CH, situação presença
  └─ Modal detalhe: descrição + AttendanceWidget (variant por attendanceMode)
```""",
    "Badge estado: Agendado info, Em andamento success, Concluído neutral. Presença: pendente/parcial/completa",
    "`DS/DataTable`, `DS/Modal`, `DS/AttendanceWidget`, `DS/Badge`",
    "Modal com widget só se janela ativa + `_links`",
    "Tabela → cards mobile",
    "Modal focus trap",
    "Widget e botões 100% HATEOAS session",
    "Criar AttendanceWidget com variants: SECRET_SINGLE, SECRET_DUAL, QR_SINGLE, QR_DUAL.",
))

SCREENS.append(spec("F1.18", "/eventos/:id/presenca", "Presença (página dedicada)", "F1", "AuthLayout/AppLayout", "Web preferencial", "P2",
    "attendance.check_in", "`GET /events/{id}/attendance/session`",
    "Validação presença web-first: PIN, QR, fases duplas, timers de janela.",
    """```
Card central max-w 480px
  ├─ Header evento (título, organizador)
  ├─ Countdown janela ativa (mono, grande)
  ├─ AttendanceWidget (modo específico)
  └─ Feedback sucesso/erro
```""",
    "Fora da janela: EmptyState + mensagem genérica 403",
    "`DS/AttendanceWidget`, `DS/Input` (PIN), `DS/Countdown`, `DS/AlertBanner`",
    "Fase entrada, fase saída, inelegível, sucesso",
    "Full screen mobile",
    "PIN com autocomplete off; erros aria-live",
    "confirmar-entrada, confirmar-saida via `_links`",
    "Frames por attendanceMode × fase.",
))

SCREENS.append(list_screen("F1.19", "/certificados", "Certificados", "F1",
    "certificate.view_own", "`GET /certificates?beneficiario=me`",
    "Certificados emitidos com download PDF + QR verificação.",
    "Tipo, Evento/Atividade, Data, Download", "tipo, ano", "N/A"))

SCREENS.append(list_screen("F1.20", "/meus-atendimentos", "Meus atendimentos", "F1",
    "service_record.view_own", "`GET /service-records?aluno=me`",
    "Atendimentos secretaria com ciência pendente.",
    "Data, Assunto, Status, Ação", "pendente", "N/A"))

# F2
SCREENS.append(spec("F2.1", "/egresso/inicio", "Dashboard Egresso", "F2", "AppLayout", "Web + Mobile", "P2",
    "alumni.view_own", "`GET /alumni/me`",
    "Dashboard read-only: histórico, certificados, diploma, colação.",
    """```
Similar F1.1 mas read-only: KpiRow histórico + cards certificados/diploma + botão Reemitir PDF
```""",
    "Sem CTAs de criação; badges 'Concluído'",
    "KpiCard, Card, Badge, Button secondary",
    "Loading, Empty",
    "Igual F1.1",
    "Indicar conteúdo read-only",
    "Reemitir se `_links.reemitir`",
    "Derivar de F1.1 com anotação READ ONLY.",
))

# F3 Professor
SCREENS.append(spec("F3.1", "/inicio", "Dashboard Professor", "F3", "AppLayout", "Web + Mobile", "P2",
    "dashboard.view_self_professor", "`GET /bff/dashboard/professor`",
    "Fila deliberar, formativas CAAF (se membro), eventos organizador, SLA.",
    """```
Mesma estrutura DashboardA com blocos:
  ├─ KpiRow: pendentes deliberar, formativas revisão, eventos hoje, SLA
  ├─ Fila solicitações (DataTable)
  ├─ Atalhos eventos + gestão
  └─ Card formativas CAAF (oculto se API não retornar bloco)
```""",
    "Card CAAF só aparece se dados no BFF — não por perfil",
    "Mesmos DS do F1.1",
    "Igual F1.1",
    "Igual F1.1",
    "Igual F1.1",
    "Todos CTAs via `_links`",
    "Duplicar F1.1 frame e ajustar blocos.",
))

SCREENS.append(list_screen("F3.2a", "/professor/eventos", "Eventos professor — Lista", "F3",
    "event.manage", "`GET/POST /events`", "CRUD eventos presença. Filtro onlyMine.",
    "Título, Agenda, Modo, Estado, CH", "onlyMine, curso", "Novo evento"))

SCREENS.append(spec("F3.2b", "/professor/eventos/:id", "Evento — Formulário", "F3", "AppLayout", "Web", "P2",
    "event.manage", "`GET/PATCH /events/{id}`",
    "Edição/visualização evento com todos campos v4.1.",
    """```
Form seções:
  1. Metadados (título, descrição, curso, público)
  2. Agenda (inicioEm, fimEm)
  3. Horas formativas (chCreditadas)
  4. Modo presença (radio: QR_SINGLE, QR_DUAL, SECRET_SINGLE, SECRET_DUAL)
  5. Janelas validação (builder: intervalo único ou duas sub-janelas)
  Footer: Excluir danger | Salvar
```""",
    "Read-only: inputs disabled + banner info",
    "`DS/Input`, `DS/Select`, `DS/RadioGroup`, `DS/DateTimePicker`, `WindowBuilder`",
    "Editável, Read-only, Saving",
    "Seções accordion mobile",
    "Radios com fieldset legend",
    "Editar/excluir só via `_links`",
    "Componente WindowBuilder para janelas.",
))

SCREENS.append(spec("F3.2c", "/professor/eventos/:id/operacao", "Operação evento (ao vivo)", "F3", "AppLayout", "Web preferencial", "P2",
    "event.host", "endpoints presença v4.1",
    "Painel dia do evento: estados, abrir janelas, QR/PIN, contadores.",
    """```
Layout 2 colunas:
  Esquerda: Status grande (Agendado/Em andamento/Concluído) + Countdown janela
  Centro: QR display OU PIN display (conforme modo)
  Direita: Contadores presentes/inelegíveis + lista ao vivo
  ActionBar: Abrir janela entrada | Abrir saída | Encerrar evento
```""",
    "QR em card 280×280 com borda brand. PIN em tipografia mono 32px",
    "`DS/Countdown`, `DS/QRDisplay`, `DS/PINDisplay`, `DS/DataTable` live",
    "Pré-evento, janela entrada, janela saída, encerrado",
    "Empilhar em tablet; projetor-friendly 1280px",
    "Alto contraste para projeção",
    "abrir-janela-*, obter-qr-*, encerrar-evento",
    "**Tela crítica presença.** Frame por modo e fase.",
))

SCREENS.append(list_screen("F3.3", "/solicitacoes?to=me", "Fila deliberar (professor)", "F3",
    "request.deliberate", "`GET /requests?canDeliberate=true`",
    "Solicitações para deliberar com ações em lote.",
    "Número, Aluno, Tipo, Prazo, SLA", "tipo, curso, atraso", "N/A"))

SCREENS.append(spec("F3.4", "/solicitacoes/:id/deliberar", "Deliberar solicitação", "F3", "AppLayout", "Web + Mobile", "P2",
    "request.deliberate", "`GET /requests/{id}`, `POST /transitions`",
    "Tela deliberação: dados, anexos, parecer, ações Deferir/Indeferir/Ajustes.",
    """```
Baseado F1.9 +
  ├─ Painel decisão sticky: Textarea parecer + botões ação (variantes)
  └─ Deep-link token: banner login se necessário
```""",
    "Deferir=success, Indeferir=danger, Ajustes=warning",
    "`DS/Textarea`, `DS/Button` variants, `DS/TimelineItem`",
    "Com/sem token email",
    "Painel decisão bottom sheet mobile",
    "Textarea com label Parecer",
    "Ações = `_links` (deferir, indeferir, solicitar-ajustes)",
    "Frames por ação disponível.",
))

SCREENS.append(list_screen("F3.5", "/formativas?to=me", "Revisão formativas (CAAF)", "F3",
    "formative.review", "`GET /formative-entries?canReview=true`",
    "Fila CAAF — só visível se capability + vínculo comissão.",
    "Aluno, Atividade, Horas, Estado", "curso, estado", "Revisar em lote"))

SCREENS.append(list_screen("F3.6", "/estagios?to=me", "Estágios — orientador/COE", "F3",
    "internship.review", "`GET /internships?canReview=true`",
    "Estágios para parecer documentos.",
    "Aluno, Empresa, Doc pendente", "tipo doc", "N/A"))

SCREENS.append(list_screen("F3.7", "/tccs?to=me", "TCC — orientador/banca", "F3",
    "tcc.supervise", "`GET /tccs?canReview=true`",
    "TCCs para nota/parecer.",
    "Aluno, Título, Papel, Situação", "situação", "N/A"))

SCREENS.append(spec("F3.8", "/comunicacao/publicar", "Publicar comunicado", "F3", "AppLayout", "Web", "P2",
    "communication.publish_class", "`POST /communications`",
    "Editor Markdown + preview + audiência + prioridade.",
    """```
  ├─ Input título
  ├─ Split view: Editor Markdown | Preview
  ├─ Select audiência (turma/curso/todos) + prioridade + expiração
  └─ Publicar primary
```""",
    "Preview com mesma tipografia Body",
    "`DS/Textarea`, `MarkdownPreview`, `DS/Select`",
    "Draft, Publishing, Success",
    "Editor full-width mobile, preview em tab",
    "Editor com label",
    "N/A",
    "Pattern EditorPage.",
))

# F4
SCREENS.append(spec("F4.1", "/comissoes/caaf", "Comissão CAAF", "F4", "AppLayout", "Web", "P2",
    "formative.review", "`GET /commissions/caaf/dashboard`",
    "Dashboard comissão: lote revisão, atribuição membros, KPIs.",
    """```
  ├─ KpiRow indicadores fila
  ├─ DataTable lote com checkbox seleção
  └─ Painel atribuição membro
```""",
    "Ações em lote na toolbar",
    "`DS/DataTable`, `KpiCard`, `DS/Button`",
    "Loading, seleção múltipla",
    "Desktop-first",
    "Checkbox select-all",
    "Atribuir via `_links`",
    "Pattern CommissionDashboard.",
))

SCREENS.append(spec("F4.2", "/comissoes/coe", "Comissão COE", "F4", "AppLayout", "Web", "P2",
    "internship.review", "`GET /commissions/coe/dashboard`",
    "Mesma estrutura F4.1 para estágios.",
    "Igual F4.1 adaptado para estágios",
    "Igual F4.1",
    "Igual F4.1",
    "Igual F4.1",
    "Igual F4.1",
    "Igual F4.1",
    "Igual F4.1",
    "Duplicar F4.1 com labels COE.",
))

# F5 Secretaria - dashboard
SCREENS.append(spec("F5.1", "/inicio", "Dashboard Secretaria", "F5", "AppLayout", "Web", "P2",
    "dashboard.view_secretary", "`GET /bff/dashboard/secretary`",
    "Filas deliberação, contadores por estado/curso, SLA, agenda do dia.",
    """```
DashboardA adaptado:
  ├─ KpiRow: abertas, atrasadas, concluídas hoje, eventos dia
  ├─ Fila solicitações prioritárias
  ├─ Alertas SLA breach
  └─ QuickTiles CRUDs frequentes
```""",
    "Destaque vermelho para SLA breach",
    "Igual F1.1 + emphasis SLA",
    "Igual F1.1",
    "Desktop 1280px+",
    "Igual F1.1",
    "Via `_links`",
    "Variante secretaria do dashboard.",
))

SCREENS.append(list_screen("F5.2", "/solicitacoes", "Fila central secretaria", "F5",
    "request.view_curso", "`GET /requests`",
    "Consulta geral como fila viva com filtros persistentes e export CSV.",
    "Número, Aluno, Tipo, Estado, Deliberador, SLA", "estado, tipo, curso, atraso", "Nova interna"))

SCREENS.append(spec("F5.3", "/solicitacoes/nova", "Nova solicitação interna", "F5", "AppLayout", "Web", "P2",
    "request.internal_open", "`POST /requests {onBehalfOf}`",
    "Mesmo wizard F1.8 + campo 'Em nome de' (busca aluno).",
    """```
Wizard F1.8 + no passo 1:
  └─ Combobox busca aluno (GRR/nome) — campo extra
```""",
    "Campo só visível por capability — no Figma anotar como conditional",
    "Wizard F1.8 + `DS/Combobox`",
    "Igual F1.8",
    "Igual F1.8",
    "Combobox com aria-autocomplete",
    "N/A",
    "Variante do frame F1.8.",
))

SCREENS.append(spec("F5.4", "/solicitacoes/:id/deliberar", "Deliberar (secretaria)", "F5", "AppLayout", "Web", "P2",
    "request.deliberate", "igual F3.4",
    "Mesma tela F3.4 sem token email — ações conforme request_type.",
    "Reutilizar frame F3.4",
    "Idêntico F3.4",
    "F3.4 components",
    "Igual F3.4",
    "Igual F3.4",
    "Igual F3.4",
    "Igual F3.4",
    "Instância F3.4 — não duplicar design.",
))

SCREENS.append(list_screen("F5.5", "/secretaria/atrasados", "Solicitações atrasadas", "F5",
    "request.view_curso", "`GET /requests?slaBreached=true`",
    "Filtro persistente SLA breach — variante F5.2.",
    "Igual F5.2", "n/a", "Exportar"))

SCREENS.append(spec("F5.6", "/secretaria/alunos", "Gestão alunos", "F5", "AppLayout", "Web", "P2",
    "user.manage_students", "`/students` CRUD",
    "CRUD alunos: busca GRR/nome trigram, matrícula, reset senha.",
    """```
  ├─ SearchBar avançada
  ├─ DataTable + row actions
  └─ Drawer/Modal formulário aluno
```""",
    "Ações row: editar, reset senha, matrícula",
    "`DS/DataTable`, `DS/Drawer`, `DS/Input`",
    "CRUD states",
    "Drawer full-screen mobile",
    "Form labels",
    "Ações via `_links`",
    "Pattern CRUD admin.",
))

for code, route, title, cap, cols in [
    ("F5.7", "/secretaria/cursos", "Cursos", "course.manage", "Nome, Sigla, Secretários"),
    ("F5.8", "/secretaria/disciplinas", "Disciplinas", "subject.manage", "Nome, Curso, Período, Ativa"),
    ("F5.10", "/secretaria/egressos", "Egressos", "alumni.list", "Nome, Curso, Ano colação"),
]:
    SCREENS.append(list_screen(code, route, title, "F5", cap, f"`{route}` CRUD",
        f"CRUD {title.lower()} com busca e export CSV.", cols, "busca, filtros", "Novo"))

SCREENS.append(spec("F5.9", "/secretaria/calendarios", "Calendários", "F5", "AppLayout", "Web", "P2",
    "calendar.manage", "`/calendars` CRUD",
    "Períodos letivos e eventos calendário acadêmico.",
    """```
  ├─ Tabs: Períodos | Eventos
  ├─ Calendar view mensal + lista
  └─ Modal evento
```""",
    "Tipos evento com cores semânticas",
    "`DS/Calendar`, `DS/Modal`",
    "Month/week views",
    "Calendar scroll horizontal mobile",
    "Dias com aria-label",
    "N/A",
    "Integrar componente Calendar.",
))

SCREENS.append(spec("F5.11", "/secretaria/diplomas", "Diplomas e colação", "F5", "AppLayout", "Web", "P2",
    "diploma.register", "`POST /graduations`",
    "Registrar colação + entrega física diploma → perfil EGRESSO.",
    """```
Wizard 2 passos: Selecionar elegíveis | Confirmar colação + entrega
```""",
    "Lista elegíveis com checkbox",
    "`DS/DataTable`, `DS/WizardStepper`",
    "Wizard states",
    "Desktop",
    "Confirm dialog destructive",
    "N/A",
    "Wizard pattern.",
))

SCREENS.append(spec("F5.12", "/secretaria/autorizacoes-imagem", "Autorizações imagem", "F5", "AppLayout", "Web", "P2",
    "image_authorization.review", "`GET /requests?type=AUTORIZACAO_IMAGEM`",
    "Revisão em lote — variante compacta F5.2.",
    """```
Layout compacto: cards densos + approve/reject batch
```""",
    "Foto thumbnail 48px na linha",
    "`DS/DataTable` compact",
    "Batch selection",
    "Desktop",
    "Thumbnails alt text",
    "Aprovar lote via `_links`",
    "Variante densa lista.",
))

SCREENS.append(spec("F5.13", "/secretaria/atendimentos", "Registrar atendimento", "F5", "AppLayout", "Web", "P2",
    "service_record.create", "`POST /service-records`",
    "Form atendimento: aluno, assunto, resposta, anexo.",
    """```
Form + busca aluno + textarea resposta + anexo opcional
```""",
    "Preview notificação aluno",
    "`DS/Combobox`, `DS/Textarea`, `DS/FileDropzone`",
    "Submit success",
    "1 coluna",
    "Labels",
    "N/A",
    "Form page.",
))

SCREENS.append(list_screen("F5.14", "/secretaria/eventos", "Eventos — Lista (secretaria)", "F5",
    "event.manage", "`GET/POST /events`", "CRUD institucional de eventos — reutiliza componentes F3.2a, escopo cursos vinculados.",
    "Título, Período, Modo, Estado, Organizador", "onlyMine, curso, estado", "Novo evento"))

SCREENS.append(spec("F5.15", "/secretaria/eventos/:id/operacao", "Operação evento (secretaria)", "F5", "AppLayout", "Web", "P2",
    "event.host", "igual F3.2c",
    "Paridade funcional com F3.2c — reutilizar mesmo frame.",
    "Instância de F3.2c",
    "Idêntico",
    "F3.2c",
    "Idêntico",
    "Idêntico",
    "Idêntico",
    "Idêntico",
    "Reutilizar F3.2c — anotar contexto secretaria.",
))

SCREENS.append(spec("F5.16", "/secretaria/importacoes", "Importações", "F5", "AppLayout", "Web", "P2",
    "import.run", "`POST /imports/{kind}`",
    "Wizard: modelo → upload → preview validação → confirmar → relatório.",
    """```
Wizard 4 passos + tabela erros por linha
```""",
    "Linhas inválidas highlight danger",
    "`DS/WizardStepper`, `DS/FileDropzone`, `DS/DataTable`",
    "Por passo + relatório final",
    "Desktop",
    "Erros com aria-describedby",
    "N/A",
    "Wizard com step preview.",
))

SCREENS.append(spec("F5.17", "/secretaria/exportacoes", "Exportações", "F5", "AppLayout", "Web", "P2",
    "export.run", "`POST /exports/{kind}`",
    "Catálogo exportações com geração assíncrona.",
    """```
Grid cards export kind + histórico downloads
```""",
    "Status: processando, pronto, expirado",
    "`DS/Card`, `DS/Badge`",
    "Async job polling",
    "Grid 3 col",
    "Status live region",
    "Download via `_links`",
    "Card grid pattern.",
))

SCREENS.append(spec("F5.18", "/secretaria/estatisticas", "Estatísticas", "F5", "AppLayout", "Web", "P2",
    "report.view_secretary", "`GET /reports/secretary`",
    "Dashboards quantitativos Recharts.",
    """```
  ├─ FilterBar período/curso
  ├─ Grid charts 2×2 (bar, line, pie)
  └─ Tabela drill-down
```""",
    "Cores charts via tokens brand/status",
    "`DS/Chart` wrappers",
    "Loading skeleton charts",
    "Charts empilham mobile",
    "Charts com texto alternativo resumo",
    "N/A",
    "Placeholder charts com dados demo.",
))

SCREENS.append(spec("F5.19", "/secretaria/tarefas", "Tarefas internas", "F5", "AppLayout", "Web", "P3 opcional",
    "task.manage", "`/tasks` CRUD",
    "Kanban simples pendente/concluída.",
    """```
  ├─ Tabs ou Kanban 2 colunas
  └─ Modal nova tarefa
```""",
    "Opcional — flag config",
    "`DS/Card`, Kanban",
    "Standard",
    "Kanban scroll mobile",
    "Drag keyboard alt",
    "N/A",
    "Opcional MVP.",
))

# F6
SCREENS.append(spec("F6.1", "/coordenacao/cursos/:id/configurar", "Configurar curso", "F6", "AppLayout", "Web", "P2",
    "course.config", "`PATCH /courses/{id}/config`",
    "Horas formativas mínimas, calendário 15/18, banca TCC.",
    """```
Form seções em Cards
```""",
    "Subform regimento",
    "`DS/Input`, `DS/Card`",
    "Save states",
    "Desktop",
    "Standard",
    "N/A",
    "Settings form.",
))

SCREENS.append(spec("F6.2", "/coordenacao/relatorios", "Relatórios coordenação", "F6", "AppLayout", "Web", "P2",
    "report.view_coordinator", "`GET /reports/coordinator`",
    "Análise evasão, formativas, comparativos.",
    "Extensão F5.18 com séries históricas",
    "Mais charts comparativos",
    "F5.18 components",
    "Igual F5.18",
    "Igual F5.18",
    "Igual F5.18",
    "N/A",
    "Derivar F5.18.",
))

# F7 Admin
admin_crud = [
    ("F7.1", "/admin/usuarios", "Usuários", "user.manage_all", "CRUD qualquer usuário + roles"),
    ("F7.2", "/admin/perfis", "Perfis (Roles)", "iam.manage_roles", "CRUD roles + authorities"),
    ("F7.3", "/admin/autoridades", "Autoridades", "iam.manage_authorities", "CRUD + matriz role×authority"),
]
for code, route, title, cap, desc in admin_crud:
    SCREENS.append(list_screen(code, route, title, "F7", cap, f"`{route}`",
        desc, "Nome, Descrição, ...", "busca", "Novo"))

SCREENS.append(spec("F7.4", "/admin/tipos-solicitacao", "Tipos de solicitação", "F7", "AdminLayout", "Web", "P2",
    "request_type.manage", "`/request-types` CRUD",
    "Editor JSON Schema + preview form + editor workflow state machine.",
    """```
Split 3 painéis:
  Lista tipos | Editor JSON | Preview DynamicForm + Workflow graph
```""",
    "Workflow: nodes estados, edges transições",
    "`DS/CodeEditor`, `WorkflowCanvas`, `FormPreview`",
    "Invalid schema highlight",
    "Desktop wide 1440px+",
    "Code editor accessible",
    "N/A",
    "**Tela crítica ADR-003.**",
))

SCREENS.append(spec("F7.5", "/admin/templates-comunicacao", "Templates comunicação", "F7", "AdminLayout", "Web", "P2",
    "communication.manage_templates", "`/communication-templates`",
    "CRUD Markdown + placeholders + versionamento.",
    "Editor split + histórico versões sidebar",
    "Preview com variáveis substituídas",
    "`MarkdownEditor`, `DS/DataTable` versions",
    "Version diff",
    "Desktop",
    "Standard",
    "N/A",
    "Editor pattern.",
))

SCREENS.append(spec("F7.6", "/admin/jobs", "Jobs / Outbox", "F7", "AdminLayout", "Web", "P2",
    "system.observe", "`/admin/outbox`",
    "Outbox PENDING/SENT/FAILED + retry + scheduled jobs.",
    """```
  ├─ Tabs status Outbox
  ├─ DataTable eventos + botão Reentregar
  └─ Seção scheduled jobs
```""",
    "FAILED em danger; DEAD em neutral",
    "`DS/DataTable`, `DS/Badge`, `DS/Button`",
    "Retry loading",
    "Desktop",
    "Status badges",
    "retry via `_links`",
    "Ops dashboard.",
))

SCREENS.append(spec("F7.7", "/admin/audit-log", "Audit log", "F7", "AdminLayout", "Web", "P2",
    "audit.read", "`/audit-log`",
    "Pesquisa audit_log com diff antes/depois.",
    """```
  ├─ Filtros avançados
  ├─ DataTable eventos
  └─ Drawer diff (JSON side-by-side)
```""",
    "Diff em `surface/code` mono",
    "`DS/Drawer`, `DiffViewer`",
    "Search, detail",
    "Drawer full mobile",
    "Diff readable",
    "N/A",
    "Drawer diff pattern.",
))

SCREENS.append(spec("F7.8", "/admin/usuarios/:id/reset-senha", "Reset senha admin", "F7", "AdminLayout", "Web", "P2",
    "user.reset_password", "`POST /users/{id}/password-reset`",
    "Confirma envio link 1-uso — operador nunca vê senha.",
    """```
Modal confirmação + AlertBanner info após sucesso
```""",
    "Sem campo senha",
    "`DS/Modal`, `DS/AlertBanner`",
    "Confirm, Success",
    "Modal centered",
    "Focus trap",
    "N/A",
    "Modal only.",
))

SCREENS.append(spec("F7.9", "/admin/sistema/saude", "Saúde do sistema", "F7", "AdminLayout", "Web", "P3",
    "system.admin", "Actuator",
    "Métricas latência, Outbox, 5xx + link Grafana.",
    """```
  ├─ KpiRow métricas
  └─ Links externos Grafana
```""",
    "Extra-MVP",
    "KpiCard",
    "Live polling",
    "Desktop",
    "Standard",
    "N/A",
    "Opcional.",
))

# F8
SCREENS.append(spec("F8.1", "/buscar?q=", "Busca global", "F8", "AppLayout / Modal", "Web + Mobile", "P2",
    "escopo derivado", "`GET /search?q=`",
    "Command palette Ctrl+K — resultados tipados.",
    """```
Modal overlay (max-w 640px):
  ├─ Input busca grande
  ├─ Resultados agrupados por tipo (ícone + label)
  └─ Atalhos teclado ↑↓ Enter
```""",
    "Abrir via Topbar SearchBar",
    "`DS/CommandPalette`",
    "Empty, Loading, Results",
    "Full screen mobile",
    "Combobox pattern aria",
    "N/A",
    "Component CommandPalette.",
))

SCREENS.append(spec("F8.2", "/suporte", "Suporte", "F8", "AppLayout", "Web + Mobile", "P2",
    "logado", "ticket/FAQ",
    "Abrir ticket + FAQ base conhecimento.",
    """```
  ├─ Accordion FAQ
  └─ Form novo ticket
```""",
    "FAQ categorizado",
    "`DS/Accordion`, `DS/Textarea`",
    "Submit ticket",
    "Standard",
    "Accordion keyboard",
    "N/A",
    "Support page.",
))

# Write conventions
CONVENTIONS = """# Convenções globais — Especificações Figma

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
"""

FILENAMES = [
        "F0.1-login.md", "F0.2-recuperar-senha.md", "F0.3-nova-senha.md",
        "F0.4-contato.md", "F0.5-erro.md", "F0.6-verificar-protocolo.md",
        "F0.7-verificar-certificado.md",
        "F1.1-inicio-aluno.md", "F1.2-primeiro-acesso.md", "F1.3-perfil.md",
        "F1.4-perfil-seguranca.md", "F1.5-perfil-notificacoes.md",
        "F1.6-comunicacao.md", "F1.7-solicitacoes-lista.md",
        "F1.8-solicitacoes-nova.md", "F1.9-solicitacoes-detalhe.md",
        "F1.10-formativas-lista.md", "F1.11-formativas-nova.md",
        "F1.12-formativas-detalhe.md", "F1.13-estagios-lista.md",
        "F1.14-estagios-detalhe.md", "F1.15-tccs-lista.md",
        "F1.16-tccs-detalhe.md", "F1.17-eventos-lista.md",
        "F1.18-eventos-presenca.md", "F1.19-certificados.md",
        "F1.20-meus-atendimentos.md",
        "F2.1-egresso-inicio.md",
        "F3.1-inicio-professor.md",
        "F3.2-professor-eventos-lista.md",
        "F3.2-professor-eventos-detalhe.md",
        "F3.2-professor-eventos-operacao.md",
        "F3.3-solicitacoes-deliberar-fila.md",
        "F3.4-solicitacoes-deliberar.md",
        "F3.5-formativas-revisao.md",
        "F3.6-estagios-revisao.md",
        "F3.7-tccs-revisao.md",
        "F3.8-comunicacao-publicar.md",
        "F4.1-comissoes-caaf.md", "F4.2-comissoes-coe.md",
        "F5.1-inicio-secretaria.md", "F5.2-solicitacoes-fila.md",
        "F5.3-solicitacoes-nova-interna.md", "F5.4-solicitacoes-deliberar-secretaria.md",
        "F5.5-secretaria-atrasados.md", "F5.6-secretaria-alunos.md",
        "F5.7-secretaria-cursos.md", "F5.8-secretaria-disciplinas.md",
        "F5.9-secretaria-calendarios.md", "F5.10-secretaria-egressos.md",
        "F5.11-secretaria-diplomas.md", "F5.12-secretaria-autorizacoes-imagem.md",
        "F5.13-secretaria-atendimentos.md", "F5.14-secretaria-eventos-lista.md",
        "F5.15-secretaria-eventos-operacao.md", "F5.16-secretaria-importacoes.md",
        "F5.17-secretaria-exportacoes.md", "F5.18-secretaria-estatisticas.md",
        "F5.19-secretaria-tarefas.md",
        "F6.1-coordenacao-configurar-curso.md", "F6.2-coordenacao-relatorios.md",
        "F7.1-admin-usuarios.md", "F7.2-admin-perfis.md",
        "F7.3-admin-autoridades.md", "F7.4-admin-tipos-solicitacao.md",
        "F7.5-admin-templates-comunicacao.md", "F7.6-admin-jobs.md",
        "F7.7-admin-audit-log.md", "F7.8-admin-reset-senha.md",
        "F7.9-admin-sistema-saude.md",
        "F8.1-buscar.md", "F8.2-suporte.md",
]


def main():
    (OUT / "00-CONVENCOES.md").write_text(CONVENTIONS, encoding="utf-8")
    print("Wrote 00-CONVENCOES.md")

    if len(SCREENS) != len(FILENAMES):
        raise SystemExit(f"Count mismatch: SCREENS={len(SCREENS)} FILENAMES={len(FILENAMES)}")

    for fname, content in zip(FILENAMES, SCREENS):
        dest = screen_out_path(fname)
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(content, encoding="utf-8")
        print(f"Wrote {dest.relative_to(OUT)}")

    print(f"Total: {len(FILENAMES)} screen specs + conventions")


if __name__ == "__main__":
    main()

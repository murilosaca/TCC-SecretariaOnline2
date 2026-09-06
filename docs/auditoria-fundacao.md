# Auditoria de fundação — SO2

Data: 2026-09-06. Fontes: `docs/tcc-docs.md`, `docs/telas-figma.md`, código e `.cursorrules`.

## Correções aplicadas nesta auditoria

- `.cursorrules` e `.cursor/rules/` passam a citar `docs/` como fonte de verdade,
  stack oficial Kotlin+React, rotas Figma, P0, Proof of Stay, BFF e a proibição
  de Lombok (decisão do time; a spec Kotlin não usa Lombok).
- `tcc-docs.md` e o mapa de telas ficam em `docs/`. O README aponta para eles.
- Conteúdo trocado de `F5.9` (calendários) e `F5.10` (egressos) foi corrigido.
- Backend: API de `periodo_letivo` (RF-F5-004-c) com validação de semestre,
  intervalo e sobreposição, mais `GET /academico/periodos/vigente`.
- Frontend React: `AuthLayout` / `AppLayout`, pastas por fluxo e rotas Figma
  (`/login`, `/secretaria/*`, `/inicio`). Rotas planas antigas redirecionam.

## IAM (sprint P0)

Módulo `modules.iam` implementado: `usuario` + `usuario_authority` (`dominio.acao`),
refresh opaco, histórico de senha, JTI blacklist, `outbox_event` mínimo e `audit_log`
append-only. Login JWT RS256 15 min; cookie `so2_refresh` (`httpOnly; Secure; SameSite=Lax;
Path=/auth`); Argon2id; primeiro acesso (RF-F1-002); recuperação via Outbox (sem e-mail
síncrono). Telas React `/login`, `/recuperar-senha`, `/nova-senha`, `/primeiro-acesso`
ligadas. Access token só em memória.

## Plano — `@PreAuthorize` no CRUD acadêmico

O CRUD `/academico/**` permanece `permitAll` (sem `@PreAuthorize`) para não quebrar a
fundação: ITs, telas de secretaria e bootstrap ainda não têm matriz FGAC (F7) nem
vínculo usuário↔curso. Quando F7 existir:

1. Fechar `anyRequest()` para `authenticated()` (exceto F0 público e `/auth/*` anônimos).
2. Anotar comandos acadêmicos com capabilities (`curso.manage`, `student.manage`,
   `calendar.manage`) — nunca `hasRole`.
3. Continuar emitindo `_links` HATEOAS; a UI segue cega a perfil (`useActions`).
4. Escopo por curso (coordenação/secretaria) entra com a tabela N:N de secretários.

## Discrepâncias que permanecem (dívida consciente)

| Item | Spec | Situação | Ação |
|---|---|---|---|
| Linguagem do backend | Kotlin + Kotest + MockK | Java 21 + JUnit 5 | Migrar sem mudar contratos |
| Motor de solicitações | RequestType + workflow | Inexistente | Depois do IAM |
| Secretários do curso | RF-F5-004-a | Só `idCoordenador` + horas | Tabela N:N quando houver cadastro de usuários |
| Config F6.1 | calendário, banca, regimento | Fora do CRUD de secretaria | Módulo coordenação |
| Eventos de calendário | tipos semânticos em F5.9 | Só período letivo | Segunda aba quando o schema existir |
| ArchUnit | regras de dependência | Não há teste | Adicionar no próximo módulo |
| Ports com `Pageable` | domain/application puros | Ports importam Spring Data | Extrair paginações próprias na migração Kotlin |
| Angular em `frontend/` | React oficial | Pasta legado travada | Apagar quando o `ng serve` soltar |
| Dispatcher de e-mail | Outbox → SMTP | Eventos ficam `PENDING` | Módulo comunicação / Outbox |
| Bucket4j + Redis | RNF-SEC-04 | Janela em memória no processo | Trocar quando houver Redis |
| Cobertura 85/70/75 | RNF de testes | Ampliar com IAM | Continuar nos módulos seguintes |

Lombok: **conforme**. Zero ocorrências. Proibição registrada nas rules.
Aluno sem `idade`: **conforme** (RF-F5-003).
HATEOAS + `useActions`: **conforme** nas telas de CRUD.
IAM / JWT / Argon2id: **entregue neste sprint** (CRUD acadêmico ainda sem FGAC).

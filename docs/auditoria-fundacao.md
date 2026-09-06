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

## Discrepâncias que permanecem (dívida consciente)

| Item | Spec | Situação | Ação |
|---|---|---|---|
| Linguagem do backend | Kotlin + Kotest + MockK | Java 21 + JUnit 5 | Migrar sem mudar contratos |
| IAM / JWT / Argon2id | P0 — RF-F0-001..003, RF-F1-002 | Telas stub; `SecurityConfig` em `permitAll` | Próximo sprint (`modules.iam`) |
| Motor de solicitações | RequestType + workflow | Inexistente | Depois do IAM |
| Secretários do curso | RF-F5-004-a | Só `idCoordenador` + horas | Tabela N:N quando houver cadastro de usuários |
| Config F6.1 | calendário, banca, regimento | Fora do CRUD de secretaria | Módulo coordenação |
| Eventos de calendário | tipos semânticos em F5.9 | Só período letivo | Segunda aba quando o schema existir |
| ArchUnit | regras de dependência | Não há teste | Adicionar com o primeiro módulo extra |
| Ports com `Pageable` | domain/application puros | Ports importam Spring Data | Extrair paginações próprias na migração Kotlin |
| Angular em `frontend/` | React oficial | Pasta legado travada | Apagar quando o `ng serve` soltar |
| Cobertura 85/70/75 | RNF de testes | Abaixo da meta | Ampliar com IAM e domínio |

Lombok: **conforme**. Zero ocorrências. Proibição registrada nas rules.
Aluno sem `idade`: **conforme** (RF-F5-003).
HATEOAS + `useActions`: **conforme** nas telas de CRUD.

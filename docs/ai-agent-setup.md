# AI Coding-Agent Setup

This starter includes repository-level setup for AI-assisted coding. It is not a runtime AI backend module.

The goal is to make cloned projects easier to work on with AI coding tools while reducing hallucinated architecture, fake endpoints, unsafe security changes, and missing tests.

## Included Agent Files

- `AGENTS.md`
- `CLAUDE.md`
- `.cursor/rules/api-starter.mdc`
- `.github/copilot-instructions.md`
- `.windsurfrules`
- `.clinerules`

## Source Of Truth

`AGENTS.md` is the main source of truth.

Other agent-specific files should stay short and point back to `AGENTS.md` where possible.

## Agent Workflow

Recommended workflow for any AI coding agent:

1. Read `AGENTS.md`.
2. Read `docs/plan.md`.
3. Read the relevant module doc, for example `docs/auth.md` or `docs/hmac.md`.
4. Inspect existing code before editing.
5. Keep changes scoped to the requested feature.
6. Add or update tests for behavior changes.
7. Run:

```powershell
.\gradlew.bat test
.\gradlew.bat check
```

8. Update docs when endpoint behavior, env config, schema, or security behavior changes.

## Architecture Guardrails

Agents should preserve these project decisions:

- Kotlin + Spring Boot 3.
- REST JSON API by default.
- PostgreSQL with Flyway migrations.
- Spring Data JPA.
- Spring Security.
- JWT access tokens and hashed refresh tokens.
- Optional OAuth, HMAC, and AES-GCM modules.
- No runtime AI module unless a downstream app explicitly needs one.
- No secrets in source control.

## Anti-Hallucination Rules

Agents must not:

- invent endpoints without adding controllers and docs
- invent database columns without Flyway migrations
- bypass Spring Security for protected APIs
- store raw refresh tokens
- trust OAuth profile data submitted directly by a frontend
- add runtime AI/provider SDK dependencies unless explicitly requested
- lower coverage thresholds to hide missing tests
- remove security checks to make tests pass

## When Cloning This Starter

For the full flow, see [Clone and start a new project](clone-and-start.md).

After cloning for a real project:

1. Rename package/base namespace if needed.
2. Update `README.md` with the project name.
3. Update `AGENTS.md` with project-specific domain rules.
4. Keep the existing security/testing rules unless there is a documented reason to change them.
5. Add project-specific examples to `docs/`.

## Supported Tools

The setup is intentionally plain-text and tool-agnostic.

Known supported workflows:

- Codex-style agents: `AGENTS.md`
- Claude-style agents: `CLAUDE.md`
- Cursor: `.cursor/rules/*.mdc`
- GitHub Copilot: `.github/copilot-instructions.md`
- Windsurf: `.windsurfrules`
- Cline/Roo-style agents: `.clinerules`

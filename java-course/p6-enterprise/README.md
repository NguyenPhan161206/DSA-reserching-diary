# Part VI — Enterprise Survey (Ch 37–41)

**Gate:** G6 · **Messages:** M4 · **Size:** T (half a page per chapter)
**Plan of record:** [`../00-roadmap.md`](../00-roadmap.md) §3

These five chapters are the most dated in the book. They are kept — but as a **survey
with a verdict**, not as material to memorise. Every card is one page and ends with two
mandatory questions:

1. **Is this still true in 2026?** — what the ecosystem actually looks like now.
2. **If starting today, what would I use instead, and why?**

| Ch | Title | Verdict to research |
|----|-------|---------------------|
| 37 | Servlets | still the standard HTTP-in-JVM API; `javax.*` → `jakarta.*` |
| 38 | JSP | legacy; survived in maintenance, not in new projects |
| 39 | JSF | effectively dead — replaced by component libraries and JS frameworks |
| 40 | RMI | niche; the idea survives in gRPC / HTTP RPC / service meshes |
| 41 | Web Services | SOAP is legacy; JAX-RS became **Jakarta REST**, RESTful is the default |

**Gate G6:** answer *"REST or Servlet — when would you pick each?"* with a reason, not a
preference.

## Why keep them at all

Because the day something is called "legacy" is the day nobody can reason about the code
still running in production. And because the *shape* — a container that owns a lifecycle,
a request/response boundary, a session, a serialisation format — is exactly the M4 lesson
that survives every framework rewrite.

---
*Legend: ⬜ Not started · 🟡 In progress · 🟢 Done*

# Part V — Advanced & Systems (Ch 32–36, 44)

**Gate:** G5 · **Messages:** M1, M2, M4, **M6**
**Plan of record:** [`../00-roadmap.md`](../00-roadmap.md) §3

Where the program stops being single-threaded, single-machine and single-file. The
unifying idea is M4: everything here is a **boundary** — threads, sockets, databases,
locales — and boundaries fail in ways your code cannot predict.

| Ch | Title | Msg | Sz | Status |
|----|-------|-----|----|--------|
| 32 | Multithreading and Parallel Programming | M1, M4 | L | ⬜ |
| 33 | Networking | M4 | M | ⬜ |
| 34 | Database Programming (JDBC) | M4 | M | ⬜ |
| 35 | Transactions and Metadata | M4 | S | ⬜ |
| 36 | Internationalization | M2 | S | ⬜ |
| 44 | Testing with JUnit | **M6** | M | ⬜ |

## Read Ch 32 against Java 21, not against the book

The chapter is built on thread pools and `synchronized`. **Virtual threads**
(`Thread.ofVirtual()`, structured concurrency via `StructuredTaskScope`) invalidate much
of the pool material for the common blocking workload. The card is not "repeat the
chapter" — it is:

- which parts survive (race conditions, memory visibility, deadlock — all still real),
- which parts evaporate (pool sizing, `ThreadPoolExecutor` as a default),
- which parts got *harder* (thread dumps no longer identify which task is stuck, because
  there is no thread per task).

## Ch 44 was already started

[`../00-toolchain/junit-demo/`](../00-toolchain/junit-demo/) is a working JUnit 5
project that has been seen red (11 failures on an off-by-one) and then green. Ch 44
extends it: `@Nested`, `@ParameterizedTest` design, timeout tests, and the rules for what
is worth asserting.

**Gate G5:** provoke a real deadlock and fix it with `Lock`; a JDBC rollback test passes.

---
*Legend: ⬜ Not started · 🟡 In progress · 🟢 Done*

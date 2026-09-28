# AGENTS.md — DSA-Researching-Diary

> Companion to [reaserching-diary](https://github.com/NguyenPhan161206/reaserching-diary) (config v3.3).
> This repo inherits the **spirit** of that repo's `AGENTS.md` (English content, depth standards, cross-referencing, push-on-command-only) but is specialised for **code + write-ups**.

## SECTION A: IDENTITY
- **Repo purpose:** DSA learning through code. Code, per-problem write-ups, practice logs, error journal.
- **Sibling repo:** [reaserching-diary](https://github.com/NguyenPhan161206/reaserching-diary) holds theory, roadmap, mental models, and technology notes. Roadmap of record: `dev_foundation/dsa/01-learning-roadmap.md`.
- **Language policy:** ALL file content in **English**. Chat with the user may be Vietnamese or English.
- **Depth standard:** explain the *derivation*, not the outcome. A write-up that says "used BFS to find shortest path" is incomplete; one that explains why BFS (not DFS) gives shortest paths in an unweighted graph is complete.

## SECTION B: WHAT LIVES WHERE
| Content | Location | Never |
|---|---|---|
| Solution source code | `solutions/<topic>/<NN-problem-name>/` | Solution code in the diary repo |
| Problem write-up | `approach.md` in the problem folder | A solution folder without a write-up |
| LeetCode/Codeforces/PTIT notes | `problems/` | — |
| Timed session logs | `practice/` | — |
| Session reflections, error journal | `journal/` | — |
| Java course track (Liang, Ch 1–44) | `java-course/` | A chapter summary in `notes/` |
| Theory, roadmap, tooling notes | **reaserching-diary** `dev_foundation/dsa/` | Duplicating roadmap content here |

**Split rule:** if it needs *code* to be understood → here. If it needs *understanding* to be understood → diary.

**The `java-course/` exception:** this repo is C++/Python-first for DSA, but the course
textbook is Java. `java-course/` is a self-contained second track with its own rules
(Section J) — it does not change the rules for `solutions/`.

## SECTION C: NAMING CONVENTIONS
- Folders: `NN-topic-name/` (2-digit prefix = learning order, chronological)
- Solution folders: `NN-kebab-case-problem-name/`
- Files: `approach.md`, `solution.cpp`, `solution.py` (fixed names — the agent and human both rely on them)
- Commits: `[topic] add: problem-name (O(n log n) time, O(n) space)` / `[fix] correct: ...` / `[journal] update: ...` / `[java] chNN: chapter-title (M-codes)`

## SECTION D: WRITE-UP QUALITY CHECKLIST
Before a solution is considered done:
- [ ] Problem restated in one sentence (inputs, outputs, constraints)
- [ ] Constraints quoted — they justify the algorithm
- [ ] Brute force written and justified (why it is too slow)
- [ ] Improvement derived, not recalled: which property of the input is exploited
- [ ] **Correctness argument** (not just "it works") — invariant, induction, or greedy exchange argument
- [ ] Complexity $O$ time and space stated, with the reasoning for the dominant term
- [ ] Code compiles and passes (self-tested before commit)
- [ ] At least one non-obvious test case written down, with the expected reason
- [ ] "What I got wrong" section — mandatory, even if it is "thought too hard"
- [ ] 🔗 Diary link when the problem connects to a note in reaserching-diary (data structure → its note)

## SECTION E: SOLUTION CODE RULES
- Readable over clever. Comments explain *why*, not *what*.
- Follow the language's idioms (C++: `bits/stdc++.h`, `using namespace std;`, competitive I/O).
- Prefer clear variables over brevity; this code is read again in six months.
- No global state unless the problem demands it.
- Include the platform/problem id in a header comment when known.

## SECTION F: AUTONOMY LEVELS
- **AUTO:** read, list, run tests, compile, `git status`/`log`/`diff`, create problem folders, log practice sessions
- **SUGGEST:** write `approach.md`, draft solution code, update README index tables
- **ASK:** commit, push, create a remote, rename/move a problem folder, delete a solution, change a message-family mapping in `java-course/00-roadmap.md`
- **BLOCK:** pushing secrets, rewriting history, force-push, bulk-deleting solutions

## SECTION G: PUSH POLICY
**Push only on an explicit user command** ("push", "lưu và push", "commit and push"), and then batch **all** new/uncommitted work in one go — never one commit per problem across multiple sessions. Before pushing: run `git status` + `git diff`, scan for secrets and stray binaries, and verify every new solution folder has an `approach.md`.

## SECTION H: AGENT CROSS-REPO LINKING
When a solution or journal entry relates to a theory note, link it with the standard format:
```markdown
**Concept note:** [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)
```
And in the diary, the mirror link is the `🔗 [DSA-reserching-diary](...)` entry (AGENTS.md Section L there).

## SECTION I: POST-ACTION CHECKLIST
After every change in this repo:
1. Update `solutions/README.md` index table with the new problem
2. Update `practice/README.md` if it was a practice session
3. Update `journal/README.md` if a reflection or error was recorded
4. Update the repo README progress table if a phase advanced
5. If a phase/topic changed status, tell the user the diary roadmap may need a sync

## SECTION J: JAVA COURSE TRACK (`java-course/`)
**Book:** *Introduction to Java Programming and Data Structures, Comprehensive Version* —
Y. Daniel Liang. **Target:** Java 21 (LTS). **Plan of record:** `java-course/00-roadmap.md`.

**The 6 message families** — every chapter maps to at least one, and a chapter with no
mapping is an unfinished plan:

| Code | Message |
|------|---------|
| M1 | Every line of code has a cost, and the runtime is a program you can read |
| M2 | Types are a tool that moves errors earlier — paid for in boilerplate and erasure |
| M3 | Complexity is the real spec; every structure trades one operation for another |
| M4 | Failure is normal; model the boundary (exceptions, I/O, network, DB) |
| M5 | Abstraction is a ladder — know which rung you are standing on |
| M6 | You do not know it is right until a test says so (cross-cutting) |

**Per-chapter artefact set** — exactly three, no more:
```
p1-fundamentals/ch02-elementary-programming/
├── message-card.md     # REQUIRED. 7 sections, template in TEMPLATE-MESSAGE-CARD.md
├── exercises.md        # L1 recall / L2 apply / L3 derive + 5 closed-book questions
└── lab/                # 1–3 runnable .java files, compiled and self-tested
```

**Rules:**
- **Never copy the book.** Message cards are assertions you *derived*, not translated
  prose. At most one short quote, with a section reference. Code is rewritten from
  scratch, minimal enough to be understood.
- **Every message needs a counter-example.** A message no example can break is not
  finished.
- **Every chapter names at least one thing the book does not say out loud** — that is
  usually where the value is.
- **`🟢` requires the lab to have been compiled and run**, with the output pasted into
  the card. Unrun code is `🟡` at best.
- `🆕 Java 21 delta vs sách` is a mandatory section, not optional.
- **Boundary rule (revised 2026-09-28):** a book exercise is **not** automatically a
  `solutions/` entry. Book exercises stay in `exercises.md`, and the **L3 derive tier
  must carry the full Section D analysis inline** (brute force, derivation, correctness
  argument, complexity with reasoning, test table, what I got wrong). A problem is
  **promoted** to `solutions/<topic>/<NN-name>/approach.md` only when it is actually
  submitted to a judge or becomes a self-authored DSA problem worth reusing — and then
  both files link to each other. `solutions/` is the C++/Python judged track; do not put
  a Java-only exercise there under a `solution.cpp` filename.
- **Toolchain:** `source java-course/00-toolchain/env.sh`, then
  `./java-course/00-toolchain/run.sh <file.java>` to compile and run any lab. A `.class`
  file in the repo is a defect.
- **Progress is measured by gates passed**, not chapters read.

**Commit format:** `[java] chNN: chapter-title (M1, M3)`

---
*Config version: 1.0 — created 2026-09-28 alongside the diary's config v3.3.*

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
| Theory, roadmap, tooling notes | **reaserching-diary** `dev_foundation/dsa/` | Duplicating roadmap content here |

**Split rule:** if it needs *code* to be understood → here. If it needs *understanding* to be understood → diary.

## SECTION C: NAMING CONVENTIONS
- Folders: `NN-topic-name/` (2-digit prefix = learning order, chronological)
- Solution folders: `NN-kebab-case-problem-name/`
- Files: `approach.md`, `solution.cpp`, `solution.py` (fixed names — the agent and human both rely on them)
- Commits: `[topic] add: problem-name (O(n log n) time, O(n) space)` / `[fix] correct: ...` / `[journal] update: ...`

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
- **ASK:** commit, push, create a remote, rename/move a problem folder, delete a solution
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

---
*Config version: 1.0 — created 2026-09-28 alongside the diary's config v3.3.*

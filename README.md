# DSA-reserching-diary

> **Knowledge base:** [reaserching-diary](https://github.com/NguyenPhan161206/reaserching-diary) — theory, roadmap & technology notes live there. This repo holds the **code** and the **per-problem write-ups**.

Personal repository for learning Data Structures & Algorithms by *writing code* — solutions, explanations, practice logs, and an error journal. The theory roadmap and cross-technology notes live in the companion diary repo; this repo is the executable side of that plan.

---

## 🎯 Why this repo is separate

| Concern | Repo |
|---------|------|
| Roadmap, mental models, complexity theory, CLI/tooling notes, reflections | [reaserching-diary](https://github.com/NguyenPhan161206/reaserching-diary) → `dev_foundation/dsa/` |
| **Source code, per-problem write-ups, practice logs, error journal** | **this repo** |

Keeping code out of the diary means the diary stays a fast, readable knowledge base, and this repo stays a clean, buildable project you can clone and run in one command.

---

## 📁 Structure

```
DSA-reserching-diary/
├── solutions/          # self-authored problems, one folder per topic, one folder per problem
│   ├── 01-fundamentals/            arrays, strings, hashing, two pointers
│   ├── 02-linear-structures/       stack, queue, linked list, monotonic structures
│   ├── 03-trees/                   traversal, BST, heap, trie
│   ├── 04-graphs/                  BFS/DFS, topo sort, union-find, shortest path
│   ├── 05-binary-search/           on the answer, on the predicate
│   ├── 06-sorting/                 counting, comparison, custom comparators
│   ├── 07-dynamic-programming/     1D, 2D, knapsack, LIS/LCS, interval
│   ├── 08-greedy/                  greedy + exchange argument proofs
│   ├── 09-backtracking/            recursion with pruning
│   └── 10-math-number-theory/      number theory, bit manipulation, strings (KMP/Z)
├── problems/           # LeetCode / Codeforces / PTIT / VNOI problem notes
├── practice/           # timed sessions, contest logs, weak-spot tracking
├── journal/            # session reflections, aha moments, error journal
├── notes/              # scratch space, cross-topic notes, experiments
└── java-course/        # second track: Liang's Java book, Ch 1–44 (Java 21)
```

**Language strategy:** C++ as primary (judges and interviews judge algorithmic intent), Python as secondary (fast prototyping, and the language of the AI stack). Same idea, two notations.

**Per-problem folder convention:**

```
solutions/03-trees/07-binary-tree-level-order-traversal/
├── approach.md      # the write-up (see NOTE_TEMPLATE.md → TEMPLATE DSA-1)
├── solution.cpp     # primary implementation
└── solution.py      # optional secondary implementation
```

---

## 🗺️ Roadmap

The authoritative roadmap — phases, exit criteria, progress tracker, and platform mix — lives in the diary:

**📍 [dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)**

Short version:

| Phase | Topic | Status |
|-------|-------|--------|
| 0 | Tooling (compile, run, time, debug, Git per solution) | ⬜ |
| 1 | Fundamentals (arrays, strings, two pointers, sliding window, hash maps) | ⬜ |
| 2 | Linear structures (stack, queue, monotonic, prefix sums) | ⬜ |
| 3 | Trees (DFS/BFS, BST, heap, trie) | ⬜ |
| 4 | Graphs (traversals, topo sort, union-find, shortest path) | ⬜ |
| 5 | Dynamic programming | ⬜ |
| 6 | Advanced (greedy, backtracking, bit tricks, number theory, segment trees) | ⬜ |

**Weekly target:** 5–8 problems across 2 sessions, at least 2 solved without hints.

---

## ☕ The Java course track — `java-course/`

A second, self-contained track whose textbook is Java: *Introduction to Java Programming
and Data Structures* (Y. Daniel Liang), 44 chapters, restructured around **6 message
families** instead of 44 summaries.

| | |
|---|---|
| **Plan of record** | [`java-course/00-roadmap.md`](java-course/00-roadmap.md) — 44/44 chapters mapped, 7 gates |
| **Start here** | [`java-course/00-toolchain/README.md`](java-course/00-toolchain/README.md) ✅ — measured, not asserted |
| **Card format** | [`java-course/TEMPLATE-MESSAGE-CARD.md`](java-course/TEMPLATE-MESSAGE-CARD.md) |
| **Rules** | [`AGENTS.md` §J](AGENTS.md) |
| **Status** | G0 ✅ · Part I 🟢 8/8 (audits + teach-backs closed) · **G1 ⏳ next** — 5 exercises from Ch 2/4/5/7, closed-book |

```bash
source java-course/00-toolchain/env.sh                              # JDK 21 + Maven on PATH
./java-course/00-toolchain/run.sh java-course/00-toolchain/lab/InputBenchmark.java
```

It does not replace `solutions/`. Part I exercises stay in each chapter's
`exercises.md`, with the full Section D analysis (brute force, derivation, correctness,
complexity, test table, what I got wrong) written inline in the L3 tier. A problem moves
to `solutions/` only when it is actually submitted to a judge or becomes a self-authored
DSA problem worth reusing — then both files link to each other. `solutions/` is the
C++/Python judged track, so a Java-only exercise is never placed there.

---

## 📏 Rules of the repo

1. **No solution is committed without a write-up.** Code without an explanation is not finished.
2. **Always state complexity before running.** $O$ time and space go in `approach.md` before the first test.
3. **Brute force first.** Keep the naive version in the write-up. The derivation is the value.
4. **Errors go in the journal.** What went wrong, why, and the smallest test that reproduces it.
5. **One commit per problem**, message format: `[topic] add: problem-name (complexity)`.
6. **Never commit binaries or IDE folders** (see `.gitignore`).
7. **Push only when explicitly told to** — the same policy as the diary repo.

---

## 🔗 Related
- 📖 [reaserching-diary/dev_foundation/dsa/](https://github.com/NguyenPhan161206/reaserching-diary/tree/main/dev_foundation/dsa) — roadmap, anchor, and theory notes
- 🐧 [dev_foundation/linux/](https://github.com/NguyenPhan161206/reaserching-diary/tree/main/dev_foundation/linux) — the CLI used in every session
- 📘 [AGENTS.md](AGENTS.md) — rules for AI agents working in this repo
- 📝 [NOTE_TEMPLATE.md](NOTE_TEMPLATE.md) — TEMPLATE DSA-1 (problem write-up) and DSA-2 (session journal)

---
*Learn by writing code, and by being wrong in a way you can read.*

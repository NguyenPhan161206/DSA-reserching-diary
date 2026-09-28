# java-course — the Java track

> *Introduction to Java Programming and Data Structures, Comprehensive Version*
> (Y. Daniel Liang) → **44 chapters, 6 message families, 7 gates.**
> **Plan of record:** [`00-roadmap.md`](00-roadmap.md) · **Rules:** [`../AGENTS.md`](../AGENTS.md) §J

This repo is C++/Python-first for DSA. This folder is a **second, self-contained track**
whose textbook is Java. It does not change any rule in `../solutions/`.

---

## 📌 Why this track exists

Reading a chapter produces a note. **Deriving a message from a chapter produces
understanding.** So this track is organised around six ideas, and every chapter is
mapped onto them:

| Code | Message |
|------|---------|
| **M1** | Every line of code has a cost, and the runtime is a program you can read |
| **M2** | Types are a tool that moves errors earlier — paid for in boilerplate and erasure |
| **M3** | Complexity is the real spec; every structure trades one operation for another |
| **M4** | Failure is normal; model the boundary |
| **M5** | Abstraction is a ladder — know which rung you are standing on |
| **M6** | You do not know it is right until a test says so |

A chapter with no mapping is an unfinished plan. The full 44-row matrix lives in
[`00-roadmap.md`](00-roadmap.md).

---

## 📁 Structure

```
java-course/
├── README.md                    # you are here
├── 00-roadmap.md                # 44 chapters × M-codes × size, gates, progress
├── TEMPLATE-MESSAGE-CARD.md     # the only allowed chapter format
├── 00-toolchain/                # JDK 21, run.sh, FastScanner, JUnit demo   ✅
├── p1-fundamentals/             # Ch 1–8
├── p2-oop/                      # Ch 9–13, 17
├── p3-javafx/                   # Ch 14–16, 31
├── p4-dsa/                      # Ch 18–30, 42–43
├── p5-advanced/                 # Ch 32–36, 44
├── p6-enterprise/               # Ch 37–41 (survey)
├── 90-appendices/               # A–J lookup tables
├── 95-projects/                 # 4 capstones
└── 99-assessment/               # 6 checks, one per message family
```

**Per chapter — exactly three artefacts:**

```
p1-fundamentals/ch02-elementary-programming/
├── message-card.md     # REQUIRED · 7 sections
├── exercises.md        # L1 recall / L2 apply / L3 derive + 5 closed-book questions
└── lab/                # 1–3 runnable .java files, compiled and self-tested
```

---

## ⚡ Start here

```bash
source java-course/00-toolchain/env.sh
./java-course/00-toolchain/run.sh java-course/00-toolchain/lab/InputBenchmark.java 1000000
```

Read [`00-toolchain/README.md`](00-toolchain/README.md) next. It is a real write-up with
measured output, and it is the template for every card that follows.

---

## 📏 Rules that are not negotiable

1. **Never copy the book.** Cards are claims you *derived*. At most one short quote, with
   a section reference. Code is rewritten from scratch.
2. **Every message needs a counter-example.** A message nothing can break is unfinished.
3. **🟢 means the lab was compiled and run**, with the real output pasted in.
4. **`🆕 Java 21 delta vs sách` is mandatory** — the printed book targets Java 8/11.
5. **Every chapter names at least one thing the book does not say out loud.**
6. **`exercises.md` vs `../solutions/`** (revised 2026-09-28): a book or self-authored
   exercise **stays in `exercises.md`**, and the **L3 derive tier carries the full
   Section D analysis inline** (brute force, derivation, correctness argument, complexity
   with reasoning, test table, what I got wrong). A problem is **promoted** to
   `../solutions/<topic>/<NN-name>/approach.md` only when it is actually submitted to a
   judge, or becomes a self-authored DSA problem worth reusing — and then both files
   link to each other. `../solutions/` is the C++/Python judged track: never put a
   Java-only exercise there under a `solution.cpp` filename.
7. **Progress = gates passed**, not chapters read.
8. **No `.class` file is ever committed.** `run.sh` writes to `.java-course-build/`.

---

## 🗺️ Progress

| Stage | Status | Gate |
|-------|--------|------|
| Toolchain | 🟢 Done | **G0 ✅** |
| Part I — Ch 1–8 | 🟢 8/8 (audits + teach-backs closed) | G1 ⏳ next |
| Part II — Ch 9–13, 17 | ⬜ | G2 |
| Part III — Ch 14–16, 31 | ⬜ | G3 |
| Part IV — Ch 18–30, 42–43 | ⬜ | G4 |
| Part V — Ch 32–36, 44 | ⬜ | G5 |
| Part VI — Ch 37–41 | ⬜ | G6 |
| Appendices A–J | ⬜ | — |
| Capstones C1–C4 | ⬜ | — |

**Legend:** ⬜ Not started · 🟡 In progress · 🟢 Done

---

## 🔗 Related

- [`00-roadmap.md`](00-roadmap.md) — the plan of record
- [`00-toolchain/README.md`](00-toolchain/README.md) — start here
- [`TEMPLATE-MESSAGE-CARD.md`](TEMPLATE-MESSAGE-CARD.md) — the card format and the done-checklist
- [`../AGENTS.md`](../AGENTS.md) §J — track rules
- [`../solutions/`](../solutions/) — judged problems, cross-linked from Part IV
- **Diary:** [reaserching-diary → dev_foundation/dsa/02-java-for-dsa.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/02-java-for-dsa.md)

---
*A book is a container. The messages are what you take out.*

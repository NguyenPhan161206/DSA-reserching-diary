# java-course — Roadmap of record

**Book:** *Introduction to Java Programming and Data Structures, Comprehensive Version* —
Y. Daniel Liang · **Target runtime:** Java 21 (LTS) · **Created:** 2026-09-28
**Rules:** `../AGENTS.md` Section J · **Card template:** `../TEMPLATE-MESSAGE-CARD.md`

This is the plan of record for the whole book. It exists to answer one question at any
moment: *what is the next thing to do, and what must already be true before it?*

---

## 1. The 6 message families

The book has 44 chapters. Summarising 44 chapters produces 44 forgettable notes. Instead,
every chapter is mapped onto a small set of ideas that the book spends 44 chapters
proving. **A chapter with no family mapping is an unfinished plan.**

| Code | Message | The question it answers |
|------|---------|-------------------------|
| **M1** | Every line of code has a cost, and the runtime is a program you can read | Where does the bytecode go, what does I/O cost, what does a thread cost? |
| **M2** | Types are a tool that moves errors earlier — paid for in boilerplate and erasure | Which errors do we want to die at compile time, and what does that cost? |
| **M3** | Complexity is the real spec; every structure trades one operation for another | Which operation became $O(1)$ and which one became $O(n)$? |
| **M4** | Failure is normal; model the boundary | When the outside world breaks, what does the program say and do? |
| **M5** | Abstraction is a ladder — know which rung you are standing on | Array → List → Map → tree → reimplement: why this rung and not the next one up? |
| **M6** | You do not know it is right until a test says so | What test would have caught the bug I have not hit yet? |

**M6 is cross-cutting.** It is not a chapter; it is the condition on every lab and every
exercise in the repo. A card cannot be 🟢 without a runnable artefact whose output is
pasted in.

---

## 2. The 7-step protocol (repeated verbatim for every chapter)

1. **Survey** — read the chapter's TOC and Liang's closing summary slide. Write the
   **3 questions** the chapter must answer into the card *before* reading further.
2. **Read with one pencil mark** — mark every behaviour that surprised you. Copy nothing.
3. **Extract** — close the book, derive **4–8 falsifiable claims** (M-codes) from your 3
   questions. Each gets a ≤10-line example you wrote yourself.
4. **Lab** — 1–3 runnable `.java` files for the non-obvious claims. **Compile and run
   them.** Paste the real output into the card.
5. **Anti-messages** — 2–3 traps: what a newcomer believes, what actually happens, minimal
   repro.
6. **Exercises** — L1 recall (1), L2 apply (1–2), L3 derive (1, must *measure* or *prove*),
   then 5 closed-book questions. **Boundary:** book exercises stay in `exercises.md`, and
   the L3 tier carries the full `../AGENTS.md` §D analysis inline. A problem is promoted to
   `../solutions/` only when it is actually judged or becomes worth reusing — then both
   files link each way.
7. **Teach-back + tick** — write 100–150 words in `../journal/` explaining the chapter to
   a beginner, without notes. Tick this file. Commit once.

Progress is **gates passed**, not chapters read.

---

## 3. The 44-chapter map

**Sizes:** `S` = card + 1 lab · `M` = card + 2–3 labs + ~5 exercises ·
`L` = card + a small project + ~8 exercises · `T` = survey card, half a page.

### Part I — Fundamentals (Ch 1–8) → `p1-fundamentals/`

| Ch | Title | Msg | Sz | Folder | Gate | Status |
|----|-------|-----|----|--------|------|--------|
| 01 | Computers, Programs, and Java | M1, M6 | M | `ch01-computers-programs-java/` | G1 | 🟢 |
| 02 | Elementary Programming | M1, M2 | L | `ch02-elementary-programming/` | G1 | 🟢 |
| 03 | Conditional Statements | M2, M1 | M | `ch03-conditional-statements/` | G1 | 🟢 |
| 04 | Functions, Characters, and Strings | M2, M1 | L | `ch04-functions-characters-strings/` | G1 | 🟢 |
| 05 | Loops | M3 | M | `ch05-loops/` | G1 | 🟢 |
| 06 | Single-Dimensional Arrays | M3, M1 | M | `ch06-arrays/` | G1 | 🟢 |
| 07 | Object-Oriented Programming | M2, M1, M5, M6 | M | `ch07-oop/` | G1 | 🟢 |
| 08 | Multidimensional Arrays | M3 | M | `ch08-multidimensional-arrays/` | G1 | 🟢 |

> **Numbering note (2026-09-28).** The Part I order on disk deviates from the printed
> book: Methods was folded into Ch04 (function definition, scope, overload, recursion)
> and Ch06–07 became Arrays then OOP, so that each chapter earns its own message set
> instead of splitting one across two folders. The plan of record follows what is built.
> 🟡 = artefacts exist and labs run, but a factual audit item is still open (see each
> card's *What I Got Wrong*).
>
> **Part I audit closed 2026-09-28:** Ch 01 bytecode (`i2l`+`ladd`, not `iadd`) and
> boxing (one shared object, not 5M) fixed with `javap` + a re-run; Ch 02 lab no longer
> fakes `reRead()`, `exercises.md` written with a measured L3 (sum-of-squares: `int`
> wraps at n=1861, `float` at 371, `double` at 300083); Ch 05 trip-count claim
> corrected (⌊(n−1)/3⌋+1 ≡ ⌈n/3⌉). Protocol step 7 teach-backs → `journal/teachbacks-part-i.md`.
> All of Part I is now 🟢; the only open item before G1 is the gate itself.

### Part II — Object-Oriented Programming (Ch 9–13, 17) → `p2-oop/`

| Ch | Title | Msg | Sz | Folder | Gate | Status |
|----|-------|-----|----|--------|------|--------|
| 09 | Objects and Classes | M5, M2 | L | `ch09-objects-and-classes/` | G2 | ⬜ |
| 10 | Object-Oriented Thinking | M5, M2 | M | `ch10-object-oriented-thinking/` | G2 | ⬜ |
| 11 | Inheritance and Polymorphism | M5, M2 | L | `ch11-inheritance-and-polymorphism/` | G2 | ⬜ |
| 12 | Exception Handling and Text I/O | M4, M6 | L | `ch12-exception-handling-and-text-io/` | G2 | ⬜ |
| 13 | Abstract Classes and Interfaces | M5, M2 | L | `ch13-abstract-classes-and-interfaces/` | G2 | ⬜ |
| 17 | Binary I/O and Serialization | M4, M1, M6 | M | `ch17-binary-io-and-serialization/` | G2 | ⬜ |

### Part III — JavaFX GUI (Ch 14–16, 31) → `p3-javafx/`

| Ch | Title | Msg | Sz | Folder | Gate | Status |
|----|-------|-----|----|--------|------|--------|
| 14 | JavaFX Basics | M5 | M | `ch14-javafx-basics/` | G3 | ⬜ |
| 15 | Event-Driven Programming and Animation | M5, M2 | M | `ch15-event-driven-programming/` | G3 | ⬜ |
| 16 | UI Controls and Multimedia | M5 | M | `ch16-ui-controls-and-multimedia/` | G3 | ⬜ |
| 31 | Advanced JavaFX, FXML, Charts | M5 | M | `ch31-advanced-javafx-and-fxml/` | G3 | ⬜ |

> **Constraint:** JavaFX left the JDK in Java 11. All four chapters run through Maven
> (`org.openjfx`), not `javac`. Headless CI needs Monocle. One FXML/MVC project
> replaces four separate chapter projects — see capstone **C4**.

### Part IV — Data Structures & Algorithms (Ch 18–30, 42–43) → `p4-dsa/`

| Ch | Title | Msg | Sz | Folder | Gate | Links to |
|----|-------|-----|----|--------|------|-----------|
| 18 | Recursion | M3 | M | `ch18-recursion/` | G4 | `solutions/09-backtracking/` |
| 19 | Generics | M2, M3 | M | `ch19-generics/` | G4 | — |
| 20 | Lists, Stacks, Queues, Priority Queues | M3, M5 | L | `ch20-lists-stacks-queues/` | G4 | `solutions/02-linear-structures/` |
| 21 | Sets and Maps | M3 | L | `ch21-sets-and-maps/` | G4 | `solutions/01-fundamentals/` |
| 22 | Developing Efficient Algorithms | M3 | L | `ch22-efficient-algorithms/` | G4 | `solutions/05..10-*/` |
| 23 | Sorting Algorithms | M3, M6 | L | `ch23-sorting-algorithms/` | G4 | `solutions/06-sorting/` |
| 24 | Implementing List, Stack, Queue, Priority Queue | M3, M5 | L | `ch24-implementing-collections/` | G4 | **C2** |
| 25 | Binary Search Trees | M3, M5 | L | `ch25-binary-search-trees/` | G4 | `solutions/03-trees/` |
| 26 | AVL Trees | M3 | M | `ch26-avl-trees/` | G4 | `solutions/03-trees/` |
| 27 | Hashing | M3, M6 | L | `ch27-hashing/` | G4 | **C2** |
| 28 | Graphs | M3 | L | `ch28-graphs/` | G4 | `solutions/04-graphs/` |
| 29 | Weighted Graphs | M3 | L | `ch29-weighted-graphs/` | G4 | `solutions/04-graphs/` |
| 30 | Streams | M3 | M | `ch30-streams/` | G4 | `solutions/01-fundamentals/` |
| 42 | 2-4 Trees and B-Trees | M3 | S | `ch42-2-4-and-b-trees/` | G4 | diary `databases/` |
| 43 | Red-Black Trees | M3 | M | `ch43-red-black-trees/` | G4 | `solutions/03-trees/` |

> **Mandatory by hand, no `java.util`:** Ch 24 (List/Stack/Queue/Heap) and Ch 27
> (HashMap/HashSet). A chapter that only calls the JDK collection has taught nothing.

### Part V — Advanced & Systems (Ch 32–36, 44) → `p5-advanced/`

| Ch | Title | Msg | Sz | Folder | Gate | Status |
|----|-------|-----|----|--------|------|--------|
| 32 | Multithreading and Parallel Programming | M1, M4 | L | `ch32-multithreading/` | G5 | ⬜ |
| 33 | Networking | M4 | M | `ch33-networking/` | G5 | ⬜ |
| 34 | Database Programming (JDBC) | M4 | M | `ch34-jdbc/` | G5 | ⬜ |
| 35 | Transactions and Metadata | M4 | S | `ch35-transactions-and-metadata/` | G5 | ⬜ |
| 36 | Internationalization | M2 | S | `ch36-internationalization/` | G5 | ⬜ |
| 44 | Testing with JUnit | **M6** | M | `ch44-testing-with-junit/` | G5 | ⬜ |

> **Java 21 reality check for Ch 32:** virtual threads (`Thread.ofVirtual()`) remove most
> of the thread-pool material the book teaches. The card must state what survived, what
> did not, and why.

### Part VI — Enterprise survey (Ch 37–41) → `p6-enterprise/`

| Ch | Title | Msg | Sz | Folder | Gate | Verdict to research |
|----|-------|-----|----|--------|------|---------------------|
| 37 | Servlets | M4 | T | `ch37-servlets/` | G6 | still standard, `javax` → `jakarta` |
| 38 | JSP | M4 | T | `ch38-jsp/` | G6 | legacy in new projects |
| 39 | JSF | M4 | T | `ch39-jsf/` | G6 | effectively dead |
| 40 | RMI | M4 | T | `ch40-rmi/` | G6 | niche; RPC is grpc/feign now |
| 41 | Web Services (SOAP/REST) | M4 | T | `ch41-web-services/` | G6 | JAX-RS → Jakarta REST |

> Every survey card ends with a mandatory **"Is this still true in 2026?"** verdict and a
> "if starting today, what would I use?" line. The obsolescence *is* the lesson.

---

## 4. Appendices → `90-appendices/`

| File | Covers | Msg |
|------|--------|-----|
| `A-D-types-and-modifiers.md` | A keywords, B ASCII/Unicode, C operator precedence, D modifiers | M2 |
| `E-G-numbers-and-bits.md` | E `double` specials, F number bases, G bitwise ops | M1, M3 |
| `H-J-regex-enum-complexity.md` | H regex, I enums, J Big-O/Ω/Θ | M4, M2, M3 |

Every entry must carry a **"used in chNN"** pointer. An appendix entry with no user is a
dead table.

---

## 5. Gates

| Gate | After | Exit criteria |
|------|-------|---------------|
| **G0** | Tooling | `run.sh` compiles and runs any lab in one command; `mvn test` has been seen red and then green. **✅ done** |
| **G1** | Ch 1–8 | 5 exercises from Ch 2, 4, 5, 7 solved in Java 21 with no documentation open |
| **G2** | Ch 9–13, 17 | Design a `Rational` + `Shape` hierarchy using `Comparable`, `clone`, `equals`, `toString` correctly; explain checked vs unchecked exceptions from memory |
| **G3** | Ch 14–16, 31 | The FXML/MVC app runs, and a fifth screen can be added without touching `Main.java` |
| **G4** | Ch 18–30, 42–43 | `MyHashMap`, `MyPriorityQueue`, `MyBST` pass self-written JUnit; MST and Dijkstra rewritten from an empty file |
| **G5** | Ch 32–36, 44 | Provoke a real deadlock, then fix it with `Lock`; a JDBC rollback test passes |
| **G6** | Ch 37–41 | Answer "REST or Servlet — when?" with a reason, not a preference |

---

## 6. Capstones → `95-projects/`

The proof that the messages were actually transferred: each capstone is built only from
things already ticked in this file.

| ID | Name | Built from | Done when |
|----|------|-----------|-----------|
| **C1** | CLI text/number analyser — menu, file I/O, exceptions, serialization | Ch 2–8, 12, 17 | handles malformed input without crashing, and a round-tripped file matches |
| **C2** | Collections library — `MyArrayList`, `MyLinkedList`, `MyStack/Queue`, `MyHeap`, `MyBST`, `MyHashMap` | Ch 20, 23–27, 44 | every method has a JUnit test, including the empty/duplicate/overflow cases |
| **C3** | Graph toolkit — adjacency, BFS/DFS, MST (Prim + Kruskal), Dijkstra, Floyd, stream analytics | Ch 28–30 | every algorithm has a hand-computed worked example in its README |
| **C4** | JavaFX FXML/MVC app — `TableView`, `LineChart`, event handlers, CSS | Ch 14–16, 31 | new data source added without touching the view layer |

Optional: **C5** multithreaded chat server (Ch 32–33), **C6** JDBC app with transactions
(Ch 34–35).

---

## 7. Execution order

Sequential. Do not start stage $n+1$ until stage $n$ meets its exit criteria.

| Stage | Work | Exit |
|-------|------|------|
| 0 | Folder tree, `.gitignore`, `AGENTS.md` §J, repo `README.md` | tree exists, `git status` understood |
| 1 | Toolchain lab → `00-toolchain/` | G0 |
| 2 | This file + `README.md` + `TEMPLATE-MESSAGE-CARD.md` | 44/44 + 10/10 rows mapped |
| 3 | Part I, ch01 → ch08 | G1 |
| 4 | Part II, ch09 → ch13 → ch17 | G2 |
| 5 | Part III (3 cards + 1 project) | G3 |
| 6 | Part IV, 15 chapters, cross-linked to `../solutions/` | G4 |
| 7 | Part V | G5 |
| 8 | Part VI, 5 survey cards | G6 |
| 9 | Appendices A–J | lookup any operator in ≤30s |
| 10 | Capstones C1–C4 | 5-minute architecture pitch, no notes |
| 11 | `../99-assessment/` — 6 checks, one per message family | 5/6 correct |
| 12 | Index updates, commit, flag the diary for sync | `AGENTS.md` §I checklist |

---

## 8. Progress

**Legend:** ⬜ Not started · 🟡 In progress · 🟢 Done

| Stage | Status | Chapters | Gate |
|-------|--------|----------|------|
| 0 — Structure | 🟢 | — | — |
| 1 — Toolchain | 🟢 | — | **G0 ✅** |
| 2 — Plan of record | 🟢 | — | — |
| 3 — Part I | 🟢 | 8/8 built + teach-backs logged | G1 ⏳ (5 exercises, closed-book, Ch 2/4/5/7) |
| 4 — Part II | ⬜ | 0/6 | G2 |
| 5 — Part III | ⬜ | 0/4 | G3 |
| 6 — Part IV | ⬜ | 0/15 | G4 |
| 7 — Part V | ⬜ | 0/6 | G5 |
| 8 — Part VI | ⬜ | 0/5 | G6 |
| 9 — Appendices | ⬜ | 0/10 | — |
| 10 — Capstones | ⬜ | 0/4 | — |
| 11 — Assessment | ⬜ | — | — |

**Coverage check (keep at 44/44 and 10/10):**

| Part | Chapters mapped | Appendices mapped |
|------|-----------------|-------------------|
| I | 8/8 | 0/10 |
| II | 6/6 | 0/10 |
| III | 4/4 | 0/10 |
| IV | 15/15 | 0/10 |
| V | 6/6 | 0/10 |
| VI | 5/5 | 0/10 |
| **Total** | **44/44** | **0/10** |

---

## 9. What the book does not say out loud

A running list — the reason this course exists rather than a reading of the book.
Each entry is added when a chapter turns out to be hiding something.

1. **Ch 1** — bytecode is not an implementation detail; it is the only place `==` on
   strings is decided.
2. **Ch 2** — the printed book treats integer overflow as an afterthought. It is the
   single most common wrong answer in Java contests.
3. **Ch 4** — `Scanner`'s cost is allocation, not regex. Measure it and the cause is
   obvious.
4. **Ch 12** — the web crawler the book builds on `URL` is written against an API that
   `HttpClient` has replaced; the *shape* (fetch, parse, handle failure) is the part worth keeping.
5. **Ch 27** — a `hashCode` that collides is not a bug and not a security hole on its own;
   `HashMap` verifies with `equals`, and that second call is the design.
6. **Ch 30** — parallel streams are slower below roughly $10^4$ elements because forking
   costs more than the work. The book mentions streams, not this.
7. **Ch 32** — most of the chapter is about thread pools that Java 21 no longer needs for
   the common case.

---

## 10. Related

- `../README.md` — how to use this track
- `00-toolchain/README.md` — the M1/M2/M6 write-up that starts everything
- `../AGENTS.md` §J — the rules
- `../TEMPLATE-MESSAGE-CARD.md` — the card format
- **Diary:** [reaserching-diary → dev_foundation/dsa/02-java-for-dsa.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/02-java-for-dsa.md)
  — the Java-for-DSA language note this track feeds

---
*Created: 2026-09-28 · Update the Status columns as gates are passed, not as chapters are read.*

# Part IV — Data Structures & Algorithms (Ch 18–30, 42–43)

**Gate:** G4 · **Messages:** M2, M3, M5, M6 · **Plan of record:** [`../00-roadmap.md`](../00-roadmap.md) §3
**Heaviest part:** 15 chapters, and the only part that must **not** stand alone.

This part and [`../../solutions/`](../../solutions/) are two views of the same work. The
rule: a card here states the *idea* and links to the solution folder; the solution folder
carries the write-up and the $O$ analysis. Never duplicate — cross-link both ways.

| Ch | Title | Msg | Sz | Links to | Status |
|----|-------|-----|----|-----------|--------|
| 18 | Recursion | M3 | M | `09-backtracking/` | ⬜ |
| 19 | Generics | M2, M3 | M | — | ⬜ |
| 20 | Lists, Stacks, Queues, Priority Queues | M3, M5 | L | `02-linear-structures/` | ⬜ |
| 21 | Sets and Maps | M3 | L | `01-fundamentals/` | ⬜ |
| 22 | Developing Efficient Algorithms | M3 | L | `05-binary-search/`, `07-10-*/` | ⬜ |
| 23 | Sorting Algorithms | M3, M6 | L | `06-sorting/` | ⬜ |
| 24 | Implementing List, Stack, Queue, Priority Queue | M3, M5 | L | **C2** | ⬜ |
| 25 | Binary Search Trees | M3, M5 | L | `03-trees/` | ⬜ |
| 26 | AVL Trees | M3 | M | `03-trees/` | ⬜ |
| 27 | Hashing | M3, M6 | L | **C2** | ⬜ |
| 28 | Graphs | M3 | L | `04-graphs/` | ⬜ |
| 29 | Weighted Graphs | M3 | L | `04-graphs/` | ⬜ |
| 30 | Streams | M3 | M | `01-fundamentals/` | ⬜ |
| 42 | 2-4 Trees and B-Trees | M3 | S | diary `databases/` | ⬜ |
| 43 | Red-Black Trees | M3 | M | `03-trees/` | ⬜ |

## Two chapters are mandatory by hand

- **Ch 24** — write `MyArrayList`, `MyLinkedList`, `MyStack`, `MyQueue`, `MyHeap` without
  touching `java.util`.
- **Ch 27** — write `MyHashMap` and `MyHashSet` without touching `java.util.HashMap`.

Rationale: a chapter that only *calls* the JDK collection teaches syntax, not structure.
Writing one from an empty file is the only way the amortised-cost argument of `ArrayList`
and the collision argument of `HashMap` become yours instead of borrowed.

**Gate G4:** `MyHashMap`, `MyPriorityQueue` and `MyBST` pass self-written JUnit; MST and
Dijkstra are rewritten from an empty file.

---
*Legend: ⬜ Not started · 🟡 In progress · 🟢 Done*

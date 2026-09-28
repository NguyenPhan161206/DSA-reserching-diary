# Part II — Object-Oriented Programming (Ch 9–13, 17)

**Gate:** G2 · **Messages:** M2, M4, M5
**Plan of record:** [`../00-roadmap.md`](../00-roadmap.md) §3

The turn from *a program that runs* to *a program you can change without breaking*. The
load-bearing idea is M5: an abstraction is a ladder, and every rung costs something you
have to name — visibility, `final`, an interface, an extra file.

| Ch | Title | Msg | Sz | Status |
|----|-------|-----|----|--------|
| 09 | Objects and Classes | M5, M2 | L | ⬜ |
| 10 | Object-Oriented Thinking | M5, M2 | M | ⬜ |
| 11 | Inheritance and Polymorphism | M5, M2 | L | ⬜ |
| 12 | Exception Handling and Text I/O | M4, M6 | L | ⬜ |
| 13 | Abstract Classes and Interfaces | M5, M2 | L | ⬜ |
| 17 | Binary I/O and Serialization | M4, M1, M6 | M | ⬜ |

**Gate G2:** design a `Rational` + `Shape` hierarchy that uses `Comparable`, `clone`,
`equals` and `toString` correctly, and explain checked vs unchecked exceptions from
memory.

> Ch 17 sits here rather than with Ch 12 because binary I/O is only interesting once
> objects exist — serialization is the first thing that walks an object graph.

**Feeds:** [`../../solutions/02-linear-structures/`](../../solutions/02-linear-structures/)
(C1 capstone uses Ch 12 + 17 together).

---
*Legend: ⬜ Not started · 🟡 In progress · 🟢 Done*

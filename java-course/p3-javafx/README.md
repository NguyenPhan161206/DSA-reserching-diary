# Part III — JavaFX GUI (Ch 14–16, 31)

**Gate:** G3 · **Messages:** M5 (M2 in Ch 15)
**Plan of record:** [`../00-roadmap.md`](../00-roadmap.md) §3
**Depth:** reduced — 3 cards + 1 project, not 4 projects

| Ch | Title | Msg | Sz | Status |
|----|-------|-----|----|--------|
| 14 | JavaFX Basics | M5 | M | ⬜ |
| 15 | Event-Driven Programming and Animation | M5, M2 | M | ⬜ |
| 16 | UI Controls and Multimedia | M5 | M | ⬜ |
| 31 | Advanced JavaFX, FXML, Charts | M5 | M | ⬜ |

## ⚠️ Build constraint

**JavaFX left the JDK in Java 11.** Nothing in this part compiles with plain `javac`.
Every project needs Maven and `org.openjfx` artifacts; headless CI additionally needs
Monocle. Do not try to work around this with a system `javafx` package — the version
will drift from the JDK.

## Why one project instead of four

The four chapters describe one architecture: a scene graph, an event dispatcher, controls
bound to properties, and a controller layer that keeps the two apart. Building four
separate toy apps would repeat the same four files. Capstone **C4** builds the
architecture once:

```
FXML layout  →  Controller (@FXML)  →  Model
                    ↕
              properties / listeners
```

**Gate G3:** the app runs, and a fifth screen can be added without touching `Main.java`.

---
*Legend: ⬜ Not started · 🟡 In progress · 🟢 Done*

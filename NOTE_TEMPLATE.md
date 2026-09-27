# Note Templates — DSA-Researching-Diary

> Two templates only. Both in **English**. Depth standards from `AGENTS.md` Section D apply.

---

## TEMPLATE DSA-1: Problem Write-Up (`approach.md`)

**Use for:** every problem in `solutions/<topic>/<NN-problem-name>/`
**Filename:** exactly `approach.md`

```markdown
# [Problem Name]

**Date:** YYYY-MM-DD
**Platform:** LeetCode #123 | Codeforces 1234A | PTIT ... | Self-authored
**Difficulty:** Easy | Medium | Hard
**Topic:** Arrays | Two Pointers | Graphs | DP | ...
**Languages:** C++17 (primary) · Python (secondary)
**Time spent:** XX min
**Solved without hints:** Yes | No (what hint?)
**Status:** Attempted | Solved | Needs revisit

---

## 📌 Problem in One Sentence
What goes in, what comes out, and the constraint that makes it hard.

**Constraints that matter:**
- `n ≤ 10^5` → an $O(n^2)$ solution is dead
- values bounded `0..1000` → counting array beats sorting

---

## 🧠 Derivation

### 1. Brute force (and why it fails)
```python
# the naive version — keep it, it is the baseline
```
- **Complexity:** $O(...)$
- **Why it fails at the constraints:** the honest reason (not "it's slow")

### 2. Observation — the property being exploited
> The key insight, in one sentence. What structure is hidden in the input?

### 3. Improved approach
- Algorithm / data structure chosen, and *why this one and not the neighbour*
- **Complexity:** $O(...)$ time, $O(...)$ space

### 4. Correctness argument
> Not "I tested it and it worked." An invariant, an induction, or an exchange argument.

### 5. Walkthrough on a small example
```
input:  [...]
steps:  1 → 2 → 3
output: [...]
```

---

## 💻 Implementation

### C++17
```cpp
// [platform] problem-id — problem name
// Complexity: O(...) time, O(...) space
#include <bits/stdc++.h>
using namespace std;

int main() { /* ... */ }
```

### Python (optional)
```python
# Complexity: O(...) time, O(...) space
```

---

## 🧪 Tests

| # | Input | Expected | Note (why this case is interesting) |
|---|-------|----------|------------------------------------|
| 1 | example from statement | ... | provided |
| 2 | ... | ... | **edge case**: empty / single element / all equal |
| 3 | ... | ... | **worst case** that triggers the slow path |
| 4 | ... | ... | the case that broke me the first time |

---

## ❌ What I Got Wrong
> Mandatory. Even if the answer is "nothing, first try" — write why that is not luck.

- **Mistake:**
- **Root cause:** concept / misread constraint / careless implementation
- **How I caught it:**
- **The habit to build:** one concrete change to my process

---

## 🔄 Alternative Approaches Considered

| Approach | Complexity | Why not chosen (or when I would choose it) |
|----------|------------|--------------------------------------------|
| | | |

---

## 🔗 Related
- **Diary concept note:** [reaserching-diary → ...](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md) — the data structure/idea has a theory note there
- **Neighbouring solution:** `../NN-other-problem/approach.md` — the shared idea
- [SUGGESTED] related problem — pattern this builds toward

## 🤔 Open Questions
- [ ] Can this be done in $O(n)$ / $O(1)$ space?
- [ ] Does this generalise to the weighted / streaming / larger-input variant?
```

---

## TEMPLATE DSA-2: Session Journal (`journal/`)

**Use for:** end-of-session reflections, error-pattern reviews, weekly reviews.

```markdown
# Session Journal — [YYYY-MM-DD] — [Topic]

**Duration:** XX min
**Problems attempted:** N | **Solved:** N | **Without hints:** N

---

## 📌 Summary
One paragraph: what the session actually achieved (not what was planned).

## ✅ What Worked
- Technique, insight, or habit that transferred

## ❌ Where I Lost Time
| Problem | Time lost | Root cause | Category |
|---------|-----------|------------|----------|
| | | | misread constraints / wrong data structure / off-by-one / slow I/O / knew it, panicked |

## 💡 Aha Moment
> The "oh — *that's* why it works" moment. One paragraph. Link to the theory note in the diary if it exists.

## 🎯 Next Session
- [ ] Specific, small, actionable (not "review graphs")

## 🔗 Related
- **Diary:** [reaserching-diary → dev_foundation/dsa/](https://github.com/NguyenPhan161206/reaserching-diary/tree/main/dev_foundation/dsa)
```

---

## Template Selection

```
What am I writing?
        │
        ├── A problem I solved (or attempted)  → TEMPLATE DSA-1  (solutions/<topic>/<NN-problem>/approach.md)
        ├── An external problem (LeetCode etc.)→ TEMPLATE DSA-1  (problems/<platform>/)
        └── A reflection on how the session went → TEMPLATE DSA-2  (journal/)
```

**Rule:** theory that does not need code belongs in the diary repo, not here.

---
*Config version: 1.0 — created 2026-09-28.*

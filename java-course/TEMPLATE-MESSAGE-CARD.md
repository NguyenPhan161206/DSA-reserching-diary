# TEMPLATE — `message-card.md`

> Copy this file to `java-course/pX-.../chNN-kebab-case-title/message-card.md` and
> fill every section. **Do not delete a section** — if a section is empty, that means
> the chapter is not finished. Per `AGENTS.md` Section J.

```markdown
# ChNN — Chapter Title

**Book ref:** Ch.N (§N.N–N.N)  ·  **Messages:** M1, M3  ·  **Size:** S | M | L
**Status:** ⬜ Not started | 🟡 In progress | 🟢 Done
**Prereq:** chNN-…  ·  **Last touched:** YYYY-MM-DD

---

## 📌 3 câu hỏi chương này trả lời
> Written *before* re-reading. Three questions, no answers yet — this is the contract
> the chapter must meet.

1.
2.
3.

---

## 🧬 Messages
> 4–8 entries. Each is a **claim you derived**, falsifiable by one example.
> A message nobody can break is not finished.

**M1-01 — <the claim, as a sentence>**
- *Evidence:* <the minimal snippet or the measured number>
- *Counter-example:* <the case that breaks the naive reading>
- *What this changed in my head:* <one line — why the old belief was wrong>

**M2-01 — <claim>**
- *Evidence:*
- *Counter-example:*
- *What this changed:*

---

## ⚠️ Anti-messages
> 2–3 traps. Each: what the newcomer believes, what actually happens, minimal repro.

| The trap | What actually happens | Repro |
|----------|----------------------|-------|
| | | |

---

## 🆕 Java 21 delta vs sách
> Mandatory. The printed book targets Java 8/11. State what is different, what is
> better, and what the book got *right* for a reason you now understand.

| Book | Java 21 | Why it matters here |
|------|---------|--------------------|
| | | |

---

## 🔗 Cầu nối
- **Previous:** `../chNN-…/message-card.md`
- **Next:** `../chNN-…/message-card.md`
- **Solution in this repo:** `solutions/NN-topic/…/approach.md`
- **Diary concept note:** [reaserching-diary → dev_foundation/dsa/…](…)
- **Lab:** `lab/` — what to run and what number to expect

---

## ❌ What I Got Wrong
> Mandatory (AGENTS.md Section D). Even "nothing, first try" — then explain why that
> is not luck.

- **Mistake:**
- **Root cause:** concept / misread constraint / careless implementation
- **How I caught it:**
- **The habit to build:**

---

## 🤔 Câu hỏi mở
- [ ] Unanswered question that this chapter raised — carry it to a later chapter.
```

---

## Checklist trước khi tick 🟢

- [ ] 3 câu hỏi đã trả lời được bằng lời của mình, không cần mở sách
- [ ] 4–8 message, mỗi cái có **evidence** + **counter-example**
- [ ] 2–3 anti-message, mỗi cái có repro tối thiểu
- [ ] Mục **Java 21 delta** đã điền
- [ ] `lab/` **đã compile và chạy**, output thật dán vào card
- [ ] `exercises.md` đã làm L1 + L2, L3 đã **đo hoặc chứng minh**
- [ ] 5 câu tự kiểm trả lời sai ≥ 1 câu (nếu không, test chưa đủ khó)
- [ ] Có **ít nhất 1 điều sách không nói ra**
- [ ] Teach-back đã viết trong `journal/`
- [ ] Ticked trong `java-course/00-roadmap.md`
- [ ] Không có đoạn nào sao chép nguyên văn từ sách

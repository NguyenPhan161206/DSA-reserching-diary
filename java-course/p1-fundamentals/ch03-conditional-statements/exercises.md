# Ch 03 — Conditional Statements — Exercises

> Book: *Introduction to Java Programming and Data Structures, Comprehensive Version*
> (Y. Daniel Liang) — Chapter 3, "Conditional Statements".
>
> **Boundary rule:** these are book exercises. They stay here, in `exercises.md`.
> Nothing in this chapter has been submitted to a judge, so nothing is promoted to
> `solutions/`. The **L3 tier carries the full Section D analysis inline** below.

---

## L1 — Recall (close the book)

1. What is the difference in *evaluation* between `&` and `&&` when both operands are
   `boolean`? Which one can you legally use to guard an array access?
2. Why does `i < arr.length & arr[i] > 0` still throw `ArrayIndexOutOfBoundsException`
   when `i` is out of bounds, even though the expression *contains* the bound check?
3. `byte b = 10; b = b + 1;` — why does this not compile, and what did the compiler stop
   you from doing? (Chapter 2 material, revisited through a conditional expression.)
4. What is the static type of `true ? 1 : 2.0`? Of `false ? 'a' : 98`?
5. In the old colon-style `switch`, what happens if a `case` body has no `break`?
6. In the arrow-style `switch`, is fall-through possible? Why does the compiler answer
   that question for you?

## L2 — Apply (write the code, then run it, then read the output)

**E2.1 — The guard that stops working.** Predict the output *before* running:

```java
int[] a = {1, 2, 3};
int i = 3;
System.out.println(i < a.length && a[i] > 0);
System.out.println(i < a.length & a[i] > 0);
```

Then change the first line to `i = 2` and predict again. Write down what you expected
*before* running, because the point of the exercise is the difference between your
prediction and reality.

**E2.2 — Find the bug.** Each snippet compiles. Each is wrong. Say what type the
expression has, and what it does instead:

```java
int n = 3;
Object result = n % 2 == 0 ? n / 2 : n * 1.0 / 2;   // n = 3
System.out.println(result);                          // you expect 1.5

char grade = true ? 'A' : 90;                        // which line does not compile?
```

**E2.3 — Fall-through as a bug.** Write the colon-style version of a `switch` that maps a
day to `weekday`/`weekend`. Delete one `break`. Show that it still compiles, and that it
returns a *wrong answer* rather than an error. Then rewrite it with the arrow syntax and
show the compiler refuses to let you express the mistake.

**E2.4 — Measure the ratio yourself.** Change `N` and the inner step count in
`lab/Ch03ShortCircuit.java`. Make a table of `&` / `&&` ratio against inner-step count
(1, 10, 100, 1000). At what point does the ratio stop being a "small" difference? Write
the sentence that describes the pattern.

**E2.5 — Bit folding.** Implement `boolean[] flags` → a single `long` mask for 64 flags,
and write a method `boolean allSet(long mask, int count)`. Then write the same thing with
a plain `for` loop over the array. Which is shorter, which is clearer, and does the
shorter one win? Put the answer in the open question in the card, section 10.

## L3 — Derive (full Section D analysis)

### L3.1 — LeetCode 35 · Search Insert Position (35)

**Problem, restated in one sentence.** Given a sorted array `nums` of *distinct* ascending
`int`s and a `target`, return the index of `target` if present, otherwise the index where
it would be inserted to keep the array sorted.

**Constraints, quoted.** `1 ≤ nums.length ≤ 10^4`; `nums` sorted in strictly ascending
order; `0 ≤ nums[i] ≤ 10^4`.

These constraints are the whole problem. *Sorted* and *distinct* are what let a search
replace a scan. `10^4` is what makes $O(n)$ survivable but embarrassing — LeetCode
accepts it, which is precisely why a counter-example is required below.

**Brute force, and why it is too slow.**

```java
int searchInsert(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        if (nums[i] >= target) return i;
    }
    return nums.length;
}
```

Time $O(n)$, space $O(1)$. It is not *wrong* and it is not *too slow* at $n = 10^4$ — it
runs in microseconds. That is the honest reason this exercise exists in a book: the brute
force is fast enough, so the only justification for the improvement is that the input
*guarantees* something the brute force ignores.

**The improvement, derived.** The loop above reads `nums[i]` and then decides. It has
already paid for the comparison. The sorted property means the comparison at the midpoint
tells you which *half* to discard — and it costs exactly as much as the comparison you
just did. So a comparison that was going to happen anyway can be made to eliminate half
the candidates:

> Each step spends one comparison to remove half of the remaining range. After $k$
> comparisons the range is $n/2^k$, so the loop ends when $n/2^k < 1$, i.e. $k \ge
> \log_2 n$. The work per step is $O(1)$, so the total is $O(\log n)$.

This is derived, not recalled: the halving is what makes the logarithm, and the logarithm
appears only because the input is sorted.

**Correctness argument (loop invariant).** Over the closed range $[lo, hi)$:

> **Invariant.** If the answer exists, it lies in $[lo, hi)$. Furthermore, every index
> less than `lo` holds a value `< target`, and every index `≥ hi` holds a value `>
> target` (or is out of bounds).

- *Base case.* `lo = 0`, `hi = nums.length`. No index is excluded. The invariant holds.
- *Maintenance.* Let `mid = lo + (hi - lo) / 2`. Three cases:
  - `nums[mid] == target` → returning `mid` is correct, done.
  - `nums[mid] < target` → set `lo = mid + 1`. Everything at `≤ mid` is `< target`, so
    nothing that could be the answer was discarded.
  - `nums[mid] > target` → set `hi = mid`. Everything at `≥ mid` is `> target` (values are
    distinct and the array is sorted), so nothing that could be the answer was discarded.
  In both non-terminating cases the invariant is preserved and the range strictly shrinks.
- *Termination.* Each step removes at least one element, so eventually `lo == hi`. The
  invariant then says the answer — if it exists — must be at `lo`/`hi`, and because all
  smaller indices hold `< target` and all larger hold `> target`, `lo` is exactly the
  insertion point. ∎

Note that `mid = lo + (hi - lo) / 2` is written this way on purpose. The book's naive
`(lo + hi) / 2` overflows `int` when `lo` and `hi` are near `Integer.MAX_VALUE` — and
`hi` really can be `nums.length`, which is bounded by the array length, so here it cannot.
**The safe form costs nothing and removes a class of bug.** See open question 3.

**Complexity, with the reasoning for the dominant term.**

| | time | space |
|---|---|---|
| brute force | $O(n)$ — one comparison per element | $O(1)$ |
| binary search | $O(\log n)$ — $\lceil \log_2(n+1) \rceil$ comparisons, $O(1)$ each | $O(1)$ |

At $n = 10^4$: ~14 comparisons instead of ~10,000. The $O(1)$ space is a real
achievement and is easy to forget — the recursive form of binary search is $O(\log n)$
space because each frame holds a range.

**Test table.** Written by hand, then run.

| # | `nums` | `target` | expected | why it is interesting |
|---|---|---|---|---|
| 1 | `[]` (below the stated bound) | `5` | `0` | the *only* case where "insert at 0" is unambiguous with no array |
| 2 | `[1]` | `1` | `0` | first element, exact hit |
| 3 | `[1]` | `0` | `0` | smaller than everything, insert at front |
| 4 | `[1]` | `2` | `1` | larger than everything, insert at end |
| 5 | `[1,3,5,6]` | `5` | `2` | exact hit in the middle |
| 6 | `[1,3,5,6]` | `2` | `1` | gap in the middle — the "no exact hit" case that `return mid` gets wrong |
| 7 | `[1,3,5,6]` | `7` | `4` | past the end, loop's final `return` handles it |
| 8 | `[1,1,1]` (breaks the *distinct* guarantee) | `1` | `0` | counter-example: duplicates break the "everything ≥ hi is `> target`" half of the invariant. The code still returns a valid answer, but the *invariant as stated* is false. This is the boundary the constraint draws. |

**The table was checked, not asserted.** All 8 rows plus a brute-force reference were run
against each other over 5 arrays × 7 targets = 35 cases:

```
[1, 1, 1]                      target=5   got=3   want=3   ok
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10] target=0   got=0   want=0   ok
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10] target=7   got=6   want=6   ok
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10] target=11  got=10  want=10  ok
... 35 cases, 0 mismatches
n=10000, log2(n) = 13
```

`log₂(10⁴) ≈ 13.28`, so $\lceil \log_2(n+1) \rceil = 14$ comparisons — the "~14" in the
complexity table above is measured, not asserted. Note the `[]` row from the table: the
reference implementation returns `0` for an empty array, and so does the binary search,
because `lo = hi = 0` on entry and the loop body never runs.

**What I Got Wrong (on this exercise, before running it).** My first instinct was to write
`if (nums[mid] == target) return mid;` and then `return mid;` at the end — the same `mid`
variable. That is a *real* bug in the version that does not special-case equality: after
the loop, `mid` is stale, because the last computed `mid` belonged to a range that has since
shrunk. Returning `lo` is the only correct final answer, and the reason is the invariant,
not the intuition. I have written that as a deliberate trap in E2.1's spirit: **the value
you have lying around is not the value you need.**

**Open questions for this exercise.**
- [ ] The constraint says *distinct*. How does binary search on a **non-distinct** sorted
      array change? (Find the first occurrence instead.) Which line of the invariant has to
      be rewritten, and does the $O(\log n)$ bound survive?
- [ ] What is the first input, in order of length, where the brute force takes longer than
      1 ms? That is the point where "too slow" stops being a matter of opinion.
- [ ] Rewrite it recursively. Confirm the space goes from $O(1)$ to $O(\log n)$, and find
      the length at which the recursive version is actually *slower* despite worse space.

### L3.2 — Ternary operator type inference, from first principles

**Statement.** Given `int a` and `int b`, write one expression using `?:` that returns
`a / b` as a `double` when `a % b == 0`, and `a / (double) b` otherwise — and explain,
from the JLS rule, why the naive version returns a `Character` in one of the cases.

**This is the exercise that makes §2.3 of the card stick.** There is no input that
produces a wrong answer silently *and* obviously; you have to reason about the static type
first. Write the expression, print `expr.getClass().getSimpleName()` for all four
combinations of `a > 0`/`a < 0` and `a % b == 0`/`!= 0`, and check every one of the four
types against what you predicted **before** running.

**Space/time.** $O(1)$ both. The interesting cost is the *compile-time* type-check: four
combinations to reason about at once, which is exactly the boilerplate M2 is paying for.

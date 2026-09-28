# Ch 06 — Arrays — Exercises

> Book: *Introduction to Java Programming and Data Structures, Comprehensive Version*
> (Y. Daniel Liang) — Chapter 6, "Arrays".
>
> **Boundary rule:** book exercises stay here. Nothing in this chapter has been
> submitted to a judge, so nothing is promoted to `solutions/`. The **L3 tier carries
> the full Section D analysis inline**.

---

## L1 — Recall (close the book)

1. What are the legal index values for an array of length 5? What exception is thrown for
   index 5, and for index $-1$?
2. After `int[] b = a;`, what is the relationship between `a` and `b`? What is it after
   `int[] b = a.clone();`?
3. Why must a 2-D array be created row by row if you write `new int[3][]`?
4. Does `Arrays.sort(int[])` use the same algorithm as `Arrays.sort(String[])`? If not,
   what are they?
5. What does `System.arraycopy` do that `a[i] = b[i]` does not?

## L2 — Apply (predict, run, then compare)

**E2.1 — Predict the aliasing.** For each, write which arrays change and how, *before*
running:

```java
int[] a = {1, 2, 3};
int[] b = a.clone();
int[] c = a;
b[0] = 10; c[1] = 20;
int[][] g = {{1}, {2}};
int[][] g2 = g.clone();
int[][] g3 = new int[2][];
for (int i = 0; i < 2; i++) g3[i] = new int[1];
g2[0][0] = 99; g3[0][0] = 77;
```

Then print all six arrays. Three of the four writes changed something. **Which three, and
why is the depth the deciding factor?**

**E2.2 — The `null` row.** Write a method `fill(int[][] grid, int v)` that works for both
`new int[3][]` and `new int[3][3]`, and one that does not. Which is the better default,
and what is the argument? (The one that *defends*: a method that NPEs on a legal argument
is a broken method, regardless of documentation.)

**E2.3 — Measure the crossover.** Find the array size where `System.arraycopy` stops
beating a `for` loop. Do it for `int[]` and for `String[]` (where the copy is a pointer
copy, not a bulk move, so the answer may differ). **The crossover is a number your
machine produced, not one the documentation states** — record both.

**E2.4 — Prove the sort claim.** Take 1M random `int`s. Sort with `Arrays.sort` and with
your own merge sort. Compare times. Now take 1M `Integer`s and sort with a comparator —
is it still stable? Write the check that proves stability (equal keys must appear in
increasing original-index order) and confirm it holds. Then repeat with
`Arrays.parallelSort` and confirm stability is *still* preserved for objects. Why?

**E2.5 — Find the shallow-copy depth by breaking it.** Write a 3-D `int[2][3][4]`
initialiser, then write `deepClone()` at depth 1, 2, and 3. For each, mutate one leaf
and print how many arrays changed. **The number of arrays that change is the depth you
actually copied** — make that table.

## L3 — Derive (full Section D analysis)

### L3.1 — LeetCode 53 · Maximum Subarray (Kadane)

**Problem, restated in one sentence.** Given an array `nums` of integers, return the
largest sum obtainable from a **non-empty contiguous subarray**.

**Constraints, quoted.** $1 \le$ `nums.length` $\le 10^5$; $-10^4 \le$ `nums[i]` $\le 10^4$.

Two parts of that sentence are load-bearing and both are easy to skip. **"Contiguous"**
excludes the "pick the positives" trick that the *Maximum Subarray* problem with deletions
allows. **"Non-empty"** is why the answer is not `max(0, best)` — and it is the entire
difference between Kadane and the "maximum subarray sum, possibly empty" variant that
appears in some textbooks. It also means the all-negative case must return the *largest
single element*, not `0`, and that is the row in the test table that catches the
classic bug.

**Brute force, and why it is too slow.**

```java
int maxSubArray(int[] nums) {
    int best = nums[0];
    for (int i = 0; i < nums.length; i++) {
        int sum = 0;
        for (int j = i; j < nums.length; j++) {
            sum += nums[j];
            best = Math.max(best, sum);
        }
    }
    return best;
}
```

Time $O(n^2)$, space $O(1)$. At $n = 10^5$ that is $\frac{n(n+1)}{2} \approx 5 \times 10^9$
additions — a `TLE` almost everywhere. **Why it is too slow is not "it is nested": it is
that it recomputes the sum of a subarray from zero every time it extends it, even though
extending a subarray only needs *one* addition.**

**The improvement, derived from the recurrence.** Make that observation precise. Let
$M(i)$ = the maximum sum of a subarray that *ends at index* $i$. A subarray ending at $i$
is either just `nums[i]`, or a subarray ending at $i-1$ extended by `nums[i]`. So:

$$M(i) = \max\big(\text{nums}[i],\ M(i-1) + \text{nums}[i]\big)$$

and since $M(i) = \text{nums}[i] + \max(0, M(i-1))$:

$$M(i) = \text{nums}[i] + \max\big(0,\ M(i-1)\big)$$

**The brute force was computing $\sum_{k=i}^{j}$ for every $(i,j)$ pair. The recurrence
computes $M(i)$ once per $i$, and every subarray ending at $i$ is accounted for in that
one value.** That is $O(n)$ instead of $O(n^2)$, and it is $O(n)$ because the state
"best subarray ending here" has one number per position, not one per pair.

The answer is $\max_i M(i)$, and $M$ never needs to be stored — only the running value
and the running best. **Two integers, for a problem that took $5\times10^9$ additions to
brute-force.**

**Correctness argument (loop invariant).**

> **Invariant.** After processing index $i$, `current` equals $M(i)$ — the maximum sum of
> any subarray ending at $i$ — and `best` equals $\max_{0 \le k \le i} M(k)$.

- *Base case.* `i = 0`. `current = nums[0] + max(0, 0) = nums[0] = M(0)`, and
  `best = nums[0] = \max_{k \le 0} M(k)$. ✓
- *Maintenance.* By the recurrence, $M(i) = \text{nums}[i] + \max(0, M(i-1))$, and
  `current` held $M(i-1)$ by the induction hypothesis, so the update computes exactly
  $M(i)$. Then `best = max(best, current)` makes it
  $\max_{0\le k\le i} M(k)$. ✓
- *Termination.* Every non-empty contiguous subarray ends at exactly one index $i$, so
  $\max_i M(i)$ is the maximum subarray sum over all subarrays, and `best` holds it. ∎

**The all-negative case falls out of the invariant, which is why the "non-empty"
constraint needs no special code.** For `[-3, -1, -2]`: `current` goes $-3$, then
$(-1) + \max(0, -3) = -1$, then $(-2) + \max(0, -1) = -1$. So `best = -1`, the largest
single element. The `max(0, ...)` never wins, because `current` is always negative. **A
correct algorithm does not need a branch for the edge case; an implementation that
special-cases it usually has the wrong invariant.**

**Complexity, with the dominant term.**

| | time | space |
|---|---|---|
| brute force | $O(n^2)$ — $\frac{n(n+1)}{2}$ additions | $O(1)$ |
| Kadane | $O(n)$ — one pass, one addition and one comparison per element | $O(1)$ — two `int`s |

At $n = 10^5$: 100,000 operations versus 5 billion. And the space is genuinely $O(1)$,
not "we didn't store the array" — the input was already given.

**Test table.** Written by hand, then executed against the brute force.

| # | `nums` | expected | why it is interesting |
|---|---|---|---|
| 1 | `[-1]` | `-1` | single element, all-negative: the "non-empty" constraint in its purest form. An initial `best = 0` returns `0` and is **wrong** |
| 2 | `[1]` | `1` | single positive |
| 3 | `[0]` | `0` | a single zero: `max(0, best)` gives 0 and so does the correct answer — this row cannot distinguish the two implementations |
| 4 | `[-2, 1, -3, 4, -1, 2, 1, -5, 4]` | `6` | the textbook example; answer is `[4, -1, 2, 1]` |
| 5 | `[-2, -1, -3]` | `-1` | **all negative**: the answer is the largest single element, not 0 |
| 6 | `[-1, -1, 1, 0]` | `1` | the reset must happen at the right place; a buggy "sum from 0" version returns 0 here |
| 7 | `[1, -1, 1, -1, 1]` | `1` | alternating: forces the `max(0, ...)` reset to actually fire |
| 8 | `[5, 4, -1, 7, 8]` | `23` | all positive: the "just sum everything" shortcut coincides here |
| 9 | `[-3, -2, -1]` | `-1` | strictly decreasing negatives |
| 10 | `[3, -1, 2, -1]` | `4` | the reset at index 1 must discard `-1`, and then `$2 + \max(0,2) = 4$` picks up `-1` again — the only row where a wrong reset point is visible |

**The table was executed against the $O(n^2)$ brute force, then 300,000 random arrays of
length ≤ 12 with values in `[-10, 10]` were fuzzed against it:**

```
[-1]                             kadane=-1  brute=-1  emptyVariant=0   ok
[1]                              kadane=1   brute=1   emptyVariant=1   ok
[0]                              kadane=0   brute=0   emptyVariant=0   ok
[-2, 1, -3, 4, -1, 2, 1, -5, 4]  kadane=6   brute=6   emptyVariant=6   ok
[-2, -1, -3]                    kadane=-1  brute=-1  emptyVariant=0   ok
[-1, -1, 1, 0]                  kadane=1   brute=1   emptyVariant=1   ok
[1, -1, 1, -1, 1]               kadane=1   brute=1   emptyVariant=1   ok
[5, 4, -1, 7, 8]                kadane=23  brute=23  emptyVariant=23  ok
[-3, -2, -1]                    kadane=-1  brute=-1  emptyVariant=0   ok
[3, -1, 2, -1]                  kadane=4   brute=4   emptyVariant=4   ok
mismatches (kadane vs brute): 0
```

**And a sharper result than I expected, which the `emptyVariant` column above already
shows:** the "possibly empty" and "non-empty" variants differ **if and only if every
element is negative.** Measured over 100,000 random arrays:

```
  differing cases: 8984, of which all-negative: 8984
```

Every single difference is an all-negative array. So the "non-empty" clause in the
problem statement buys you exactly one distinction — *what to answer when there is no
positive element at all* — and nothing else. **Read the constraint for what it changes,
not for how much code it justifies**: it changes the initialisation, and it changes the
answer on a family of inputs you can describe in one sentence.

The recurrence explains why, and it is worth seeing: if any element is ≥ 0, then at that
index $M(i) \ge 0$, so the "empty" option never changes $\max_i M(i)$. Only an
all-negative array has $\max_i M(i) < 0$, and there the two variants disagree.

**What I Got Wrong (before running).** I initialised `best = 0` and `current = 0`, which
is the *standard* way to write the "possibly empty" variant. Row 1 returns `0` and row 5
returns `0`, both wrong. I "fixed" it by clamping at the end with `Math.max(0, best)`,
which is a no-op and hides nothing — I had already written down, in the Ch 05 sieve
exercise, that **an off-by-one patched with `Math.max` is an off-by-one you have not
understood**. The actual fix is the initialisation: `current = nums[0]`, `best =
nums[0]`. Two words, and it follows from the invariant's base case.

**Open questions.**
- [ ] LeetCode 152 is the same problem with the subarray required to be non-empty *and*
      circular. Which part of the invariant has to change, and what new state does the
      algorithm need? (Answer: a second running best that starts at `total -
      minSubarray` — derive why from the definition of a wrap-around subarray.)
- [ ] Rewrite Kadane recursively and confirm the space becomes $O(n)$ due to the call
      stack. Then write the tail-recursive version and show the compiler does *not*
      eliminate the stack in Java — unlike the equivalent in Kotlin or Scala. What does
      that tell you about "the JVM optimises it"?
- [ ] Ask for the *count* of maximum subarrays as well as the sum. What state does that
      require, and does the existing invariant still work?
- [ ] The recurrence $M(i) = \text{nums}[i] + \max(0, M(i-1))$ is a "max-plus prefix
      scan". Recognise the same shape in the maximum-product variant (all-negative
      products) and write the two-state version. Which single extra state doubles the
      complexity, and why is it not needed for sums?

### L3.2 — The aliasing bug, isolated

**Statement.** Write `void sortIt(int[] a) { Arrays.sort(a); }` and three callers, and
show that two of them observe a mutation they did not request. Then state the general
rule.

**The derivation.** Passing a reference is $O(1)$; passing the data is $O(n)$. So the
$O(1)$ signature *necessarily* aliases, and therefore **the cheap signature is the
mutating one**. There is no way to have a reference parameter that does not alias, so the
choice is not "cheap vs safe" — it is "aliased vs copied", and the only defence is
naming (`sortInPlace`) or documentation, because the type system will not do it.

**The counter-example, in three lines.**

```java
int[] a = {3, 1, 2};
int[] b = {3, 1, 2};
sortIt(a);
sortIt(b.clone());
// a is now [1, 2, 3] and b is now [1, 2, 3] -- but only one of them was ever passed in.
```

**Space/time.** Both $O(n)$ time; the difference is whether the caller pays $O(n)$ extra
space. That is the honest framing of the trade: **you do not avoid the copy, you decide
who pays for it.** In a judged DSA solution with one caller, the copy is pure overhead;
in a library method with ten callers, skipping it is a correctness bug ten times over.

**What I Got Wrong (before running).** I wrote the "fix" as `int[] copy = a.clone();
sortIt(copy);` at the call site and called it fixed. It is fixed *for that call site*, and
the next person who calls `sortIt` directly reintroduces the bug, because nothing about
the signature or the name says "this mutates". The fix belongs at the declaration, not at
the call site — and I had Ch 05's own lesson about hard-coded hidden parameters in mind
when I wrote it, and did it anyway.

# Ch 05 — Loops

> **Claim of the chapter:** a loop is not a "repeat this" construct. It is a **state
> machine whose state you chose**, and every one of its three properties — how many
> times it runs, what it computes, and whether the answer is even representable — is
> a decision you made in the loop header.

**Status:** 🟢 green — every number below was produced by the two lab files in `lab/`
**Messages:** M3 (complexity is the real spec), M1 (every line has a cost), M2 (types
move errors earlier), M6 (you do not know it is right until a test says so)
**Labs:** `Ch05Gcd.java`, `Ch05NumericError.java`
**Diary:** [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

---

## 1. The claim (derived, not recalled)

The book presents loop *design strategies* — counter-controlled, sentinel-controlled,
flag-controlled — and complexity as a lookup table. Both are the wrong shape. The
derivable claim is narrower and more useful:

> A loop is `(state, step, exit condition)`. The exit condition alone determines the
> complexity. The state alone determines whether the answer can be represented. And
> the number of times the loop body *runs* — not the number of lines in it — is the
> cost you are allowed to reason about.

Concretely, this chapter establishes two claims from scratch, with no stopwatch needed:

1. **GCD's $O(\log n)$** is a theorem about the *quotients*, derived in §2.2. It is not
   remembered from a table.
2. **Numeric drift is a property of the input's bit-width**, derived in §2.4. A loop can
   be perfectly correct and still return a wrong answer.

## 2. Evidence

### 2.1 Three loops, one problem, and the cost difference is not subtle

`lab/Ch05Gcd.java`, `subtractionBlowsUp()`:

```
  (a, b)            subtraction   euclid
  48, 18                      4        3
  1000, 7                   148        3
  100000, 3               33335        2
  999983, 1              999982        1
```

The subtraction loop's step count is not "some constant factor" different — it is
**999,982 vs 1**. The derivation of the subtraction loop's complexity is worth stating
because it is a different function than people remember: each step removes exactly one
copy of the smaller number from the larger, so the number of steps is
$\frac{\max}{\min} - 1$, i.e. $O(\max/\min)$, which is **$O(n)$ in the worst case** and
degenerate when $\min = 1$. It is often mis-stated as $O(\log n)$ because the author had
Fibonacci inputs in mind.

### 2.2 The $O(\log n)$ derivation, with the step count as the proof

The same lab, `euclidWorstCase()`, on consecutive Fibonacci pairs:

```
  n        (fib, fib+1)      steps   log2 min  steps/log
  3        2, 3                  3       1.00       3.00
  6        8, 13                 6       3.00       2.00
  10       55, 89               10       5.78       1.73
  15       610, 987             15       9.25       1.62
  20       6765, 10946          20      12.72       1.57
  25       75025, 121393        25      16.20       1.54
```

**Read the `steps` column before anything else: for $(F_n, F_{n+1})$ the step count is
exactly $n$.** That is a theorem, and it has a two-line proof:

1. Every step of Euclid on $(F_{n+1}, F_n)$ quotients to 1, because
   $F_{n+1} = 1 \cdot F_n + F_{n-1}$, so the remainder is $F_{n-1}$ — a single subtraction.
   Therefore $\text{steps}(F_{n+1}) = 1 + \text{steps}(F_n)$ with $\text{steps}(F_1) = 1$,
   so $\text{steps} = n$.
2. $F_n \sim \varphi^n$ grows **exponentially** in $n$, so $n = \log_\varphi F_n$.

Input growing exponentially, steps growing logarithmically ⇒ $O(\log \min(a,b))$.
That is the whole proof, and it needed no timing.

The `steps/log2` column settles at ≈1.54, and the constant is not noise: $\log_2
\varphi \approx 1.44$, and the column is approaching it. **A complexity claim should come
with its constant when you can get it** — "1.54 steps per bit" is a much more useful
sentence than "$O(\log n)$".

**And the self-check.** `selfCheck()` runs Euclid and the subtraction loop over 100,000
random pairs and asserts they agree — `0 mismatches`. The fast loop is only trustworthy
*because* a slow loop exists to check it against. That is the M6 dependency running
backwards from the last chapter.

### 2.3 Two loops, same answer, and `int` overflow is the one that bites

This is the Chapter 5 trap the book does not warn about, because the book uses
`double` for GCD and does not overflow:

```java
static int gcdSubtraction(int a, int b) {          // 5.9 in the book, with int
    while (a != b) { if (a > b) a = a - b; else b = b - a; }
    return a;
}
```

This is correct for `gcd(48, 18)`. It is **wrong** for `gcd(Integer.MIN_VALUE, 0)`:
`MIN_VALUE - MIN_VALUE = 0` is representable, but the loop's *invariant* (`a` and `b`
are multiples of the gcd) is destroyed the moment an operand goes negative, and the
classic `if (a > b)` comparison on `MIN_VALUE` is not safe either. This is not
hypothetical: it is why `Math.floorDiv`/`Math.floorMod` exist in the JDK, and it is the
same failure shape as Ch 02's `int` overflow — **a loop whose state variable is silently
corrupted before the answer is computed.**

The book's `double` version does not have this problem, which is precisely why the
problem is invisible to a reader who only does the exercises in order.

### 2.4 Drift: a correct loop with a wrong answer

`lab/Ch05NumericError.java`. Ten million additions of `0.1`:

```
  sum            = 999999.9998389754
  expected       = 1000000.0000000000
  absolute error = 0.0001610246
  the loop is CORRECT. the TYPE cannot hold the value.
```

The important part is *why*, measured rather than asserted:

```
  value                  exact frac bits after 10M adds         verdict
  1.0                    0            10000000.000000        exact, no drift
  0.5                    1            5000000.000000         exact, no drift
  0.25                   2            2500000.000000         exact, no drift
  0.2                    54           1999999.999678         DRIFT
  0.1                    55           999999.999839          DRIFT
  0.3333333333333333     54           3333333.333714         DRIFT
```

`exactFractionalBits()` computes the minimal $m$ such that $v \cdot 2^m$ is an integer —
**how many fractional bits the exact value actually needs.** `0.5` needs 1, `0.25` needs
2, `1.0` needs 0, and `0.1` needs **55**. The first three are exact and provably cannot
drift no matter how many times you add them; the rest round on every single addition.

**The derivation, and the reframe.** The standard textbook explanation is "floating point
is approximate". That is a description, not a cause. The cause is: a `double` has 52
mantissa bits, and $1/10$ has no finite binary expansion, so it needs infinitely many.
A value that needs ≤ 52 bits is exact forever; a value that needs 55 is rounded forever.
**Drift is a property of the input's bit-width, not of the loop's iteration count.** The
loop count only decides *how much* error accumulates, never *whether* any does.

### 2.5 `%.20f` is the wrong tool for showing this

```
  printf %.20f of 0.1      = 0.10000000000000000000   <- looks exact
  Double.toString(0.1)      = 0.1
  new BigDecimal(0.1)      = 0.1000000000000000055511151231257827021181583404541015625
  new BigDecimal("0.1")    = 0.1
```

Twenty decimal digits **cannot** display an error of $5.55 \times 10^{-18}$, which lives at
the 18th decimal place. So the most obvious way to demonstrate the problem produces a
result that looks like a refutation of it. `new BigDecimal(double)` is the view that shows
the truth, because it prints the *exact* value of the double.

**Rule: to show that a value is inexact, print its exact value. Never print it rounded.**

### 2.6 The fix, and a prediction of mine that was flatly wrong

```
  BigDecimal (exact)   124,145,506 ns   sum = 1000000.0   exact? true
  double               10,062,244 ns   sum = 999999.9998389754
  -> BigDecimal is 12x slower. A real cost, paid deliberately.

  BigDecimal + MathContext(20)   277,298,005 ns   sum = 1000000.0
  -> 2.2x SLOWER than exact, not faster.
```

I expected bounded precision to be *cheaper* than exact — "rounding to 20 digits is less
work than carrying unlimited digits". It is the opposite, by 2.2×. The reason: exact
addition of two small `BigDecimal`s stays inside a single `long`, so there is a fast
path; a `MathContext` must renormalise and re-round on **every** operation.
**"Bounded" is not a synonym for "cheaper"** — it is a promise about precision, and
promises have a price.

Also worth stating because it is a silent trap: `new BigDecimal(0.1)` and
`new BigDecimal("0.1")` are **different numbers** (see §2.5). The `double` constructor
faithfully preserves the double's error, so it "fixes" nothing while looking like a fix.

## 3. Counter-example — where the intuition breaks

**Intuition to break:** "a loop's complexity is the number of lines in its body."

Consider:

```java
// "one line"
for (int i = 0; i < n; i++) { f(i); }              // 1 statement,  O(n)

// "one line" too
for (int i = 0; i < n; i++) { for (int j = 0; j < n; j++) { g(i, j); } }  // 1 statement, O(n^2)
```

Both bodies are a single statement, and one is $n^2$. What distinguishes them is the
**exit condition**: `j < n` is re-evaluated once per `i`, so the number of times the
inner body runs is $n \times n$, not $n$. The count of *lines* is not a proxy for
anything; only the product of the loop-trip counts is.

The same counter-example kills the "O(1) per line" intuition in the other direction:

```java
int s = 0;
for (int i = 0; i < n; i++) s += arr[i];      // 2 lines, O(n)
for (int i = 0; i < n; i++) s += (i % 2 == 0) ? f(arr[i]) : g(arr[i]);   // still O(n)
```

No line is doing $O(n)$ work. **The loop's cost is a property of the trip counts, and
`%`, `/`, and `String` operations are the places where a single line hides an
asymptotic cost** — which is the bridge to Ch 04's $O(n^2)$ `+=` and to Ch 06's
recursion.

## 4. Anti-message

> **Anti-message:** "Floating point is inaccurate, so avoid `double` in loops."

Wrong in a way that costs real performance. `double` is exact for the overwhelming
majority of DSA inputs (§2.4: ≤ 52 fractional bits) and the error that does appear is
$O(n \cdot \epsilon)$ — a *relative* error of about $10^{-16}$, which no integer-answer
judge will ever see. The correct anti-message is much more specific:

> **Anti-message:** "`BigDecimal` is the safe replacement, so use it for any decimal
> arithmetic."

`BigDecimal` is ~12× slower *and* gives you a new trap (`new BigDecimal(0.1)` ≠
`new BigDecimal("0.1")`) *and* still has a `MathContext` decision to make. **The rule
is: use `double` unless the problem's *answers* are decimal and must round-trip
(money, exact text output), in which case use `BigDecimal` — and if the answers are
integers, do not use either, use `long`.**

## 5. What the book does not say out loud

1. **The book gives complexity as a table of loop shapes.** A table is a memorisation
   aid, and memorised complexity is why people write $O(\log n)$ for the subtraction GCD
   (§2.1) and get it wrong. §2.2's two-line proof is more work up front and correct
   forever.

2. **The book does not say that a loop's cost can be proved by counting *trips*, not
   by timing.** The step counters in `Ch05Gcd` are integers, so the complexity claim is
   machine-independent and reproducible on any computer. Timing is a sanity check; a
   trip count is a proof. This is the single most transferable habit in this chapter.

3. **The book does not connect the sentinel-controlled loop to the fact that the
   sentinel must be outside the valid range.** A `-1` sentinel works only because
   $-1 \notin [1, \text{ratings}]$. Pick `0` as a sentinel for a scale that starts at `1`
   and the loop silently reads one past the data. **A sentinel is a type-domain
   argument, not a magic number** — and it is the same class of mistake as using `0` as
   an array index or `-1` as a "not found" that collides with a legitimate `-1` in the
   data.

4. **The book never mentions that the *type* of the accumulator can invalidate the
   loop.** §2.4's whole result is that the loop is correct and the answer is not. A book
   that only discusses "does the loop terminate" leaves you with no way to notice that
   this is a different failure.

## 6. 🔄 What changed in Java 21 (delta vs the book's Java-8 baseline)

| Old (book) | Java 21 | Why it matters |
|---|---|---|
| `gcd(a, b)` with `double` intermediates | `Math.floorDiv` / `Math.floorMod` for negative-safe integer division | the §2.3 overflow trap is fixable with library calls, not by hand-written `a - (a/b)*b` |
| `for (int i = 0; i < n; i++)` as the only loop | `for (int c : collection)` for arrays/iterables, and the `int[]` form avoids the index entirely | bound-checking bugs of the Ch 03 kind disappear; but you *lose the index*, which is often what the algorithm needs |
| `double` drift discovered at the answer | `BigDecimal` + `MathContext` as an explicit precision *policy* (Ch 12 covers it properly) | the §2.6 finding stands: bounded precision is a promise, not a speedup |
| no way to count iterations portably | JFR (`jdk.ObjectAllocationSample`, `jdk.ThreadPark`) or `ThreadMXBean.getCurrentThreadAllocatedBytes` | the "count trips, don't time" habit from §5.2 becomes measurable in production |
| sentinel loops only | `Iterator.remove()` makes "remove while iterating" explicit and checked | removing from a collection by index during iteration was a bug class; now it throws instead |

**What has NOT changed:** Euclid is still $O(\log n)$, `double` is still base-2, the
subtraction GCD is still $O(\max/\min)$, and `%.20f` still cannot show a $10^{-18}$
error. **Every derivation in §2 is a language-independent fact.**

## 7. 🔗 Diary link

- [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)
  — the GCD step count (1.54 steps per bit) is the number to quote when justifying a
  time bound in a write-up, and §2.4's "drift is a property of the input's bit-width"
  is the line that separates integer arithmetic advice from float arithmetic advice.

## 8. Compiled and run — actual output

```
$ ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch05-loops/lab/Ch05Gcd.java

100,000 random pairs: euclid == subtraction, 0 mismatches
--- GCD of 48 and 18, three ways, counting every step ---
  subtraction : 4 steps
  euclid      : 3 steps
  result      : 6 (both implementations agree)

--- the subtraction loop is O(max/min), and min can be 1 ---
  (a, b)            subtraction   euclid
  48, 18                      4        3
  1000, 7                   148        3
  100000, 3               33335        2
  999983, 1              999982        1
  (999983, 1): the subtraction loop needs 999,982 subtractions to
  find a gcd of 1. euclid needs 1. That is the whole lesson.

--- euclid is O(log min): measured against fibonacci inputs ---
consecutive fibonacci numbers are the WORST case, and the reason is
derivable: every step is one subtraction, so the quotients are all 1.

  n        (fib, fib+1)      steps   log2 min  steps/log
  3        2, 3                  3       1.00       3.00
  6        8, 13                 6       3.00       2.00
  10       55, 89               10       5.78       1.73
  15       610, 987             15       9.25       1.62
  20       6765, 10946          20      12.72       1.57
  25       75025, 121393        25      16.20       1.54

  READ THE `steps` COLUMN FIRST: for (F_n, F_n+1) the step count is
  EXACTLY n. That is not a coincidence, it is a theorem: euclid on
  (F_n, F_n+1) quotients every step to 1, and the recursion
  steps(F_n) = 1 + steps(F_n-1) with steps(F_1) = 1, so steps = n.
  And F_n ~ phi^n grows EXPONENTIALLY in n, so n = log_phi(F_n).
  Exponentially growing input, logarithmically many steps => O(log min).
  The `steps/log2` column hovering near 1.5-1.6 is that constant;
  it is log2(phi) ~ 1.44, not an accident of measurement.
```

```
$ ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch05-loops/lab/Ch05NumericError.java

--- adding 0.1 ten million times ---
  sum            = 999999.9998389754
  expected       = 1000000.0000000000
  absolute error = 0.0001610246
  the loop is CORRECT. the TYPE cannot hold the value.

--- 0.1 is not 1/10, and printf will hide it from you ---
  printf %.20f of 0.1      = 0.10000000000000000000   <- looks exact
  Double.toString(0.1)      = 0.1
  new BigDecimal(0.1)      = 0.1000000000000000055511151231257827021181583404541015625
  new BigDecimal("0.1")    = 0.1
  0.1 is really 0.1000000000000000055511151231257827...
  so %.20f is the WRONG tool for showing this: the error is at the
  18th decimal place. Measure drift by comparing to the expected
  total, never by printing the value with more digits.

--- how many fractional BITS does each value really need? ---
  value                  exact frac bits after 10M adds         verdict
  1.0                    0            10000000.000000        exact, no drift
  0.5                    1            5000000.000000         exact, no drift
  0.25                   2            2500000.000000         exact, no drift
  0.2                    54           1999999.999678         DRIFT
  0.1                    55           999999.999839          DRIFT
  0.3333333333333333     54           3333333.333714         DRIFT
  0.5 needs 1 bit, 0.25 needs 2, 1.0 needs 0: adding them 10M times
  is exact arithmetic and cannot drift. 0.1 needs 55 bits, so 10M
  additions each round. the damage is a property of the INPUT
  (how many bits it needs), not of the loop (how many times).

--- the fix, and what it costs ---
  BigDecimal (exact)   124,145,506 ns   sum = 1000000.0   exact? true
  double               10,062,244 ns   sum = 999999.9998389754
  -> BigDecimal is 12x slower. A real cost, paid deliberately.

  --- and a prediction of mine that was flatly wrong ---
  I expected `MathContext` (bounded precision) to be FASTER than exact
  BigDecimal, on the grounds that rounding to 20 digits is less work than
  carrying unlimited digits. Measured:
  BigDecimal + MathContext(20)   277,298,005 ns   sum = 1000000.0
  -> 2.2x SLOWER than exact, not faster.
  Why: exact addition of two small BigDecimals stays in a single
  long, so it is a fast path. Bounded precision must renormalise and
  re-round on every operation. 'Bounded' costs work; it does not
  save it. (Both are ~10-25x the double, which is the number that matters.)
```

## 9. What I Got Wrong

- **I wrote "euclid needs 2" for `gcd(999983, 1)` in the lab's own explanatory prose,
  directly above a table that printed `1`.** The algorithm is right, the code is right,
  the sentence underneath the table was written from memory. Fixed to `1`. I have now
  done this three times in five chapters (§2.2 of Ch 03's card, this, and the `54`/`55`
  bit count below) — so it is a *pattern*, not a slip: **when a number is already on
  screen, quoting a different one from memory is never acceptable.**
- **My `isDyadic(0.5)` returned `false`.** The whole function was a bad question — every
  `double` is a dyadic rational, so "is it dyadic?" is vacuous, and my implementation
  answered it wrongly. The right question is *how many fractional bits does the exact
  value need*, which yields a number you can act on (0, 1, 2, …, 55) instead of a
  meaningless boolean. **When a predicate comes out constant, check whether the
  predicate is the problem.**
- **I predicted `MathContext` would be faster than exact `BigDecimal`, and it is 2.2×
  slower.** I did not delete the experiment. This is the most valuable measurement in
  the chapter, because the plausible-sounding version of the claim ("bounded precision
  is cheaper") is exactly the kind of advice that gets repeated without ever being
  measured.
- **My prose said `0.1` needs 54 fractional bits; the measurement says 55.** Same failure
  as the "euclid needs 2" line, in the same file, minutes later. The lab now prints the
  number and the prose refers to the printed value rather than repeating it.
- **I used `%.20f` to demonstrate a $10^{-18}$ error, and it printed
  `0.10000000000000000000`.** The demonstration refuted itself. Twenty decimal places
  cannot show an error in the 18th decimal place. The fix (`new BigDecimal(double)`,
  which prints the exact value) is now in the lab, and the general rule — *to show a
  value is inexact, print it exactly* — is in §2.5.

## 10. Open questions (to measure, not to guess)

- [ ] The §2.2 constant is ≈1.54 steps per bit and $\log_2\varphi \approx 1.44$. Find the
      input that maximises the *ratio* steps/bit, over all pairs below $10^6$. Is it
      Fibonacci, or is there something worse?
- [ ] Prove the subtraction loop's $O(\max/\min)$ by trip count instead of by the
      observation in §2.1, and find the exact input that is worst for a *fixed* $\max$
      (it is not $\min = 1$ if you forbid that, and the second-worst case is interesting).
- [ ] Is drift monotone in the iteration count? Accumulate `0.1` and print the error
      after 1k, 10k, 100k, 1M, 10M — is it $O(n)$, $O(n^2)$, or does it saturate? If it
      saturates, *why* would that be, given there is a 52-bit mantissa?
- [ ] `BigDecimal` is 12× slower here, but this is adding a *constant*. What is the cost
      of multiplying two 1000-digit `BigDecimal`s, and does the ratio to `double` change
      with size? "12×" is not a portable constant.
- [ ] `Math.floorMod` vs hand-written `a - (a / b) * b` for negative `a` and `b`: measure
      both, and find an input where the hand-written version is *wrong*, not just slower.
      That is the §2.3 trap, and it wants a witness.

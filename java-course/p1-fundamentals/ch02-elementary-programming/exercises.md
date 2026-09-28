# Ch 02 — Elementary Programming — Exercises

> Book: *Introduction to Java Programming and Data Structures, Comprehensive Version*
> (Y. Daniel Liang) — Chapter 2, "Elementary Programming".
>
> **Boundary rule:** book exercises stay here. Nothing in this chapter has been
> submitted to a judge, so nothing is promoted to `solutions/`. The **L3 tier carries
> the full Section D analysis inline**.

---

## L1 — Recall (close the book)

1. Give the *exact* set of rules that turn `b = b + 1` into a compile error on a
   `byte` but leave `b += 1` legal. Where does the cast come from?
2. Widening is implicit and lossless until one specific step of the ladder `byte →
   int → long → double`. Which rung has a gap, and what is the name of the
   discontinuity (the value below which every `int` is representable)?
3. `(int) 1e20` evaluates to `2147483647`. Is that an error, a wraparound, or a
   saturation? (It is a defined behaviour, not a tip-off — that is the point.)
4. What exactly does `Integer.MAX_VALUE + 1` equal, and which of the *JLS categories*
   does that behaviour belong to ("defined", "implementation-defined", "error")?
5. When does `Scanner.nextDouble()` depend on something other than the text you feed
   it? Name the mechanism and the fix, with one line of code.

## L2 — Apply (predict, run, then compare)

**E2.1 — The `+=` trap with a real value.** Without running, write the output of this
program, then run it (`lab/Ch02Narrowing.java` reproduces all of it), then say in one
sentence why `byte b = 127; b += 1;` and `b = b + 1;` behave differently.

```java
byte b = 127;
b += 1;
System.out.println(b);
```

**E2.2 — Sum with the *wrong* accumulator.** `sum = 0; for (i = 1; i <= 100000; i++)
sum += i;` — you know the true answer is `5_000_050_000` (Gauss). Predict which of
these four programs prints the truth and which print something else, *before* you
write a single line:

- `int sum`
- `float sum`
- `double sum`
- `long sum`

`int` overflows (true answer needs 33 bits). `double` happens to be exact here.
`float` loses whole millions. `long` is exact. Then explain why `double` is exact for
THIS `n` but cannot be trusted in general (that is L3's target).

**E2.3 — The missing `\n`, in your own words.** Using `lab/Ch02ScannerTraps.java`, the
empty `nextLine()` is returned *before* the user's data is ever reached. Draw the
position of the Scanner cursor after `nextInt()`, after the *first* `nextLine()`, and
after the *second*. One line each.

**E2.4 — Locale is a per-run variable.** Run `Ch02ScannerTraps.java` with
`-Duser.language=de -Duser.country=DE` and with `-Duser.language=en -Duser.country=US`.
Which line of output flips? Then answer: would flipping it on the judge cost you a
submission even if the judge's machine is in a different timezone but the same locale? Why does `timezone != locale` matter here?

**E2.5 — Quantity is not correctness.** The lab measures Scanner vs `FastScanner` at
52×. Which one of these two claims does 52× support? (a) "Scanner is too slow for
judged input", (b) "Scanner can misparse the same bytes on two machines". One is a
throughput fact, the other is a *correctness* fact, and only one of them is what
`Ch02ScannerTraps` demonstrates.

## L3 — Derive (full Section D analysis)

### L3.1 — Self-authored: sum of the first `n` squares, judged exactly

**Problem, restated in one sentence.** Given `0 ≤ n ≤ 10^6`, print $S(n) = \sum_{i=1}^n
i^2$ as an exact decimal integer — with no trailing `.0`, no rounding, no wrap.

**Constraints, quoted, and why they are the story.** $0 \le n \le 10^6$. The answer at
$n = 10^6$ is $S = 10^6 \cdot 1{,}000{,}001 \cdot 2{,}000{,}001 / 6 \approx 3.33 \times
10^{17}$. Three facts collide at this scale, each from Ch 02's own message:

1. $3.33 \times 10^{17} > 2^{31}$ → `int` cannot hold the answer at **any** large $n$.
2. $3.33 \times 10^{17} < 2^{63}$ → `long` can.
3. $3.33 \times 10^{17} \gg 2^{53} \approx 9.0 \times 10^{15}$ → **even `double` cannot
   hold it exactly** — every `double` at that magnitude is a multiple of
   $2^{58-53} = 2^{5} = 32$.

So the type choice, which Ch 02 frames as a "grammar" matter, is here a *correctness*
matter visible to the judge. That is the derivation this L3 exists for.

**Brute force, and why it is too slow (and which brute force is not).**

```java
// A: loop, long accumulator — CORRECT, O(n)
long s = 0;
for (int i = 1; i <= n; i++) s += (long) i * i;

// B: loop, int accumulator — WRONG after n ≈ 1848
// C: loop, double accumulator — WRONG after n ≈ 300,000
// D: loop, float accumulator — WRONG in the thousands
```

`A` is a correct reference: it is $O(n)$ time, $O(1)$ space, and at $n = 10^6$ takes
~2 ms. The reason to still derive a closed form is the judge's *scale-typing*: a "Sum
of Squares" problem sheet that says $n \le 10^{12}$ makes `A` a **timeout**, and the
whole point of the derivation is that the answer then costs $O(1)$ — you are not
allowed to stop at "it works for $10^6$".

**The improvement, derived.** The inductive identity
$$ \sum_{i=1}^{n} i^2 = \frac{n(n+1)(2n+1)}{6} $$
is standard; the *interesting* step for this chapter is **not** that it exists but
that it must be evaluated without the intermediate product overflowing `int`:
`n(n+1)(2n+1)` at $n = 10^6$ is $\approx 2.0 \times 10^{18}$, which fits
$2^{63} \approx 9.2 \times 10^{18}$ but not $2^{31}$. So the formula line must be:

```java
long s = (long) n * (n + 1) * (2 * n + 1) / 6;   // the (long) on the FIRST factor only
```

The single `(long)` on the first factor is load-bearing: Java evaluates
`a * b * c` left to right, so `n * (n + 1)` is computed in `int` *first* if `n` is an
`int`, and `1848 * 1849` already overflows before the `(long)` ever gets a chance.
**That is the same Ch 02 rule as `b + 1` on a `byte`: the arithmetic happens in the
type of the operands, and a cast later in the expression does not widen a result
already computed.**

**Correctness argument.**

> **Claim (Gauss–Faulhaber identity for squares).** For every integer $n \ge 0$,
> $\sum_{i=1}^{n} i^2 = n(n+1)(2n+1)/6$.
>
> *Proof by induction.* Base: $n = 0$ gives $0 = 0 \cdot 1 \cdot 1 / 6$. Step:
> assume the identity for $n$; then
> $\sum_{i=1}^{n+1} i^2 = \frac{n(n+1)(2n+1)}{6} + (n+1)^2
> = \frac{(n+1)\big(n(2n+1) + 6(n+1)\big)}{6}
> = \frac{(n+1)(2n^2 + 7n + 6)}{6}
> = \frac{(n+1)(n+2)(2n+3)}{6}$, which is the identity at $n+1$. ∎
>
> **Claim (integer division does not truncate here).** $n(n+1)(2n+1)$ is divisible by
> 6 for every integer $n$ (one of $n, n+1$ is even; one of $n, n+1, 2n+1$ is divisible
> by 3). So `... / 6` is exact division, and the `long` evaluation never loses a
> fraction it was going to drop. The division-then-truncate hazard that would turn
> this into a bug is absent precisely because of that divisibility fact — which is
> itself a derivable claim, not a coincidence.

**Complexity, with the dominant term.** Loop `A`: $O(n)$ time (the dominant term, one
multiply-add per $i$), $O(1)$ space. Formula: $O(1)$ time, $O(1)$ space. The entire
difference between the two is the drop from $O(n)$ to $O(1)$ — the motivation for the
derivation — but the *constant* also beats `A`: one multiply-add, no loop-carried
dependence.

**Test table** ($S(n) = n(n+1)(2n+1)/6$, all verified against loop `A`):

| # | `n` | exact $S(n)$ | `int` loop | `float` loop | `double` loop | why this row |
|---|---|---|---|---|---|---|
| 1 | `0` | `0` | ok | ok | ok | the stated lower bound; any formula with a bare `2*n-1` style breaks here |
| 2 | `1` | `1` | ok | ok | ok | trivial |
| 3 | `5` | `55` | ok | ok | ok | hand-checkable |
| 4 | `1848` | `2105411924` | **ok** | `2.1054131E9` WRONG | ok | the value *just* fits `int` — it is **not** the wrap point |
| 5 | `300_000` | `9000045000050000` | WRONG | `9.0000363E15` WRONG | **ok** | `double` still exact here (below $2^{53}$) |
| 6 | `1_000_000` | `333333833333500000` | WRONG | WRONG | WRONG | the quoted constraint; only `long`+`long` is exact |

**The harness printed:**

```
crossovers (first n where each accumulator disagrees with the EXACT long sum):
  int:    n=1861   (first n where the true sum no longer fits an int)
  float:  n=371    (float sum rounds; error becomes visible)
  double: n=300083 (double sum crosses 2^53)
  note: comparing int sum against (int)longSum NEVER mismatches, because int
  arithmetic is exactly long arithmetic mod 2^32 — wrapping is not a bug, it is
  the type's own arithmetic.

n          exact                    int          float        double
--------------------------------------------------------------------------------
1848       2105411924               ok           2.1054131E9 WRONG ok
300000     9000045000050000         1160824144 WRONG 9.0000363E15 WRONG ok
1000000    333333833333500000       -143234976 WRONG 3.33382E17 WRONG 3.3333383333312755E17 WRONG
```

Everything wrong here is a **language law** made visible, not a bug in some program's
logic: `int` wraps at $2^{31}$ (n=1861, not "near 1848" — see below), `float` rounds
above $2^{24}$ so it dies in the hundreds, and `double` holds every integer
exactly only up to $2^{53}=9{.}007\times10^{15}$, then silently rounds — at
$n=10^6$ it prints `333333833333127.55×10^3`, off by **372,448**, with no exception,
no warning, and a value that still *looks* like the answer.

**What I Got Wrong (before/while building this).**
1. **I wrote the formula as `n * (n + 1) * (2 * n + 1) / 6` with `n` an `int` and
   declared only the *result* `long`.** The Java compiler accepted it, and my own
   early table's row for `n = 1000000` was wrong while I stared at it waiting for a
   warning that never came. Fixing took remembering *my own* Ch 02 line: arithmetic
   happens in the operand type BEFORE the cast or assignment. The cast must be on the
   first factor.
2. **I "recalled" three crossover points and every big-`n` value without running.**
   I wrote `int` wraps "at n=1848" (the true value `2105411924` is smaller than
   `Integer.MAX_VALUE`, so it fits!), `float` at 2641 (real: 371), `double` at
   300000 (real: 300083), `int` at $10^6$ = `-1941265411` (real: `-143234976`),
   `double` error "500" (real: `372448`). **This is the third time this month I
   decided a benchmark column before running it** (also Ch 08 matmul, Ch 07 dispatch)
   — the numbers that survive are the ones the harness prints. Not one of my seven
   recalled values survived.
3. **My float loop `s += (float)(i * i)` was itself a Ch 02 bug.** The inner `i * i`
   is computed in `int` first and overflows *before* the cast widens it, so the float
   column was measuring "int overflow, then float rounding", twice wrong. It printed
   `1.68E13` for $n = 10^6$ and looked like a meaningful finding. The fix,
   `(float) i * i`, is the same lesson as item 1: **the operator's type, not the
   variable's, decides where the arithmetic happens.**
4. **I wanted to conclude "int wrap = a bug a judge catches".** The harness shows
   `int` arithmetic is *exactly* long arithmetic mod $2^{32}$, so an `int` sum always
   agrees with `(int) longSum` by construction — it wraps "correctly", then disagrees
   with reality. Calling wrapping a bug is wrong; calling it *the type's arithmetic*
   is the honest statement, and it is the stronger one (it is why `long` is the
   reference, not `int`).

**Open questions.**
- [ ] Sum of cubes: derive $\sum i^3$ the same way (it is $(n(n+1)/2)^2$). Repeat the
      five-tool table. At which $n$ does `long` no longer fit, and which type *would*?
- [ ] A judge variant with $n \le 10^{12}$: the closed form is $O(1)$, but `long`
      overflows at $n = 3{,}024{,}617$ for squares and $n = 77{,}936$ for cubes
      (computed by search). Would `BigInteger` or modular arithmetic be the right
      tool? (link Ch 09.)
- [ ] Measure the `int` wrap with `Math.multiplyExact` instead of guessing: which
      row of the table does the exception catch, and what is `multiplyExact`'s role in
      Ch 07's dispatch cost discussion?

### L3.2 — The `nextLine()` cursor, drawn to scale

**Statement (type-domain argument, Ch 02 scope).** Write `readPerson(Scanner)` that
reads `name`, `age`, returning a `String`, so the empty-`nextLine()` trap cannot
happen. The *derivation* is the contract: "after `nextInt()`, the cursor stands on the
newline; `nextLine()` consumes the newline; the *user's* line can only be obtained by a
second `nextLine()`." The correctness claim is that consuming the newline once is a
*type* fact (a line is `data '\n'`), and so the fix is structural — never "add a new
`nextLine()` here" by eyeball.

**Space/time.** $O(1)$ per read. The interesting point is that there are **three**
`Scanner` conventions — `next()` (token), `nextInt()` (parsed token), `nextLine()`
(rest of line) — and the fix is to pick one convention for the whole program (here:
all `nextLine()` + `parseInt`), which is itself the Ch 02 "one convention, not three"
lesson.

**What I Got Wrong.** My first `Ch02ScannerTraps` had a `reRead()` helper that
*hard-coded* the answer `"hello world"` instead of calling `nextLine()` again — a
staged, unmeasured "reproduction". It is removed; the honest version reads the input
twice and prints what was actually read. **A demonstration that prints a value it
invented is theater, not evidence** — and it would have stayed until someone reread
the file. This is the same trap flagged in the chapter's card.
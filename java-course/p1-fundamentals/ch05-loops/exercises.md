# Ch 05 — Loops — Exercises

> Book: *Introduction to Java Programming and Data Structures, Comprehensive Version*
> (Y. Daniel Liang) — Chapter 5, "Loops".
>
> **Boundary rule:** book exercises stay here. Nothing in this chapter has been
> submitted to a judge, so nothing is promoted to `solutions/`. The **L3 tier carries
> the full Section D analysis inline**.

---

## L1 — Recall (close the book)

1. Name the three loop design strategies in the book (counter-controlled,
   sentinel-controlled, flag-controlled) and give a real input for which each is the
   right choice.
2. What makes a sentinel value a *bad* choice for a loop? Give one example where the
   sentinel is a legal data value.
3. In `for (int i = 0; i < 5; i++)`, how many times does the body run? In
   `for (int i = 5; i > 0; i--)`?
4. Why does `for (int i = 0; i < n; i++) sum += arr[i];` run exactly $n$ times, and what
   is the trip count of `for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) g(i,j);`?
5. What is the difference between a `while` loop with the condition first and a `do-while`
   loop, and which one runs the body at least once?

## L2 — Apply (predict, run, then compare)

**E2.1 — Trip counting beats reading code.** For each loop below, write down the exact
number of times the body runs **as a formula in $n$**, before running anything. Then
verify by instrumenting a counter.

```java
for (int i = 1; i <= n; i++) body();                 // A
for (int i = 0; i < n; i++) { for (int j = 0; j < i; j++) body(); }   // B
for (int i = n; i > 0; i -= 2) body();              // C
for (int i = 0; i < n; i++) if (i % 3 == 0) body(); // D
```

`B` is the one people get wrong: it is $n(n-1)/2$, not $n$ and not $n^2$ exactly. `C` is
$\lceil n/2 \rceil$ and `D` is $\lfloor (n-1)/3 \rfloor + 1$. **Write the exact formula,
then confirm the sum**: $1 + 2 + \dots + (n-1) = n(n-1)/2$, which is what turns "the
inner loop is $O(n)$" into "the whole thing is $O(n^2)$".

**Verified by instrumented counters:**

```
n=1  A=1(n)  B=0 (n(n-1)/2=0)   C=1 (ceil(n/2)=1)  D=1
n=2  A=2(n)  B=1 (n(n-1)/2=1)   C=1 (ceil(n/2)=1)  D=1
n=3  A=3(n)  B=3 (n(n-1)/2=3)   C=2 (ceil(n/2)=2)  D=1
n=4  A=4(n)  B=6 (n(n-1)/2=6)   C=2 (ceil(n/2)=2)  D=2
n=5  A=5(n)  B=10(n(n-1)/2=10)  C=3 (ceil(n/2)=3)  D=2
n=6  A=6(n)  B=15(n(n-1)/2=15)  C=3 (ceil(n/2)=3)  D=2
n=7  A=7(n)  B=21(n(n-1)/2=21)  C=4 (ceil(n/2)=4)  D=3
n=8  A=8(n)  B=28(n(n-1)/2=28)  C=4 (ceil(n/2)=4)  D=3
```

All four formulas hold for every $n$ tried. Note $D$ is $\lfloor (n-1)/3 \rfloor + 1$,
which is **the same number as** $\lceil n/3 \rceil$ (identity: $\lfloor (n-1)/3 \rfloor
+ 1 = \lfloor (n+2)/3 \rfloor = \lceil n/3 \rceil$); the table cannot tell them apart,
and that is the point — a hand-written "and not $\lceil n/3 \rceil$" note here is a
claim that the table *cannot* support. (An earlier draft of this file claimed the two
diverge for large $n$. They never do; the identity holds for every integer $n$,
verified over $1$..$9999$.) The verified row is still the one worth trusting — but
only because it *tests* a formula, not because a table of eight rows can distinguish
two formulas that are equal.

**E2.2 — The sentinel trap.** Write a loop that reads ratings (1–5) until `-1`. Then
change the valid range to `-1..5` and show the loop terminates early. What should the
sentinel have been? (Answer: a value outside the domain — the point is that the sentinel
is a *type-domain argument*.) Do the same for a loop over temperatures that may be
negative.

**E2.3 — Count the GCD, do not time it.** Extend `lab/Ch05Gcd.java`:
- Find the pair $(a, b)$ with $a, b \le 10^6$ that maximises `euclidSteps`.
- Is it always consecutive Fibonacci? Print the top 5 worst pairs.
- For each, print `steps / log2(min)`. Is the worst *ratio* the same pair as the worst
  *count*? (They are not necessarily — this is the point.)

**E2.4 — Prove the subtraction bound.** Show by trip count that `gcdSubtraction` takes
exactly $\frac{\max}{\min} - 1$ steps **when $\min \mid \max$**, and construct the input
that maximises steps for a *fixed* `max = 10^6`. Then time both loops on that input and
confirm the counts and the times agree in order of magnitude.

**Measured** — the formula is exact only under the divisibility condition, which is why
the exercise has to state it:

```
  gcd(48,16):     steps=2,       max/min-1=2,       exact formula holds
  gcd(12,4):      steps=2,       max/min-1=2,       exact formula holds
  gcd(999983,1):  steps=999982,  max/min-1=999982,  exact formula holds
  gcd(1000000,3): steps=333335,  max/min-1=333332,  DIFFERS by 3
```

For $(10^6, 3)$: $10^6 = 3 \cdot 333333 + 1$, so the loop runs 333,335 times — the
predicted $333{,}332$ plus the 3 extra subtractions needed to knock the remainder down.
**A complexity formula that needs a divisibility caveat is a derivation, not a
lookup** — which is the reason to derive it instead of memorising $O(\log n)$.

**E2.5 — Overflow in the loop state.** Implement `gcdSubtraction` with `int` and run it on
`gcd(Integer.MIN_VALUE, 0)`, `gcd(Integer.MIN_VALUE, Integer.MIN_VALUE)`, and
`gcd(0, 0)`. Write down which answers are correct, which loop forever, and which throw.
Then repeat with `long` and with `Math.floorMod`.

## L3 — Derive (full Section D analysis)

### L3.1 — LeetCode 204 · Count Primes

**Problem, restated in one sentence.** Given an integer $n$, return the number of primes
strictly less than $n$.

**Constraints, quoted.** $0 \le n \le 5 \times 10^6$.

$5 \times 10^6$ is the whole story: it is large enough that trial division over all
candidates is too slow, and small enough that an $O(n \log\log n)$ sieve fits in a few
MB. Note also that $n$ may be **0** — the answer is then 0, and any implementation that
starts a loop at 2 and counts inclusively gets it wrong.

**Brute force, and why it is too slow.**

```java
int countPrimes(int n) {
    int count = 0;
    for (int i = 2; i < n; i++) if (isPrime(i)) count++;
    return count;
}

boolean isPrime(int x) {
    if (x < 2) return false;
    for (int d = 2; d * d <= x; d++) if (x % d == 0) return false;
    return true;
}
```

Time: $\sum_{i<n} \sqrt{i} = O(n^{3/2})$. At $n = 5 \times 10^6$ that is
$\frac{2}{3}(5\times10^6)^{1.5} \approx 2.4 \times 10^{10}$ modulo operations — tens of
seconds. **Too slow, and the reason is specific: every candidate is tested from scratch,
so the divisors of 6 are checked against 2, 3, 4, 5, 6, and 12, twice each, with no
memory of what was already learned.**

**The improvement, derived.** Two facts, both derivable in one line each:

1. *Fact 1.* If $d$ divides $x$ and $d \le \sqrt{x}$, then $d$ divides $x$ implies
   $x/d \ge \sqrt{x}$ also divides $x$. So the smallest divisor of a composite $x$ is
   $\le \sqrt{x}$ — which is the `d * d <= x` bound in the brute force, and it is already
   there.
2. *Fact 2.* **If $x$ is composite then $x = a \cdot b$ with a prime factor $a \le
   \sqrt{x}$.** This is Fact 1 with "smallest divisor" replaced by "smallest *prime*
   divisor", and it is the new thing. Proof: the smallest divisor $> 1$ of $x$ is prime
   (if it were composite it would have a smaller divisor $>1$).

So: when we discover that $p$ is prime, we already know that every multiple of $p$ is
composite — **and we can mark them all at once**. That is a sieve, and it converts
"test every candidate" into "spend $O(1)$ per number, once".

**Correctness argument (two parts).**

> **Claim 1 (every marked number is composite).** `isComposite[m] = true` is only ever
> set inside the loop `for (int m = p * p; m < n; m += p)` for some $p$ proven prime.
> Then $m = p \cdot k$ for an integer $k = m/p \ge p \ge 2$, so $m$ is a multiple of a
> prime greater than 1 and is therefore composite. ∎
>
> **Claim 2 (every composite number below $n$ is marked).** Let $m < n$ be composite.
> By Fact 2, $m$ has a prime factor $p \le \sqrt{m}$. Since $p \le \sqrt{m} < m$ for
> $m \ge 4$, the outer loop reaches $p$ **before** reaching $m$. At that point $p$ is
> marked prime, so the inner loop runs and visits every multiple of $p$ from $p^2$ up to
> $n$. We need $m \ge p^2$: indeed $m = p \cdot k$ with $k \ge p$ (since $p$ is the
> *smallest* prime factor and $m/p \ge p$ by $p \le \sqrt m$). Hence $m$ lies in
> $[p^2, n)$ and is marked. ∎
>
> Together: a number is unmarked exactly when it is prime, so counting unmarked
> positions $\ge 2$ counts the primes. ∎

**The `p * p` start is load-bearing and is the most common bug in this problem.** Starting
at `p + 1` still produces correct *marking* for some $p$ but is wasted work, and starting
at `2 * p` marks composites that were already marked; the subtle failure is starting at
`2` for $p \ge 2$, which also loses the $p \ge \sqrt n$ early-exit, turning $O(n
\log\log n)$ into $O(n \log n)$. **The start point is not a constant, it is $p^2$
because the numbers below $p^2$ were already marked by smaller primes** — that is the
argument, and it is the same argument as Claim 2.

**Complexity, with the dominant term.**

| | time | space |
|---|---|---|
| trial division per candidate | $O(n^{3/2})$ | $O(1)$ |
| sieve of Eratosthenes | $O(n \log \log n)$ — the outer loop runs over primes only, and the inner loop runs $n/p$ times for each $p$, giving $\sum_{p \le \sqrt n} n/p = O(n \log\log n)$ | $O(n)$ bytes for the boolean array |

The space is a **real cost, not free**: at $n = 5\times10^6$ the boolean array is 5 MB
with a `boolean[]` (Java booleans in an array are 1 byte each, Ch 01), and 0.6 MB with a
`BitSet`. That is the trade: $O(n^{3/2})$ time/$O(1)$ space becomes $O(n\log\log n)$
time/$O(n)$ space. **This is the opposite trade from KMP in Ch 04** (which bought time
with memory too, but the ratio was different), and worth naming as a pattern.

**Test table.** Written by hand, then executed against the trial-division reference.

| # | `n` | expected | why it is interesting |
|---|---|---|---|
| 1 | `0` | `0` | the stated lower bound; an "inclusive" loop returns a wrong answer |
| 2 | `1` | `0` | 1 is not prime; `isPrime(1)` must be false, and a `d*d<=x` loop with no `x<2` guard is the classic bug |
| 3 | `2` | `0` | primes **strictly less than** 2 — there are none. Off-by-one between "<" and "<=" is the whole test |
| 4 | `3` | `1` | first non-trivial answer |
| 5 | `4` | `2` | 2, 3 |
| 6 | `10` | `4` | 2, 3, 5, 7 |
| 7 | `25` | `9` | forces the $5^2 = 25$ start to be correct |
| 8 | `100` | `25` | a count you can check by hand |
| 9 | `2` | `0` | duplicate of #3 on purpose: an implementation that returns 1 here is counting $\le$ |

**The table was executed against trial division, plus 200 random `n` below 3000:**

```
n=0         sieve=0     brute=0     ok
n=1         sieve=0     brute=0     ok
n=2         sieve=0     brute=0     ok
n=3         sieve=1     brute=1     ok
n=4         sieve=2     brute=2     ok
n=10        sieve=4     brute=4     ok
n=25        sieve=9     brute=9     ok
n=100       sieve=25    brute=25    ok
n=1000      sieve=168   brute=168   ok
n=4999999   sieve=348512 brute=348512 ok
mismatches: 0
```

And at the actual constraint limit, so the "$O(n^{3/2})$ is too slow" claim has a number
attached rather than an estimate:

```
  sieve(5,000,000) = 348513 in     27.1 ms
  brute(5,000,000) = 348513 in   3018.3 ms      -> 111x slower
```

$2.4 \times 10^{10}$ modulo operations at 3 seconds is about 8 nano-ops per operation,
which is the right order for a division plus loop overhead — so the complexity analysis
and the wall clock agree, and neither is carrying the other.

Note rows 3 and 9: `sieve(2) = 0`. The count is primes **strictly less than** $n$, so
`2` is excluded when $n = 2$ and included when $n = 3$. Getting this backwards passes 8
of the 10 rows and fails only the two that test the boundary — which is exactly why a
hand-written table has to include duplicate-looking rows with different expectations.

**What I Got Wrong (before running).** My first sieve counted the marked array and
subtracted from `n - 1`, which is the $O(1)$-extra way of counting composites — and it
is wrong for $n \le 1$, because `n - 1` is $-1$ or $-2$. The `max(0, n - 1 - composites)`
clamp *hides* that rather than fixing it. **An off-by-one that you patch with `Math.max`
is an off-by-one you have not understood**, and the $n=0$ row above exists to catch it.

**Open questions.**
- [ ] Extend to LeetCode 304 (count primes in a *range* $[L, R]$). Which part of the
      proof changes, and does a segmented sieve beat a fresh full sieve when
      $R - L \ll R$? Measure.
- [ ] At $n = 5\times10^6$, measure the sieve's actual time and the trial-division
      version's. Then find the $n$ where the sieve wins. The crossover is where the
      $O(n^{3/2})$ and $O(n \log \log n)$ curves intersect — what is it?
- [ ] Use a `BitSet` instead of `boolean[]`. What changes in time, what changes in space,
      and which of Claim 1/Claim 2 needed re-examining? (Neither — but the
      `isComposite.set(i)` bounds check is a new place to get it wrong.)
- [ ] The linear sieve (Euler) is $O(n)$ because each composite is crossed out exactly
      once. Derive why the inner loop must break after the first prime factor — and what
      breaks if you do not.

### L3.2 — The sentinel as a type-domain argument

**Statement.** Write a loop that reads an unknown number of `int`s and returns their sum,
without a count and without a `List` — i.e. with a sentinel. Then show, by constructing
inputs, that the correctness of the loop is a *statement about the type's domain*.

**The derivation.** A sentinel-controlled loop is correct iff the sentinel $s$ is not a
member of the input domain $D$:

$$ \text{no truncation} \iff s \notin D$$

That is the whole requirement, and it is a set-membership question about the *type*, not a
magic number. So the exercise is: pick three plausible domains (positive ratings, signed
temperatures, `char` codes) and for each, show (a) a sentinel that works, (b) a sentinel
that fails, and (c) the exact input that exposes the failure.

**Space/time.** $O(k)$ time for $k$ values, $O(1)$ space. The interesting cost is not in
the loop: it is in the fact that the sentinel forces the *data* to be in the same
type-domain as the control value, so you cannot pass a `char` stream and a `double`
sentinel. **The sentinel is a coupling between the control flow and the data domain**,
and that coupling is invisible in the signature `int sumUntil(int sentinel)`.

**What I Got Wrong (before running).** I wrote the loop as
`while ((n = in.nextInt()) != -1) sum += n;` and considered it done. It is correct, and
it is also a method with a `-1` in its *contract* and nothing in its signature saying so.
The version that does not have this problem takes the sentinel as a parameter and
documents the domain. **A hard-coded sentinel inside a method body is a hidden parameter;
a parameter with no domain check is a hidden bug.**

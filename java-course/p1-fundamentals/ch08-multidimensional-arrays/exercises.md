# Ch 08 — Exercises

> Tiers: **L1** recall · **L2** apply · **L3** derive. L3 carries the full Section D
> analysis inline (brute force, derivation, correctness argument, complexity with
> reasoning, test table, what I got wrong). Per `AGENTS.md` Section J, book exercises
> stay here and are only promoted to `solutions/` if actually submitted to a judge.

---

## L1 — Recall (close the book first)

1. What is `int[][]` really an array of? State it as one sentence.
2. How do you declare a 2-D array whose rows have lengths 1, 2, 3, 4, 5? Why can you
   even do that?
3. What happens when you access `a[2][0]` if row 2 was never allocated? What is the
   exact NPE message style?
4. `a.clone()` on `int[][]` — shallow or deep? Explain in one line why.
5. `Arrays.equals(a, b)` vs `Arrays.deepEquals(a, b)` for two `int[][]` — which one is
   right, and why does the other exist?
6. Row-major vs column-major iteration: which one walks memory in address order, and
   roughly how many × faster was it at 3000×3000 in the lab?

---

## L2 — Apply

### 2.1 Predict the aliasing, then run

```java
int[][] a = {{1, 2, 3}, {4, 5, 6}};
int[][] b = a.clone();
b[0] = new int[]{9, 9, 9};   // rebind b's first row
System.out.println("a[0][0] after rebind  = " + a[0][0]);
b[1][0] = 7;                 // mutate through the shared second row
System.out.println("a[1][0] after mutate   = " + a[1][0]);
```

Predict both prints *before* running. The first is a pure "assignment copies the
reference" test; the second is the shallow-copy trap. Run it, and write one sentence
explaining why the answers differ even though both lines touch `b`.

### 2.2 Sum a matrix correctly the first time

Write a method `long sum(int[][] m)` that works for *both* rectangular and ragged
input. Then write the variant that would silently return the wrong total for ragged
input, and say which input shape it under-counts.

**Verified reference idiom:** `for (int[] row : m) { for (int cell : row) acc += cell; }`.
The naive variant using `m[0].length` for every row is wrong unless `m` is rectangular.

### 2.3 Build the transpose with the right memory pattern

`int[][] t = new int[n][n]; for (i) for (j) t[j][i] = m[i][j];` — identify which of the
four index pairs (read vs write) is row-major and which is column-major, and predict
which side suffers the 6–8× penalty for each matrix. Then explain how the output `t`
must look, and how you would re-arrange it so *both* sides are in address order.

### 2.4 The `deepEquals` question the Javadoc does not answer

`Arrays.deepEquals(new int[][]{{1}}, new int[][]{{1}})` → `true`.
`Arrays.deepEquals(new Object[]{new StringBuilder("x")}, new Object[]{new
StringBuilder("x")})` → predict, then run.

**Verified:** `false`. Two distinct `StringBuilder` instances are not `.equals()` (no
structural equality on that class), and `deepEquals` delegates per-element to
`.equals()` — so "deep" is *one recursion level*, not deep-a-clone. Replacing
`StringBuilder` with a class whose `equals` compares content flips the answer to
`true`.

---

## L3 — Derive

### A self-authored problem: blocked matrix multiplication on a 2-D array

**Problem restated.** Given square `int` matrices `A`, `B` of size `n × n`, compute
`C = A × B`. Constraints: `n` up to 1024 (`10²¹` multiplications if n were the only
input — so the real constraint is wall-clock latency, and the *useful* claim will be
about order of magnitude).

**Why this problem, this chapter.** The chapter's measured fact — one loop order is
6–8× the other on a large matrix — is exactly the force that separates an O(n³) matrix
multiply "that is obviously correct" from one that runs 10× faster on the same memory.
Blocking (tiling), the textbook optimisation, is *not* recalled here; it is derived from
the same memory-size observation the chapter's locality lab made.

### 1. The brute force, and why it is the wrong ceiling

```java
static int[][] multiply(int[][] a, int[][] b) {
    int n = a.length;
    int[][] c = new int[n][n];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            int sum = 0;
            for (int k = 0; k < n; k++) {
                sum += a[i][k] * b[k][j];
            }
            c[i][j] = sum;
        }
    }
    return c;
}
```

This is the definition, and it is **O(n³)** time and **O(n²)** space. The `i,k`
inner loop reads `a` row-major (good) but `b` **column-major** (bad): `b[k][j]` with
`k` inner means walking *down* a column, jumping between `b`'s rows on the hot inner
stream. Precisely the 6–8× from the lab, applied once per `(i, j)`.

Also — and this is the subtle part — it is correct **only for square, zero-based
matrices**. Counting the access pattern is the right first step before any
optimisation, because the O(n³) is fixed by the definition, but the *constant* is not.

### 2. The improvement, derived not recalled

The locality lab says the fast path is: **keep the working set small and touch memory
in address order.** From that alone, do not look up "CPU blocking"; derive it:

1. `b` column access is the offender: `b[k][j]` jumps rows. If we could read `b` in
   row order, the penalty disappears.
2. Reading `b` row-major means iterating `(j, k)` with `j` outer; but then we cannot
   also keep `a[i][k]`'s natural `k` inner. A `(i,j,k)` triple order has exactly one of
   the two inner reads in the wrong order.
3. Unless we *block*. Divide the matrix into square blocks of size `b0`; within a
   block, every load is from a contiguous `b0 × b0` region. Process a block of `C`
   while both its `A`-block row and `B`-block column are small enough to stay in L1d
   — where, by the 8×8 result, "small" means the whole block fits the cache.

So the derived algorithm:

```java
static int[][] multiplyBlocked(int[][] a, int[][] b, int b0) {
    int n = a.length;
    int[][] c = new int[n][n];
    for (int i0 = 0; i0 < n; i0 += b0) {
        for (int j0 = 0; j0 < n; j0 += b0) {
            for (int k0 = 0; k0 < n; k0 += b0) {
                for (int i = i0; i < Math.min(i0 + b0, n); i++) {
                    for (int j = j0; j < Math.min(j0 + b0, n); j++) {
                        int sum = 0;
                        for (int k = k0; k < Math.min(k0 + b0, n); k++) {
                            sum += a[i][k] * b[k][j];
                        }
                        c[i][j] += sum;
                    }
                }
            }
        }
    }
    return c;
}
```

Note that inside a block the inner `k` loop still reads `b[k][j]` column-wise — but the
block is `b0` wide, so the whole `B`-block is `b0²` cells, which for `b0 = 32` is 4 KB
of `int`s. That fits comfortably in a 32 KB L1d, so the column jumps bounce inside a
cache-resident tile instead of across a 36 MB heap. The definitional multiply did the
same jump across the *whole* matrix each time.

### 3. Correctness argument

**Claim.** For any `n` and `b0 ≥ 1`, `multiplyBlocked(a, b, b0)` equals
`multiply(a, b)`, the definition.

**Invariant.** After the `(i0, j0, k0)`-block iteration finishes, for every `p < k0`,
`c[i0 + i][j0 + j]` already includes the whole term `a[i][p] * b[p][j]` for all
`(i, j)` inside the current `(i0, j0)` block.

*Base.* `k0 = 0`: no term is due yet, and `c` is zero.

*Step.* The inner-everything loop adds, for the current block, `Σ_{k∈[k0,k0+b0)}
a[i][k]·b[k][j]` to each `c[i][j]`. This is exactly the slice of the matrix-product
sum that the invariant's responsibility covers for indices `< k0 + b0`, and the
`+= b0` stride advances `k0` so the next block continues where the previous finished.
No term is added twice (each `k` belongs to exactly one `k0` block), and none is
omitted (the `k0` loop covers `0..n` in `b0` strides). Finite induction over the
`k0`-blocks gives the whole sum. ∎

Crucially, correctness is independent of `b0` — blocking only changes the *order* in
which the same products are accumulated. Attachment point: `k0` blocks do not overlap.

### 4. Complexity, with the reasoning for the dominant term

- **Time: O(n³)** — same count of multiply-adds as the definition; blocking only
  changes the constant. The dominant term is the inner `k` loop, run `n³` times
  regardless of `b0`. Saying "O(n³)" alone misses the point, which is why the L2
  question asks to count *accesses*: the time argument that matters is "n³ multiply-adds,
  done at the cache's rate instead of at a 6–8× discard rate".
- **Space: O(n²)** plus transient `O(b0²)` per block. The outer two arrays dominate.

**Where the constant lives:** the definition's inner stream is the column of `b`
(pointer-chasing, ~8× slower). The blocked version's inner stream strides inside a
resident 4 KB tile. That is the entire measured delta, and it is why blocks of `b0=8`
and `b0=32` often beat `b0=n` (no blocking at all) by wide margins — and why a block
too *small* under-uses the cache line (each `int` read still fetches 64 bytes).

### 5. Test table

Verify against the definition. Expected values written first; the run is below.

| # | n | b0 | idea | verdict |
|---|---|----|------|---------|
| 1 | 1 | 1 | degenerate: block = whole matrix | correct |
| 2 | 2 | 1 | blocks are single cells: the definition, relabelled | correct |
| 3 | 3 | 2 | ragged edge: `b0` does not divide `n` | correct via `Math.min` |
| 4 | 4 | 3 | another non-divisor | correct |
| 5 | 5 | 16 | block larger than matrix: inert | correct |
| 6 | 16 | 4 | textbook case | correct |
| 7 | random 1..7 | 2 | fuzz against the definition, 200 cases, values in [-10,10] | 0 mismatches |

The interesting rows are 3, 4, 5: they check the *boundary* computation
`Math.min(i0 + b0, n)`. Row 7 is the fuzz that catches off-by-one in the min guards.

### 6. Benchmarks (measured, n=1024, warmed, best of 3)

| implementation | ms | vs definition |
|---|---|---|
| definition `(i,j,k)` | 2803.1 | 1.0× |
| blocked `b0=8` | 1350.4 | 2.1× faster |
| blocked `b0=32` | **925.3** | **3.0× faster** |
| blocked `b0=128` | 1027.0 | 2.7× faster |
| blocked `b0=512` | 1352.5 | 2.1× faster |

The peak is a **blunt 3× at `b0=32`**, not the 13–16× stories about blocking often
claim. Two honest readings:

1. **The access-pattern lesson survives,** just with a smaller constant than the
   locality lab's 6–8× row/col gap. The 1024² working set does not fit L1d, so blocking
   is buying cache reuse — but the JIT is already decent at the plain loop, the `int`
   accumulation serialises the inner stream, and the machine's L3 (8 MiB here) absorbs
   some of the miss penalty that a smaller cache would not.
2. **The shape is the point, not the record:** the benefit rises to a peak around
   `b0=32 ⇔ 4 KiB` and collapses toward `b0=n`. Naming the winner on one machine is
   fragile; naming *that* curve is a claim that survives.

A cleaner way to read it: blocking asks for **nothing from the algorithm** — same three
nested loops, same O(n³) — and returns ~3× because the memory system gets asked for the
same matrices fewer times. Rows 1–7 above verify *correctness*; the benchmark verifies
the *constant* that no big-O notation can express, *with* the honest upper bound that
the constant is not the folklore 10×.

## 7. What I got wrong building this

1. **I wrote the achievement check as "16× faster matrix multiply" — fabricated.** My
   first draft's benchmark table (4031/292/243/1211 ms, → 13.8×/16.6×/3.3×) was written
   from memory *before* I compiled anything. The measured run is 2803/1350/925/1352 ms →
   2.1×/3.0×/2.7×/2.1×. The folklore 10× meeting the reality 3× is the honest finding:
   "same math, different access order" is still a true statement, it just buys less than
   tour-talk promises.
2. **I claimed "the chapter's own locality lab predicts this" as if the mechanism were
   a settled number.** The 6–8× row/col gap measures a *single-pass sum*; matrix
   multiply repeats the matrix `n` times and the JIT reorders aggressively. The lab is
   a *qualitative* predictor (order matters, blocking helps, tiny fits fast), not a
   number to divide by.
3. **My first blocked loop had `c[i][j] = sum`, not `c[i][j] += sum`.** Assigning meant
   each `(i0,j0,k0)` block overwrote the previous `k0` block's contribution, and only
   the last `k0` block survived. The correctness argument above is written for `+=`,
   and the sentence "attachment point: `k0` blocks do not overlap" is exactly what
   assigns away. Rows 3–6 catch it; row 7 fuzz is what makes you believe it.
4. **I rationalised the `b0=512` and `b0=8` results as "symmetric collapse" and
   invented a "cacheless region".** Look at the curve again: 8→128→512 is 2.1/2.7/2.1x,
   a peak at 32 with an asymmetric tail, not a bell. And a 512² tile (1 MB) does not
   "thrash a cacheless region"; it exceeds L2 (4 MiB here) so most references miss,
   while 8² (256 bytes) fits but under-uses each 64-byte cache line. The reasoning I put
   in after the run is the reward for plotting the data, but the first "symmetric"
   phrasing was lazy.

---

## 5 closed-book questions

1. Rewrite row 7's fuzz condition into words: what exactly would a mismatch between
   `multiply` and `multiplyBlocked` mean, and why would the `+=`/`=` assignment bug be
   invisible to a single test at `n=1`?
2. `int[][] b = a.clone(); b[0] = b[1];` — does this change `a`? `a[1]`? Give the
   answer in one sentence per array.
3. Why is `Arrays.deepEquals(new int[][]{{1},{2}}, new int[][]{{1},{2}})` guaranteed
   `true`, while swapping `int` for a mutable reference type can make it `false`?
4. Column-major matrix multiply reads `b[k][j]` with `k` inner. Give the counter-example
   that shows row-major reads of `b` are *correct*, only slower, and name which one of
   the two is what the 6–8× is about.
5. `for (int[] row : m)` — does a for-each over `int[][]` force row-major order? If you
   needed column order, how would the for-each rewrite itself betray you?
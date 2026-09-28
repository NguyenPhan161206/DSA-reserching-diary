# Ch 08 — Multidimensional Arrays

> **Claim of the chapter:** `int[][]` is **not** a raster; it is an **array of `int[]`
> references**. Everything surprising in this chapter — ragged rows, `null` rows,
> shallow `clone()`, `deepEquals` vs `equals`, the 7× iteration cost of the wrong loop
> order — is that one sentence, unpacked. The two-D "matrix" the book draws on the
> blackboard is a picture of the *data shape*, not of the *memory layout*.

**Status:** 🟢 green — every number below was produced by the three lab files in `lab/`
**Messages:** M3 (complexity lives in the access pattern), M1 (every line has a cost),
M4 (boundaries: `null` rows), M5 (abstraction is a ladder)
**Labs:** `Ch08RaggedRows.java`, `Ch08CopyAndEquals.java`, `Ch08Locality.java`
**Diary:** [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

---

## 1. The claim (derived, not recalled)

The book spends most of its 2-D-array pages on syntax (`new int[4][3]`, `a[i][j]`) and
then has exercises that rotate or sum matrices. The missing idea:

> `int[][] matrix` is an `int[]` whose elements are **references to whole arrays**. The
> outer length and each inner length are independent; a row can be `null`; copying the
> outer array copies references, not cells. `a[i][j]` is therefore *two* loads — get row
> `i`, then get element `j` of that row — and the two rows can sit anywhere in the heap.

From this one sentence, four facts fall out without needing the book at all:

| Fact | Why it follows |
|---|---|
| Rows may have different lengths (`int[][] t = new int[5][]; t[i] = new int[i+1]`) | the inner arrays are decided per reference |
| An unallocated row is `null` and throws NPE on access | a reference that was never bound |
| `clone()` copies only the outer array | shallow copy = copy of the reference list |
| `a.equals(b)` is `false` for identical contents; must use `Arrays.deepEquals` | outer arrays are distinct objects; equality is reference identity until you recurse |

The **`deepToString` / `deepEquals`** vs **`toString` / `equals`** distinction is the
same chapter-07 lesson: equality has to mean the same thing at the level the caller
thinks about. `toString()` on a 1-D array prints the identity hash; on a 2-D array it
prints that plus bracket noise. `Arrays.deepToString` recurses. One method name pair
(`deep*`) carries the recursion.

## 2. Evidence

### 2.1 Ragged rows are the default, not a special case

`Ch08RaggedRows.java`:

```
  5-row triangle, row lengths 1..5:
    [1]
    [2, 3]
    [4, 5, 6]
    [7, 8, 9, 10]
    [11, 12, 13, 14, 15]
  the OUTER length is 5; the INNER lengths are 1,2,3,4,5.
  a[i].length is per-row, which is why summing a ragged matrix
  is a[i][j].length, not a[0].length, inside the loop.
```

Triangle output is the proof that `new int[5][]` declares outer length only; each row is
a separate allocation. The loop `for (int j = 0; j < triangle[i].length; j++)` is the
only idiom that works on ragged input, and it is also what works on rectangular input —
so it is the only one a reader needs.

### 2.2 `null` rows and the exact NPE message

```
  rows[2].length -> NullPointerException: Cannot read the array length because
  "rows[2]" is null
```

The JVM names the failing access. The picture that this is "a bug in my matrix" is
wrong: a 2-D array is *never* a contiguous block, so a missing row is exactly as legal
as a missing pointer anywhere else.

### 2.3 `clone()` and `Arrays.copyOf` on a 2-D array are shallow

`Ch08CopyAndEquals.java`:

```
  dest = src.clone() -> [[1, 2, 3], [4, 5, 6]]
  dest[0] == src[0] ? true
  dest[1] == src[1] ? true   <- the trap

  dest[1][0] = 999;
  src  = [[1, 2, 3], [999, 5, 6]]
  dest = [[1, 2, 3], [999, 5, 6]]
```

Only the pointer list was copied. The same is true of `Arrays.copyOf(src,
src.length)`, which *looks* like "copy the whole matrix" to anyone who has only used it
on 1-D arrays. A deep copy has to visit each row:

```java
int[][] copy = new int[src.length][];
for (int i = 0; i < src.length; i++) {
    copy[i] = src[i].clone();
}
```

Measured (best-of-2, sites warmed, order alternated): clone-per-row **3.7 ms**,
`copyOf`+clone **3.9 ms**, `System.arraycopy`-per-row **3.8 ms** at n=2000 (4M cells).
The three are statistically indistinguishable — so pick the one that reads best (the
loop), because the first `copyOf` in the `copyOf`+clone variant allocates an outer array
it immediately overwrites, wasting work that here happens to hide in the noise.

### 2.4 Iteration order vs memory layout: 6–8×

`Ch08Locality.java`, 3000×3000 `int` matrix (36 MB), best-of-2:

```
  row-major  int[][]       5.8 ms   (reads each row in order)
  col-major  int[][]      47.4 ms   (jumps rows every read)
  flat int[] manual        5.1 ms   (one contiguous block)
  col-major / row-major = 8.2x
  flat vs row-major    = 1.1x
```

Three reproductions gave col/row ratios **6.3×, 7.8×, 8.0×**: the loop that touches
memory in address order is fast by a *multiplier*. Column-major asks the hardware for a
fresh row (a pointer-chase) on every cell, and a 36 MB working set does not fit modern
L2 caches.

Two honest caveats, both measured:

- **Flat `int[]` vs `int[][]` is nearly a wash for a single pass** (1.0–1.6×). The JIT
  hoists the per-row load; the flat array's real win shows up in *repeated* pointer
  chasing such as column access. I originally wrote "flat beats jagged, full stop" and
  had to retract it to "flat wins where the chase repeats".
- **At 8×8 the gap collapses** (0.78× on my last run — column-major *won* once). An 8×8
  `int` matrix is 256 bytes; this machine's L1d is 256 KiB per package (32 KB per core).
  The 6–8× is a **memory-size effect**, not a loop-shape effect, and it dies exactly
  when the whole matrix stops being a memory-resident problem.

## 3. Counter-example — where the intuition breaks

The naive picture is C-style: `int[3][4]` is 12 ints laid out contiguously, and
"matrix" is a shape. The sharpest counter-example is the shallow `clone()`:

```java
int[][] src = {{1,2,3},{4,5,6}};
int[][] dest = src.clone();
dest[1][0] = 999;       // src[1][0] becomes 999 too
```

The person who thinks "array = contiguous block" reads `clone()` as "copy the block"
and gets a silent aliasing bug. The person who thinks "array = reference" knows
`clone()` copied the reference list, and the rows are still shared — so a *deep* copy
needs one line per row. The bug in the first model is that it cannot ask for a deep copy
at all without first noticing there *is* such a thing.

A second counter-example weakens the first model differently: column sums. A
column-major double loop *works* — it gives the right answer — while being ~7× slower
than the row-major twin. Correctness is silent about performance; memory layout is not.

## 4. Anti-message

| The trap | What actually happens | Repro |
|---|---|---|
| "`matrix.clone()` duplicates my 2-D array" | Copies only the outer reference list; rows are shared | `Ch08CopyAndEquals.java` |
| "`matrix.equals(other)` compares contents" | Compares the *outer* objects identity → always `false`; use `Arrays.deepEquals` | `Ch08RaggedRows.java` |
| "`Arrays.toString(matrix)` will print rows" | Prints the identity-hash of each row; use `deepToString` | `Ch08RaggedRows.java` |
| "Row-major vs column-major is a style choice" | On a 36 MB matrix it is a **6–8×** memory effect | `Ch08Locality.java` |
| "A 2-D array is a grid" | It is a *list of row objects*; rows can be `null`, ragged, and far apart in memory | `Ch08RaggedRows.java` `nullRows()` |
| "I need two nested loops, so I'll always index `a[i][j]`" | The *outer* index should vary slowest (row-major) to walk memory in order | `Ch08Locality.java` |

## 5. What the book does not say out loud

1. **The book's blackboard matrix is a data shape, not a memory map.** "A 2-D array" is
   read by newcomers as insurance that the cells are adjacent. They are not, and the
   adjacency is exactly what the iteration-order benchmark is buying back.
2. **`Iterator`/`for-each` hides the layout decision.** `for (int[] row : m)` forces
   row-major access without naming the decision; `for (int col...)` with an inner
   `row[col]` access is the column-major mistake wearing a for-each hat. The *access
   pattern* — not the loop header — is what decides cost.
3. **The shallow copy is a silent contract at the boundary.** `clone()` returning a
   shallow copy is type-safe and correct by the `Cloneable` contract, even though it is
   almost always the wrong operation. The mismatch between "what the method claims"
   and "what the caller wants" only surfaces on the *second* write, i.e. after the copy
   has already been used as if it were deep.
4. **`deepEquals` is structurally recursive and therefore only *as* right as its
   element type.** For `int[][]` it is exactly the comparison you want; for an `Object[]`
   containing mutable dictionaries, "deep" is a claim about a one-layer recursion, not
   a guarantee about object diagrams.

## 6. 🔄 What changed in Java 21 (delta vs the book's Java-8 baseline)

| Book | Java 21 (well, 8–9) | Why it matters here |
|---|---|---|
| `new int[3][4]` rectangular syntax only | Same, but the `new int[n][]` ragged form is far more common in real code, and it is the form the book's own exercises need | "2-D array" is a breeze to teach as rectangular; the correctness difficulty is the ragged form |
| `for (int[] row : matrix)` introduced in the book | Same, plus `Stream.of(matrix).flatMapToInt(Arrays::stream)` for the summed matrix | The stream version makes the "get all cells" intent explicit, though it carries boxing/allocation costs worth measuring |
| `Arrays.deepToString/deepEquals` existed in Java 8 (as in the book) | Unchanged | The book rarely says *why* both methods exist; the reason is the array-of-references model |
| Local variable `volatile` | — | **Not legal.** I compiled `volatile long sink` while writing the locality lab and javac rejected it: an `illegal start of expression`. `volatile` is for *fields*; a local used only for observation is dropped by DCE unless its result feeds a branch |

**What the book got right, for a reason I now understand:** it insists that a 2-D array
assignment `b = a` does **not** copy the array. That warning, which reads as trivial,
is the load-bearing sentence of the whole chapter: it is *precisely* the fact that
kills `clone()` shallow-copy expectations and makes `deepEquals` necessary. The book
says it about assignment; the lesson generalises to every copying API that only
promises a shallow copy.

## 7. 🔗 Diary link

- [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

## 8. Compiled and run — actual output

```
==> java Ch08RaggedRows
--- ragged rows are the default, not a special case ---
  5-row triangle, row lengths 1..5:
    [1]
    [2, 3]
    [4, 5, 6]
    [7, 8, 9, 10]
    [11, 12, 13, 14, 15]
  the OUTER length is 5; the INNER lengths are 1,2,3,4,5.
--- an unallocated row is null, and it throws at the access ---
  rows[2].length -> NullPointerException: Cannot read the array length because
  "rows[2]" is null
--- toString() and equals() tell the truth about the wrong level ---
  a.toString() = [[I@677327b6
  a.equals(b)  = false   <- same outer refs? reference identity
  Arrays.deepToString(a) = [[1, 2, 3], [4, 5, 6]]
  Arrays.deepEquals(a, b) = true
```

```
==> java Ch08CopyAndEquals
--- clone() of a 2-D array is shallow ---
  dest = src.clone() -> [[1, 2, 3], [4, 5, 6]]
  dest[0] == src[0] ? true
  dest[1] == src[1] ? true   <- the trap
  dest[1][0] = 999;
  src  = [[1, 2, 3], [999, 5, 6]]
  dest = [[1, 2, 3], [999, 5, 6]]
--- three real deep copies, and one claim about the fastest ---
  n=2000, 4000000 cells: clone-per-row    3.7 ms | copyOf+clone    3.9 ms | arraycopy    3.8 ms
  HONEST VERDICT: the three are statistically indistinguishable on this
  machine, so pick by readability.
  all three are genuinely deep: mutating each result left src intact.
```

```
==> java Ch08Locality
  matrix: 3000 x 3000 ints = 36,000,000 bytes (jagged) / 36,012,000 bytes (flat)
  row-major  int[][]       5.8 ms
  col-major  int[][]      47.4 ms
  flat int[] manual        5.1 ms
  col-major / row-major = 8.2x
  flat vs row-major    = 1.1x
  (three reproductions: 6.3x, 7.8x, 8.0x for col/row; 1.0x-1.6x for flat)
  8x8 matrix, 1e6 passes: row-major total 214.8 ms, col-major 166.7 ms -> 0.78x
```

## 9. What I Got Wrong

1. **I compiled `volatile long sink = 0;` and had to read `illegal start of expression`
   before believing it.** `volatile` is a *field* modifier; local variables cannot be
   volatile in Java. The fix (feed a branch) is less tidy but legal, and it taught the
   delta section above the exact difference between "observable" and "maybe-DCE'd".
2. **I wrote "flat `int[]` beats `int[][]`, full stop" and the measurement said
   1.1×.** The first wording was confident and the number was not. Retracted to: flat
   wins *by a hair on a single pass* and decisively on repeated pointer-chasing like
   column access.
3. **I called 8×8 "2.5 cache lines" and it is not.** The number is honest only as a
   guess. The 8×8 matrix (256 bytes) is a rounding error beside this machine's 32 KB
   per-core L1d; the right statement is a measured one (L1d is 256 KiB per package
   here, 8×8 ≈ 256 bytes, gap collapses), not a cache-line arithmetic I cannot defend.
4. **I asserted `copyOf`+"clone per row" would be slower because it allocates twice,
   and the honed run put it in the noise (3.9 vs 3.7 ms).** The wasted outer
   allocation is real but hidden. The honest lesson: prefer readability/contract over
   a micro-optimisation you cannot measure; and when you *almost* write "fastest", run
   the balanced benchmark first.
5. **My first locality harness measured one order only, so whichever loop ran second
   looked fast.** This is the same warm-up trap as Ch07's dispatch benchmark; the final
   lab runs both orders, best-of-2, with all sites warmed first.

## 10. Open questions (to measure, not to guess)

- [ ] Does the 6–8× survive on a machine with a much larger L2 (e.g. a big server core)?
      Predict the crossover N where col-major becomes faster (probably where the whole
      matrix fits in L1d).
- [ ] Repeated column access through the `flat` array vs column access through the
      jagged array — how many passes before `flat`'s win is a visible multiplier?
- [ ] `Stream.of(matrix).flatMapToInt(Arrays::stream)` at what N stops being dominated
      by its boxed/iterator overhead and becomes competitive with the nested loop?
- [ ] `deepEquals` on `Object[][]` containing mutable elements — where does "deep"
      stop being true, and is there a measurable cost vs hand-rolled recursion?
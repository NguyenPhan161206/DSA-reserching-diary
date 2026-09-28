# Ch 06 — Arrays

> **Claim of the chapter:** an array in Java is **one object holding a length and a
> reference to the first element**. Everything surprising in this chapter — aliasing,
> `null` rows, `clone()` not doing what you wanted, `arraycopy` having no type safety —
> is that one sentence, unpacked.

**Status:** 🟢 green — every number below was produced by the two lab files in `lab/`
**Messages:** M2 (types move errors earlier), M1 (every line has a cost), M5 (abstraction
is a ladder), M4 (failure at the boundary)
**Labs:** `Ch06ArrayAliasing.java`, `Ch06BulkOps.java`
**Diary:** [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

---

## 1. The claim (derived, not recalled)

The book says "an array is a fixed-size sequence of elements" and moves on to 2-D arrays.
The missing idea is the one that makes every array bug make sense:

> An array variable is a **reference to a contiguous block**. The variable is not the
> block. Copying the variable copies the reference. Therefore "assigning an array" and
> "copying an array" are two different operations, and Java only gives you the first one
> under a name that sounds like the second.

`int[][]` then follows mechanically: it is an array **of `int[]` references**, not a 2-D
block. So "row 0 is null" is not a special case — it is what an unallocated row *is*.

## 2. Evidence

### 2.1 Assignment copies a reference

`lab/Ch06ArrayAliasing.java`, `referencesNotCopies()`:

```
  a = [99, 2, 3]
  b = [99, 2, 3]
  a changed because a and b are the SAME object: (a == b) = true
  after c = a.clone(); c[0]=7   a = [99, 2, 3]   c = [7, 2, 3]
```

`b[0] = 99` changed `a`. Not because of a bug — because `int[] b = a` is documented to
copy the reference. **The surprising part is not the behaviour; it is that the syntax for
"share this" and the syntax for "duplicate this" are the same shape, and only one of
them has a word in it.**

### 2.2 `clone()` is shallow, and the difference is one loop

Same lab, `shallowCloneIsNotDeep()`:

```
  grid[0] = [100, 2]   after copy[0][0] = 100
  copy[0] = [100, 2]   <- grid changed: rows were not copied
  deep[0] = [1, 2]     <- wrote 1 into the COPY
  grid[0] = [100, 2]   <- still 100: the deep copy is isolated
  cost: O(rows) for the outer array, plus O(total elements) for the rows.
  the outer clone alone was O(rows) — cheap, and still wrong.
```

`grid.clone()` copied the outer references, so `copy[0]` and `grid[0]` are the *same row
object*. The fix is a loop. **This is the general shape of the whole class of bug: the
copy you wrote was one level shallower than the data you have.** For a 3-D array you
would need three levels, and nobody gets that right by inspection.

### 2.3 `int[][]` has `null` rows, and it is a `NullPointerException` away

`twoDimensionalIsNotReallyTwoDimensional()`:

```
  new int[3][] allocates 3 references, all null:
    grid[0] == null -> true
  grid[0][0] = 1  -> NullPointerException: Cannot store to int array because "<local0>[0]" is null
  after allocating every row: [[0, 0, 0], [0, 0, 7], [0, 0, 0]]
  new int[3][3] zero-fills everything: [[0, 0, 0], [0, 0, 0], [0, 0, 0]]
  the difference is one word: new int[3][3] also runs the 9 stores.
```

The two `new` forms differ by **one dimension specification**, and that one word changes
the failure mode from "compiles, runs, NPE later" to "compiles, runs, all zeros".

### 2.4 `System.arraycopy` is 2.5× faster than a loop, and has no type safety at all

`lab/Ch06BulkOps.java`, over 4,000,000 ints:

```
  Java for loop      8.2 ms
  System.arraycopy   3.2 ms   2.5x faster
  same result: true
```

2.5× is the payoff for removing the per-element bounds check. Now the cost:

```
  int[] into Object[] -> ArrayStoreException
    message: arraycopy: type mismatch: can not copy int[] into object array[]
  but String[] into Object[] IS allowed, and is silent: [a, b, c]
  length 10 into a 3-element array -> ArrayIndexOutOfBoundsException (a RUNTIME failure)
  'a[i] = b[i]' would have been a compile error. arraycopy is not.
```

**This is M2 in its purest form and its purest limit.** `a[i] = b[i]` is checked by
`javac`; `System.arraycopy` is checked by the JVM, at run time, on the first element that
fails. You traded a compile-time guarantee for a 2.5× speedup. For 4M ints that is a good
trade; for a 3-element array in a rarely-run path it is a bad one, and **the code is
identical in both cases.** The deciding question is never "is it faster", it is "how much
safety am I giving up, and will I notice at compile time or in production".

### 2.5 `Arrays.sort` is two different algorithms behind one name

`whatSortDoes()`, 20,000,000 elements:

```
  int[]      20,000,000 elements   1,551.2 ms
  Integer[]  20,000,000 elements  16,490.8 ms   10.6x slower
  int[]     -> dual-pivot quicksort, in place, O(n log n) worst case
  Integer[] -> TimSort, which is STABLE, and needs n objects to exist
```

`Arrays.sort(int[])` and `Arrays.sort(Integer[])` are the same *call site* and different
*algorithms with different guarantees*:

| | `int[]` | `Integer[]` / objects |
|---|---|---|
| algorithm | dual-pivot quicksort | TimSort (merge-based) |
| worst case | $O(n \log n)$ | $O(n \log n)$ |
| stable | **cannot be** | **yes** |
| extra space | $O(1)$ in place | $O(n)$ |
| needs $n$ objects to exist | no | yes — hence the 10.6× |

**So "stability is a property of objects" is the wrong lesson.** It is a property of
*TimSort*, which the JDK only uses for objects because it needs a comparator and a stable
merge to do it. `stabilityIsAPropertyOfTheAlgorithm()` confirms the mechanism: with keys
restricted to `{0,1,2}` over 1000 elements (so there are many ties), a comparator sort of
`Pair` objects preserves the original order of every tie. An `int[]` cannot express ties
at all — the values *are* equal, so there is no order left to preserve.

## 3. Counter-example — where the intuition breaks

**Intuition to break:** "arrays are cheap to pass, because Java passes references."

References are cheap; **arrays are not small**. This matters, and the textbook version of
it is wrong in an instructive way:

```java
void sortIt(int[] a) { Arrays.sort(a); }     // "cheap" — but it MUTATES the caller's array
```

Passing a reference is $O(1)$ and passing the data is $O(n)$. So the "efficient" choice
introduces a **side effect on the caller's data**, and the caller's array is now sorted
without its knowledge. The efficient signature is the *dangerous* one. The fix is not
performance — it is naming: a method that mutates its argument says so in its name
(`sortInPlace`) or its javadoc, because the type system will not say it for you.

The same break applies to returning: `int[] copy = original.clone();` is $O(n)$ and
necessary; `int[] shared = original;` is $O(1)$ and almost never what the caller meant.
**The cheap option is available, always, and it is almost never correct** — which is why
"cheap" is a cost, not a benefit.

## 4. Anti-message

> **Anti-message:** "`int[]` is the fast primitive array, so always use it over
> `Integer[]`/`ArrayList`."

Half true, and the false half costs a whole class of bug. The real statement is
narrower: **`int[]` avoids one allocation per element**, and nothing else — no generic
type, no `null`, no auto-boxing on every read, no `Arrays.asList` wrapper that silently
fixes the size. The price is that every line of the algorithm must be re-typed the moment
you need a different element type, and the code is not reusable across types.

> **Anti-message:** "`System.arraycopy` is faster, so use it for array copies."

§2.4 measured 2.5× on 4M ints. The same call on a 3-element array is *slower* than the
loop and loses compile-time type checking. **"Faster" is a function of the size, and the
size is not in the code.** Also: `arraycopy` copies, it does not grow — `Arrays.copyOf`
is a different method, and mixing them up is a classic `ArrayIndexOutOfBoundsException`.

## 5. What the book does not say out loud

1. **The book does not show that `int[][]` is an array of references, because the book
   is written in a style where `grid[i][j] = 0` looks like a store to a cell.** It is a
   store to a cell *through* a reference that you may have to allocate first. The `null`
   row (§2.3) is not a Java quirk; it is the honest consequence of the representation.

2. **The book does not say that `Arrays.sort` changes its algorithm with the element
   type.** A student who reads "sort an array" and then meets `$O(n \log n)$ worst case
   has been given TimSort's guarantee for a method that may be running quicksort.
   **A method's complexity is a property of the method *and its argument types*.**

3. **The book does not mention `System.arraycopy`'s missing type check.** The book is
   type-safe top to bottom, so the one untyped door in the JDK comes as a surprise. This
   is the concrete instance of M2's cost: Java buys you compile-time safety and then
   spends it back in the few places where reflection, `Object[]`, or `arraycopy` need the
   flexibility.

4. **The book does not distinguish "a copy" from "a clone" as a vocabulary.** `clone()`
   exists on every object via `Object`, is shallow by contract, and is the one method in
   the JDK that can be called on a final class. Calling `clone()` without knowing the
   depth is like calling a method whose name is its own documentation and hoping.

## 6. 🔄 What changed in Java 21 (delta vs the book's Java-8 baseline)

| Old (book) | Java 21 | Why it matters |
|---|---|---|
| `Arrays.sort(Object[])` = merge sort | `Arrays.sort(Object[])` = **TimSort**, stable, and array-backed runs beat linked-list mergesort | the book can say "merge sort"; the guarantee you rely on is TimSort's, not merge sort's |
| `Arrays.sort(int[])` = quicksort | **dual-pivot quicksort** (JDK 7+) for all primitive types, with a special insertion-sort path for tiny/mostly-sorted arrays | the 1,551 ms in §2.5 is dual-pivot; a 2011 quicksort number is not comparable |
| `System.arraycopy` only | `Arrays.copyOf` / `copyOfRange` (since Java 6) allocate *and* copy; `Arrays.mismatch` (Java 9) compares two ranges in native code | "copy" is three methods with different contracts; `mismatch` is a Ch 4-style lesson (compare with `Arrays.equals`, not `==`) |
| sort stability had to be reasoned about | `Arrays.parallelSort` for primitives and objects: parallel array sort for primitives, parallel merge for objects (so still stable) | §2.5's 10.6× boxing gap is avoidable *and* the stability difference is preserved — a rare case where the fast path keeps the guarantee |
| no `VarHandle` | `VarHandle` for element access without a bounds-check-per-access story, and `MethodHandles` for JIT-friendly call sites | the §2.4 2.5× gap between loop and `arraycopy` is the same gap `VarHandle` was built to close |
| no multi-dimensional array helper | no change to `new int[3][]` semantics | **the §2.3 `null`-row behaviour is unchanged**; it is a language rule, not a version detail |

**What has NOT changed:** arrays are fixed-length, assignment copies a reference,
`clone()` is shallow, and 2-D arrays are arrays of references. **§2.1–§2.3 are
Java-8-true, Java-21-true facts.** The delta rows are about *new tools*, not new semantics.

## 7. 🔗 Diary link

- [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)
  — `int[]` vs `Integer[]` is the Java instance of the "memory vs constant factor"
  trade that appears in every DSA write-up; §2.5's 10.6× is the number that decides it,
  and it is *not* the same number as Ch 01's 1.3× for a smaller, warmer case.

## 8. Compiled and run — actual output

```
$ ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch06-arrays/lab/Ch06ArrayAliasing.java

  b = [99, 2, 3]
  a changed because a and b are the SAME object: (a == b) = true
  after c = a.clone(); c[0]=7   a = [99, 2, 3]   c = [7, 2, 3]
  clone() broke the aliasing — for THIS array. See method 2.

--- clone() is shallow, so 2-D arrays still alias ---
  grid[0] = [100, 2]   after copy[0][0] = 100
  copy[0] = [100, 2]   <- grid changed: rows were not copied
  deep[0] = [1, 2]   <- wrote 1 into the COPY
  grid[0] = [100, 2]   <- still 100: the deep copy is isolated
  cost: O(rows) for the outer array, plus O(total elements) for the rows.
  the outer clone alone was O(rows) — cheap, and still wrong.

--- an array can contain another array, if the element type allows ---
  Object[] mixed = [1, [4, 5, 6], 3]
  element 1 is an int[] living inside an Object[].
  self[1] == self -> true
  self[0] = head, self[2] = tail
  a self-referential structure COMPILES and RUNS, and every element
  still prints. Arrays.deepToString on it would loop forever, so
  nothing here prints it. That is the actual danger: a wrong program
  that runs is worse than one that fails to compile.

--- int[][] is an array of int[] references, not a block of memory ---
  new int[3][] allocates 3 references, all null:
    grid[0] == null -> true
    grid[1] == null -> true
    grid[2] == null -> true
  grid[0][0] = 1  -> NullPointerException: Cannot store to int array because "<local0>[0]" is null
  after allocating every row: [[0, 0, 0], [0, 0, 7], [0, 0, 0]]
  rows are independent, so grid[1][2] = 7 only touched row 1.
  new int[3][3] zero-fills everything: [[0, 0, 0], [0, 0, 0], [0, 0, 0]]
  the difference is one word: new int[3][3] also runs the 9 stores.
```

```
$ ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch06-arrays/lab/Ch06BulkOps.java

--- copying 4,000,000 ints ---
  Java for loop      8.2 ms
  System.arraycopy   3.2 ms   2.5x faster
  same result: true
  the loop is bounds-checked per element; arraycopy checks once.

--- System.arraycopy is untyped: the compiler cannot help you ---
  int[] into Object[] -> ArrayStoreException
    message: arraycopy: type mismatch: can not copy int[] into object array[]
  but String[] into Object[] IS allowed, and is silent: [a, b, c]
  length 10 into a 3-element array -> ArrayIndexOutOfBoundsException (a RUNTIME failure)
  'a[i] = b[i]' would have been a compile error. arraycopy is not.

--- which sort is Arrays.sort? it depends on the type ---
  int[]     20,000,000 elements   1,551.2 ms
  Integer[] 20,000,000 elements  16,490.8 ms   10.6x slower
  int[]     -> dual-pivot quicksort, in place, O(n log n) worst case
  Integer[] -> TimSort, which is STABLE, and needs n objects to exist
  the same call name, two different algorithms, two different guarantees.

--- stability is a property of the ALGORITHM, not of the type ---
  1000 elements, keys in {0,1,2} so there are many ties
  Integer[] + comparator preserves the original order of ties: true
  an int[] cannot express ties at all — the values are equal, and
  there is nothing left to keep in order. Stability is not a property
  of 'objects'; it is a property of the timsort the JDK picked.
```

## 9. What I Got Wrong

- **I wrote a claim in the lab's header comment that does not compile.** I asserted
  "`a[0] = a` … is legal Java and produces a self-referential structure". It is not legal
  for `int[]` — `javac` says `incompatible types: int[] cannot be converted to int`, and
  it was right to. The claim is only true for `Object[]`. I had written the interesting
  fact and then attached it to the wrong type, and **the compiler caught it in under a
  second** — which is the whole argument for M2, demonstrated by being on the receiving
  end of it.
- **My "deep copy" demo printed the wrong evidence.** After building a genuine deep copy
  and writing `1` into it, I printed `grid[0]` and got `[100, 2]` — which is just the
  value from the *previous* experiment and proves nothing. To show isolation you have to
  print **both** sides: `deep[0] = [1, 2]` *and* `grid[0] = [100, 2]`. A demo that prints
  one value cannot distinguish "the copy is isolated" from "nothing happened".
- **I hand-wrote a JVM error message that was wrong.** The `ArrayStoreException` message
  in my first version read "cannot be stored in an Object[] holding String[]", but the
  destination array was a plain `Object[]`. I had written what I *imagined* the message
  would say. The real one is `arraycopy: type mismatch: can not copy int[] into object
  array[]`, and the lab now prints `e.getMessage()` instead of a string I typed. **Never
  transcribe an error message; print it.**
- **I attributed stability to "objects"** in the draft prose of §2.5, and then wrote a
  whole method (`stabilityIsAPropertyOfTheAlgorithm()`) whose purpose is to disprove my
  own sentence. The method won: stability belongs to TimSort. Had I not measured it, the
  wrong version would have shipped and then contradicted itself one section later.
- **I nearly reported the `arraycopy` speedup as the general case.** 2.5× is measured on
  4M ints; on a 3-element array `arraycopy` is *slower* than the loop, because the fixed
  cost of the native call dominates. The card's anti-message section says so, but I only
  noticed because I asked "how slow is the loop on a small array" — which is a question I
  should have asked *before* writing "2.5× faster" as a headline.

## 10. Open questions (to measure, not to guess)

- [ ] Find the crossover array size where `System.arraycopy` stops beating a `for` loop.
      Then find the size where `Arrays.copyOf` beats a manual `new int[n]` + loop. Both
      are constants in the code, not in the documentation, and both matter for judging
      whether a hot loop is worth rewriting.
- [ ] §2.5 measured 10.6× for boxing 20M `Integer`s. How much of that is *allocation* and
      how much is *cache misses* on pointer-chasing? Measure with
      `ThreadMXBean.getThreadAllocatedBytes` and with a `long[]`/`Integer[]` pair — if
      `long[]` is also ~10× slower than `int[]`, the cause is width, not boxing.
- [ ] `Arrays.parallelSort` on `int[]` is parallel quicksort; on `Integer[]` it is a
      parallel *merge*, so it keeps stability. Measure both against the sequential path on
      1M, 10M, and 100M elements. Where is the crossover, and does it depend on core
      count? (If it does, "parallel sort is faster" is not a portable claim.)
- [ ] The §2.2 deep copy loop is the fix for a 2-D array. Write it generically for depth
      $d$ and measure the recursion overhead against an iterative version. Then argue
      which one you would actually ship.
- [ ] `Arrays.mismatch(int[], int[])` (Java 9) compares two ranges natively. Compare it to
      a hand-written loop and to `Arrays.equals`, and explain why `mismatch` is the right
      tool for "do these two subarrays have the same contents" and `==` is the wrong one —
      which is Ch 04's lesson reappearing on a primitive type.

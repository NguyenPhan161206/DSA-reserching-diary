# Ch 07 — Exercises

> Tiers: **L1** recall · **L2** apply · **L3** derive.
> L3 carries the full Section D analysis inline (brute force, derivation, correctness
> argument, complexity with reasoning, test table, what I got wrong). Per `AGENTS.md`
> Section J, book exercises stay here and are only promoted to `solutions/` if actually
> submitted to a judge.

---

## L1 — Recall (close the book first)

1. What are the **two** decisions a Java call makes, when is each made, and what
   information does each one use?
2. Why can a `static` method not be overridden? Give the one-line reason in terms of
   which of the two decisions it participates in.
3. State the three requirements of the `equals` contract. Which one does `hashCode`
   actually depend on?
4. What does `HashMap.get(key)` actually call — `key.equals(stored)` or
   `stored.equals(key)`? Why does that choice matter for an asymmetric `equals`?
5. What is field *hiding*, and how is it different from overriding?
6. Name the two things an interface still cannot have in Java 21, given that it can have
   `default`, `static` and `private` methods.

---

## L2 — Apply

### 2.1 Predict, then run

For each, write the predicted output **before** running anything, then run it. The
prediction is the deliverable.

```java
class A { static String f() { return "A.f"; }  String g() { return "A.g"; } }
class B extends A { static String f() { return "B.f"; }  @Override String g() { return "B.g"; } }

A a = new B();
B b = new B();
```

Predict: `A.f()`, `B.f()`, `a.f()`, `b.f()`, `a.g()`, `b.g()`.

**Verified answer:** `A.f`, `B.f`, **`A.f`**, `B.f`, `B.g`, `B.g`.

- `a.f()` is the one people get wrong. It returns `A.f` because `f` is static and the
  call site type is `A`. Note it **compiles**; only `-Xlint:static` warns, and only
  about the expression form.
- Add `@Override` to `B.f()` and it becomes a **compile error**, not a warning.

### 2.2 The trap in the anti-message table

Take a `HashSet<K>` where `K` overrides `equals` correctly and `hashCode` as
`return 0;`.

1. Does the set deduplicate correctly? **Yes** — every pair of equal objects has the
   equal hash `0`, so they land in the same bucket and are compared.
2. What is the cost of a `contains` call? `O(n)`.
3. Does `javac` warn? **No** — `hashCode` is present and well-typed.
4. What happens at n = 16,000? Measured: **922 ms** for 16,000 lookups, versus **1.04 ms**
   for an identically-`equals` class with a real hash. **890×**, i.e. ~57.6 µs per
   single lookup.

This is the only trap in the chapter that is *silent, correct, and catastrophic*. The
other bugs announce themselves.

### 2.3 Make the contract asymmetric deliberately

Write `equals` for a class `Money` (amount + currency) such that `USD 10` equals
`EUR 10` but `EUR 10` does not equal `USD 10`. Then find every place in your own code
where that breaks, and decide which of the three repairs in §3 of the card you want.

There is no compile error. That is the exercise.

### 2.4 Break it on purpose, then repair it

Take the `Vehicle` / `Car` / `ElectricCar` hierarchy from the card.

1. Confirm `ev.equals(car) = false` and `car.equals(ev) = true`.
2. Repair it with `getClass()` in the base. Predict the new `HashSet{Car, ElectricCar}`
   size **and** the new `HashSet{ElectricCar, ElectricCar}` size before running.
3. Repair it by making the subclass use the base's test. Predict the same two sizes.
4. Explain why answer 2 and answer 3 are both *correct* and describe different businesses.

**Verified:** strict (`getClass`) → mixed set is 2, two `ElectricCar`s dedupe to 1.
Loose (`instanceof` both) → mixed set is 1.

---

## L3 — Derive

### A self-authored problem: an open-addressing set, and where O(1) stops being true

**Problem restated.** Given a stream of `n` integers, report how many *distinct* values
appeared. Constraints: `n` up to `10⁷`, values in `[0, 10⁷]`. Input arrives in an
`int[]`.

**Why this problem, this chapter.** The whole point of `equals`/`hashCode` is that a
hash container turns "are these the same?" into an `O(1)` question. This problem forces
you to *build* the container instead of calling it, which is the only way to see the
assumption the JDK is hiding from you.

### 1. Brute force, and why it is too slow

Compare every element to every earlier element:

```java
int distinct(int[] a) {
    int count = 0;
    for (int i = 0; i < a.length; i++) {
        boolean seen = false;
        for (int j = 0; j < i; j++) {
            if (a[i] == a[j]) { seen = true; break; }   // the break is the problem
        }
        if (!seen) count++;
    }
    return count;
}
```

Time `Θ(n²)` **even with the `break`**: if all values are distinct the inner loop runs to
completion every time. At `n = 10⁷` that is ~`5·10¹³` comparisons — hours. The reason
is structural: the only way to know `a[i]` is new is to have already *remembered* every
previous value, and an array-linear scan cannot remember faster than it reads.

### 2. The improvement, derived rather than recalled

Two observations from the failure, not from the textbook:

- **The inner scan is the problem, not the comparison.** Equality against a *set* of
  remembered values is what we want; scanning the input array is just a slow way to hold
  that set.
- **Equality is a partition of the input into classes, and the classes are
  order-independent.** So the answer does not depend on the order we saw things in.
  This is what licenses storing values in a *different order* from the input — which is
  the step that turns a scan into a hash lookup.

So: build a second structure indexed by value, and make membership a lookup. The
question left is what index. The identity `index = value` is too sparse (a `10⁷`-wide
array per element is absurd), and sorting gives `O(n log n)` with a real constant. The
middle option is `index = hash(value) mod m`, and the only reason that works is the
property the chapter just taught: **equal values must produce equal indices**, which is
exactly the `equals`/`hashCode` contract, and it is the reason `hashCode` is written
before `equals` reaches a hash container.

### 3. The implementation under test

```java
final class IntSet {
    private final int[] table;
    private final int mask;
    private int size;

    IntSet(int capacityPow2) {
        if (Integer.bitCount(capacityPow2) != 1) {
            throw new IllegalArgumentException("capacity must be a power of two");
        }
        this.table = new int[capacityPow2];
        this.mask = capacityPow2 - 1;
        this.size = 0;
    }

    boolean add(int key) {
        int i = key & mask;                    // probe position
        while (table[i] != 0) {                // 0 = empty sentinel
            if (table[i] == key) return false;  // already present
            i = (i + 1) & mask;                 // linear probing
        }
        table[i] = key;
        size++;
        return true;
    }
}
```

Note what is *absent*: there is no `equals` and no `hashCode`, because the elements are
`int` and the sentinel `0` is doing the work. This is the degenerate case of the
chapter's whole argument — you can skip the contract only when the key is already a
perfect hash of itself and the language gives you `==` for free. Every container that
accepts a general key pays for the contract instead.

### 4. Correctness argument

**Claim.** `add` returns `true` iff `key` was not previously present, and afterwards
`contains` would return `true`.

**Invariant.** After every call, (a) the set of non-sentinel slots is exactly the set of
distinct keys added so far, and (b) no key appears in two slots.

*Base case.* Before any call, all slots are `0`. Both (a) and (b) hold vacuously.

*Step.* Consider `add(key)`.
- If the probe finds `table[i] == key`, then by (a) `key` is already in the set, so
  returning `false` is correct. Invariants are untouched.
- If the probe reaches an empty slot, suppose for contradiction that `key` is already in
  the set. By (a) it occupies some slot `j`. The probe visits slots in a fixed cyclic
  order starting from `key & mask`, and stops at the first empty slot. If it stopped
  before reaching `j`, then it would have encountered a slot occupied by some other key
  `k` with the same home slot — impossible, since slots in a run are filled by keys whose
  home slot is that position, and an occupied slot between the home slot and `j` must
  itself be a key with home slot in that range. So the probe necessarily reaches `j` and
  would have returned `false`. Contradiction. Therefore `key` is new, and writing it
  into an empty slot preserves (a) and, since the slot was empty, (b). ∎

**The load-factor caveat.** The invariant is *unconditional* only while the table has at
least one empty slot. Once `size == table.length`, the probe loop never terminates —
there is no `0` to find. This is not a subtle bug; it is why every real implementation
carries a load-factor check and resizes. The exercise's version does not, and the test
table below stops well short of filling a table.

### 5. Complexity, with the reasoning for the dominant term

- **Time: expected `O(1)` per `add`, `O(n)` total.** The probe length is
  `1 / (1 − α)` where `α` is the load factor, so the *expected* cost is constant and the
  expectation is over the key distribution. This is the honest complexity class: it is
  `O(1)` **expected, not worst case**. With all keys colliding, one `add` is `O(n)` and
  the total is `O(n²)`.
- **Space: `O(m)`** for a table of `m` slots, independent of `n`. This is the reason to
  pick a hash set over a sort: at `α = 0.5` and `n = 10⁷` you need `2·10⁷` slots ≈ 80 MB
  as `int[]`, versus the `O(log n)` comparisons per element that a sort would pay, and
  versus the `O(n log n)` that sorting the input itself costs.

**Where the expected O(1) comes from, and what it costs to state it honestly.** The
`O(1)` is a statement about an *average over inputs*, not a guarantee. It is exactly the
same statistical claim as §2.6 of the card: a hash container is fast when the hash
distributes well, and degrades smoothly to linear when it does not. The JDK's `HashMap`
adds treeification (bins become red-black trees past a threshold, bounding a lookup at
`O(log n)` even under adversarial keys); a teaching implementation does not, and the
difference between "expected `O(1)`" and "amortised `O(1)` with a worst-case bound" is
the difference between §2.6 and a production container.

### 6. Test table

`distinct` on an `int[]`, verified against a brute-force reference in the same harness.
Expected values were written down first; the run is reproduced at the end of this
section.

| # | input | expected | sentinel-0 impl | why it is the interesting case |
|---|-------|----------|-----------------|------------------------------|
| 1 | `[]` | 0 | 0 ✅ | empty input, the off-by-one candidate |
| 2 | `[7]` | 1 | 1 ✅ | single element, `n−1 = 0` loop guard |
| 3 | `[1,1,1,1]` | 1 | 1 ✅ | all equal; the brute force's `break` looks efficient here |
| 4 | `[1,2,3,4]` | 4 | 4 ✅ | all distinct; the `Θ(n²)` worst case for the brute force |
| 5 | `[0,0]` | 1 | **2 ❌** | `0` is the sentinel. `add(0)` writes the empty marker and the size increments again on the second call |
| 6 | `[0,0,1]` | 2 | **3 ❌** | the row that makes row 5 unambiguous: two zeros, so the error is visible in the count |
| 7 | `[0,1]` | 2 | 2 ✅ | **passes, and that is the lesson** — see below |
| 8 | `[4,8,12]` | 3 | 3 ✅ | all `≡ 0 (mod 4)`, so under `& 3` every key shares a home slot: the linear-probe collision path |
| 9 | `[1,5,9,13]` | 4 | 4 ✅ | same bucket, all distinct, so the probe walks 4 slots and still returns 4 |
| 10 | `[3,1,3,1,3]` | 2 | 2 ✅ | interleaved duplicates; checks the probe terminates on repeat |
| 11 | 10⁶ random in `[0,10⁶)` | 632,121 (Poisson) | 631,575 | relative error **0.086%** — asserts the *distribution* is right, not one value |

**Row 7 is the one worth arguing about.** `[0,1]` returns the correct 2, so it is the
test that makes a reviewer think the zero case is handled. It passes *by accident*: one
zero over-counts the size by one, and the missing element is the sentinel itself, so the
count accidentally lands on the right answer. Row 5 and row 6 are the same bug without
the coincidence. A test that passes for the wrong reason is worse than no test, because
it is later quoted as evidence.

### 6b. The standard fix, and the bug it creates

The textbook repair is to store `key + 1` and keep `0` as the empty marker. Verified
against the same expectations:

| input | expected | sentinel-0 | store `key+1` |
|---|---|---|---|
| `[0,0]` | 1 | 2 ❌ | 1 ✅ |
| `[0,0,1]` | 2 | 3 ❌ | 2 ✅ |
| `[0,1]` | 2 | 2 ✅ | 2 ✅ |
| `[-1]` | 1 | 1 ✅ | 1 ✅ |
| **`[-1,-1]`** | **1** | **1 ✅** | **2 ❌** |
| `[-1,5]` | 2 | 2 ✅ | 2 ✅ |
| `[MAX,MAX]` | 1 | 1 ✅ | 1 ✅ |

`key + 1` repairs every zero case — and moves the sentinel collision to `-1`, because
`-1 + 1 == 0`, which is the empty marker. `add(-1)` writes the marker, and the second
`add(-1)` does not see the key, so it counts twice.

**The general lesson, now measured rather than asserted:** a sentinel-based container
needs a value outside the key domain, and `int` has no such value. Every choice leaks
somewhere, and which inputs fail is a property of the *encoding*, not of the logic. The
fixes that actually close it are structural — a separate `boolean[] occupied` (extra
memory, extra cache line), or `Integer` boxing with `null` as the marker (the memory
cost of Ch01's boxing lesson), or a `long`/`long+1` pair if the range allows it. This is
the same trade the chapter's card describes for `equals`: a small encoding decision
silently determines which inputs are correct.

### 6c. Verified run

```
input          expected   IntSet    brute  verdict
[]                    0        0        0  ok
[7]                   1        1        1  ok
[1,1,1,1]             1        1        1  ok
[1,2,3,4]             4        4        4  ok
[0,0]                 1        2        1  *** MISMATCH *** (brute correct: true)
[0,0,1]               2        3        2  *** MISMATCH *** (brute correct: true)
[0,1]                 2        2        2  ok
[4,8,12]              3        3        3  ok
[1,5,9,13]            4        4        4  ok
[3,1,3,1,3]           2        2        2  ok

row 10: 1e6 random ints in [0,1e6) -> got 631575, Poisson expects 632121
  relative error 0.086%  -> ok
```

Note that the brute-force reference is **correct on every row**, including the two the
implementation fails. When two methods disagree, the slower one being obviously right is
what makes the disagreement diagnostic rather than a coin flip.

### 7. What I got wrong building this

1. **I wrote `[0,1]` into the test table and claimed it would catch the sentinel bug.**
   The run printed `2` — the *expected* answer. Row 7 passes by accident, because a single
   zero over-counts the size by exactly one and the sentinel itself is the element that
   got lost. I then had to add `[0,0,1]` to get a row that actually fails. A test row is
   a hypothesis about a failure mode; writing it from the code instead of from the
   failure mode is how you get tests that agree with the bug.
2. **I asserted that the `key + 1` fix "changes the answer for `Integer.MAX_VALUE`".**
   Checked: it does not — `MAX + 1` overflows to `MIN_VALUE`, which is a perfectly good
   stored value, and `[MAX,MAX]` still returns 1. The real breakage is at **`-1`**, where
   `key + 1 == 0` lands exactly on the empty marker. I had memorised "the fix breaks
   overflow" and never checked which value actually matters. The bug moved to a
   *different* input than the one I predicted, which is the normal outcome of reasoning
   about sentinel encoding from memory.
3. **My first brute-force complexity note said the `break` makes it `O(n)` on
   all-duplicate input.** It does not matter either way (that input is `O(n)`, distinct
   input is `O(n²)`, both bad), but I had written it as if the `break` were the saving
   grace rather than an accident that helps exactly one input shape.
4. **I asserted "expected `O(1)`" and then wrote a correctness argument that does not
   depend on any expectation** — the invariant holds for *every* probe sequence. Those are
   different claims: the invariant is about correctness, the `O(1)` is about time. Only
   one of them is average-case, and I had them tangled in the same paragraph.
5. **I wrote the `brute` reference as ground truth without checking it against anything.**
   It happens to be correct on all 11 rows, but a reference implementation is also code
   and can be wrong. I only trusted it *after* seeing that it agreed with my hand-written
   expected column on the two rows where the fast version disagreed — which is the order
   in which a reference earns the right to be called one.

---

## 5 closed-book questions

1. You override `equals` but not `hashCode`. A `HashSet` reports size 2 for two objects
   your `equals` calls equal. Name the two different reasons `javac` may or may not help
   you here, and explain why the "one fixture, added twice" test cannot catch it.
2. `hashCode` returns `0` for every instance. Is the resulting `HashMap` *incorrect*?
   Is it *slow*? Which of those two is `javac` able to detect, and why is that asymmetry
   the dangerous one?
3. A base class uses `instanceof` in `equals`; a subclass adds a field and uses
   `instanceof Subclass`. Give the two results of `a.equals(b)` and `b.equals(a)` for a
   base instance and a subclass instance, and name the contract clause that is broken.
4. Explain why `s.area()` and `s.field` can resolve to *different* objects, using the
   two-decision model. Which one would `s.field` have to be for polymorphism to work?
5. You measure a virtual call at ~3.3 ns and a direct call at ~0.6 ns. Name the two
   conditions under which the direct-call figure applies to a *polymorphic* call site,
   and what happens to the ratio when a fifth type appears.

---

## 🔗 Diary link

- [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

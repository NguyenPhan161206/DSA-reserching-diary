# Teach-Backs — Part I (Ch 01–08)

> Protocol step 7: 100–150 words per chapter, explaining to a beginner, without notes.
> One core mental model per chapter, not a summary.

---

## Ch 01 — Computers, Programs, and Java

`javac` does not make a program; it makes **bytecode**, and the CPU never sees your
Java. Between your file and the machine there are two translators: the JVM loads the
bytecode and starts interpreting it, then re-translates the hot loops into real
machine code. That is why the first batch of a loop is slow and the tenth is fast —
measure, and you see the compiler working. Two habits followed: type sizes are
facts you print (`Boolean` has no `BYTES` — I asserted it existed because I wanted it
to), and "boxing is slow" is a myth I repeated until a harness said otherwise: an
`Integer[]` costs **more memory (12.5×)** than an `int[]`, but time barely moves
because the JIT unboxes in the loop. Cost first, story later. That ordering is the
chapter.

## Ch 02 — Elementary Programming

`int` and `double` are not two spellings of "number"; they are two machines. `int`
wraps at 2³¹ with silence — `Integer.MAX_VALUE + 1` is `Integer.MIN_VALUE` — and
`double` never wraps, it just rounds, losing the last digits once you cross 2⁵³.
From that one sentence, the famous traps are consequences, not trivia: `(int) 1e20`
saturates to `MAX_VALUE`, `9007199254740993` rounds to `9007199254740992`, and
`byte b = 127; b += 1` compiles while `b = b + 1` does not because `+=` carries a
hidden cast. And `Scanner` decides your number by the *machine's locale*, not your
string: `12,5` is an exception on my laptop and 12.5 on another. Every trap was a
warning I needed a run to believe.

## Ch 03 — Conditional Statements

`&&` and `||` are not faster versions of `&` and `|`; they are **conditional**
evaluation. The right operand runs only if the left one doesn't already settle the
answer. That difference is a crash or a hang: `s != null && s.length() > 0` is safe
only because the right side may never run; with `&`, the `null` dereference happens
every time. The book's "one is faster" framing is worse than wrong — it hides that
the *guarantee* the condition makes is the point. I kept my own micro-benchmark of
the two hoping for a speed gap; the only measurable thing was "they compute the same
boolean". The claim is not about clocks at all, and trying to turn it into a timing
race was category error.

## Ch 04 — Functions, Characters, and Strings

`String` is immutable, and every string puzzle is that one fact unfolded. "Append a
character" must allocate a new object and copy everything that was there, so building
a long string with `s += c` is O(n²) — copying the growing string once per character.
`StringBuilder` exists because "mutable field of characters" is a *different data
structure*, giving O(n). `==` on strings is a reference question, so it is right/wrong
depending on the JVM internals, which is why `equals` exists. I measured my own
`StringBuilder` prediction ("bigger capacity = faster?") and it barely moved — the
real win was already there in the design. Complexity comes from the data structure's
contract, not from how fast you type the operator.

## Ch 05 — Loops

A loop is `(state, step, exit condition)`, and the exit condition alone sets the
complexity: `for (int i = 0; i < n; i++)` runs n times, written `O(n)`, but the same
letters with `i += 2` cut it to `⌈n/2⌉`. Count trips as formulas before running, or
you will inherit the textbook's error: a hand note once told me `⌊(n-1)/3⌋+1` "differs
from `⌈n/3⌉` in general" — it never does; the two are the same number. The state
decides representability (an `int` counter both wraps and sets the ceiling on how
much you can sum). GCD showed the sharpest version: Euclid is O(log n) measured in
*steps*, and the count of a naive subtract loop has an exact formula that needs a
divisibility caveat — formulas are derived, not recalled.

## Ch 06 — Arrays

An array variable is a **reference to a block**, not the block. So `int[] b = a;`
does not copy; it makes a second name for one array, and `b[0] = 9` visibly changes
`a`. `a.clone()` sounds like copying and is only a shallow copy — fine for `int[]`,
a lie for `int[][]`, where the inner rows are shared. This is not library trivia; it
is the difference between a `for` loop that mutates in place and one that reads stale
data. The mental picture fixes the bugs: a *copy* is built element by element, and
`clone()` is "new name for the same references". Null rows, ragged rows, and the
capacity/length split all read off the same picture.

## Ch 07 — Object-Oriented Programming

`equals` and `hashCode` are a contract, and Java punishes breaking it: equal objects
with different hash codes land in different buckets, so `HashSet` reports two
objects where one exists, and `HashMap.get()` returns `null` for a key that is
`in`. An overridden `equals` without a touched `hashCode` gets you a compiler warning
and a silent bug. Behind that, dispatch is not free but it is cheap: the same call
dispatched to three types cost ~3 ns and ~5× a monomorphic loop — real, but nothing
like the folklore. Static members are chosen at compile time; a `Circle` typed as
`Shape` calls `Shape.describe()`. Asymmetries (`car.equals(ev)` vs `ev.equals(car)`)
make the contract the safest ground to argue from.

## Ch 08 — Multidimensional Arrays

`int[][]` is an array **of `int[]` references** — the "matrix" is a picture of data
shape, not memory. Ragged rows, `null` rows, and shallow `clone()` are that one
sentence. Locality made the cost visible: iterating a 3000×3000 matrix in the wrong
order ran 6–8× slower, and the gap collapsed to nothing at 8×8 — proving it is about
cache, not about `for` syntax. Blocked matrix multiply kept the same nested loops and
still only bought ~3× at the right block size, because the machine deduplicates.
Every surprising number in this chapter came from a run I forced myself to make after
my first-draft "memory" values were all wrong.

---

*Written 2026-09-28, after the Part I audits (Ch 01 bytecode/boxing, Ch 02 Scanner +
exercises + L3, Ch 05 trip-count identity) were closed.*
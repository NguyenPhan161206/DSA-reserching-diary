# Ch 04 — Functions, Characters, and Strings

> **Claim of the chapter:** `String` is immutable, and that single design decision —
> not "strings are special" — is the reason behind a $O(n^2)$ loop, the reason `==`
> fails, and the reason `hashCode` is a polynomial. Every cost in this chapter is a
> consequence of one word: **final**.

**Status:** 🟢 green — every number below was produced by the two lab files in `lab/`
**Messages:** M2 (types move errors earlier), M1 (every line has a cost), M3 (complexity
is the real spec)
**Labs:** `Ch04StringImmutability.java`, `Ch04StringIdentity.java`
**Diary:** [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

---

## 1. The claim (derived, not recalled)

The book introduces `String` as "a class that represents character strings", and treats
`+` as a convenience. That framing hides the mechanism. The mechanism is:

> A `String` cannot be modified. So "append a character" cannot mean "put a character at
> the end of the existing object". It has to mean **"allocate a new object and copy every
> character that was already there"**.

Every consequence follows mechanically:

| the word `final` on the field | what it forces | what you pay |
|---|---|---|
| cannot append in place | `s += c` must copy `len(s)` chars | $O(n)$ per iteration → $O(n^2)$ loop |
| two equal-content strings may be distinct objects | `==` compares identity → can be `false` | wrong answers, silent |
| contents never change, so the hash never changes | `hashCode` can be cached and is a pure function of content | a fixed polynomial → collisions exist but never "break correctness" |

That is the whole chapter in one table.

## 2. Evidence

### 2.1 The O(n²) loop, produced and timed

`lab/Ch04StringImmutability.java`, `whyConcatIsQuadratic()`:

```
  iteration 0: length 0 -> 1, new object, old one is garbage
  iteration 1: length 1 -> 2, new object, old one is garbage
  iteration 2: length 2 -> 3, new object, old one is garbage
  iteration 3: length 3 -> 4, new object, old one is garbage
  iteration 4: length 4 -> 5, new object, old one is garbage
  final: 01234
  the loop did 4 copies of a growing string: 1 + 2 + 3 + 4 = 10 char copies
  to produce a 4-char result. It works. It is just O(n^2).
```

The copy count is the arithmetic, not a guess: to build a length-$n$ string by
concatenation you copy $1 + 2 + \dots + (n-1) = \frac{n(n-1)}{2}$ characters, which is
$\Theta(n^2)$.

Then the three ways to build a 20,000-char string:

```
  String s += 'x'            76.8 ms
  StringBuilder append        1.3 ms   59x faster
  char[] + new String         0.5 ms   140x faster
```

**Derivation, not recall:** `StringBuilder` is $\Theta(1)$ amortised per append because the
buffer doubles, so the total copy work is $n + n/2 + n/4 + \dots = O(n)$ — a geometric
series. The `char[]` version is $\Theta(n)$ because we fill a buffer we own, in place, with
no intermediate objects at all. The measured 59× and 140× are the concrete sizes of a gap
that is only $O(1)$ in theory.

### 2.2 A prediction of mine that measurement refused to confirm

`sbCapacity()` — I expected pre-sizing `new StringBuilder(20000)` to win, because the
default one reallocates:

```
  new StringBuilder()             0.6 ms   (model predicts 11 reallocations)
  new StringBuilder(20000)        0.5 ms   (0 reallocations)
  MEASURED: no difference. The model predicted a win and there was none.
```

The model is right about the reallocations and irrelevant about the cost. 11
reallocations copy at most ~36k characters **in total**, against 20,000 appends. The loop
is bound by the appends, not by the copies, so the amortisation is already doing its job
and removing it saves nothing measurable.

**This is the honest shape of most "optimisation" advice:** *amortised* already means the
worst case is paid rarely enough that you have to engineer a workload to feel it. Do not
carry "always pre-size a StringBuilder" as a rule; carry "StringBuilder is $O(1)$
amortised, and the constant is small" and measure the rest.

(Also worth recording: the JDK's actual growth rule is `(old * 2) + 2`, not `old * 2`. My
first version of the lab modelled plain doubling. The reallocation *count* happens to be
the same here, which is exactly the kind of coincidence that makes a wrong model look
right.)

### 2.3 `==` on String: identity, and the trap that makes it look reliable

`lab/Ch04StringIdentity.java`, `literalsAreInterned()` and `builtStringsAreNot()`:

```
  a = "hello", b = "hello"      a == b -> true    a.equals(b) -> true
  c = new String("hello")       c == a -> false   c.equals(a) -> true
  c.hashCode() = 99162322, a.hashCode() = 99162322   (equal!)

  "ab" == "a" + "b"                  -> true   <- TRUE, and that is a trap
  "ab" == new StringBuilder("ab")    -> false
  "ab" == String.valueOf(char[])     -> false
  all three equal()? true / true / true   (content is identical)
```

**The first line of that block is the important one, and it is the opposite of what most
people expect.** `"ab" == "a" + "b"` is `true` — but *not* because concatenation interns
its result. It is `true` because `javac` **constant-folds** `"a" + "b"` into the single
interned literal `"ab"` at compile time. The `+` never runs. Two different mechanisms,
same observable result, and the one that would *not* hold is the one people believe.

The result: `==` on strings works on your test cases (which are literals) and fails on the
input that came from a scanner. This is the single most common silent WA in Java judged
code, and the failure is *not* a wrong answer from the algorithm — it is a wrong answer
from the comparison operator.

Note also that `hashCode` is **equal** for `c` and `a` while `==` is `false`. `hashCode`
answers "what bucket?", `==` answers "same object?". **They are different questions, and
only one of them is what you meant.**

### 2.4 `hashCode()` is a fixed polynomial, so collisions are constructible

Same lab, `hashCollisionsAreReal()`. The spec is `s[0]*31^(n-1) + s[1]*31^(n-2) + …`, and
the lab computes it **by hand** in `manualHash()` and compares to the JDK's answer:

```
    Aa     -> 2112
    BB     -> 2112
    hello  -> 99162322
    AaAa   -> 2031744
    BBBB   -> 2031744
  and by hand:  'A'*31 + 'a' = 65*31 + 97 = 2112
                 'B'*31 + 'B' = 66*31 + 66 = 2112
  both are 2112.
```

`65*31 + 97` and `66*31 + 66` are both 2112, and the hand computation matches the JDK for
every string in the table. Any two characters whose code points sum to the same value
collide, so **two-character collisions are trivial to construct by hand** — you do not
have to search for them.

Why this is safe and why it is still worth knowing: a `HashMap` uses `hashCode` only to
pick a bucket, then confirms with `equals`. So a collision costs **time, never
correctness**. `whatHashMapActuallyDoes()` shows the consequence:

```
  put(new String("key"), ...) then map.get("key") -> value
  size = 1   (the new String and the literal are the same key)
```

The `new String("key")` and the literal are *different objects* (`==` is `false`) but the
map still finds it, because `hashCode` matched and `equals` confirmed. That is the whole
contract in two method calls, and it is why overriding `equals` without `hashCode` is the
one override combination the compiler warns about.

## 3. Counter-example — where the intuition breaks

**Intuition to break:** "`String` is immutable, therefore `String` operations are slow, so
prefer `char[]`."

This is wrong in two measurable ways:

1. **A single `+` is not slow.** `s + t` is $O(|\!s\!| + |t|)$ and produces exactly one
   new object. For concatenating a *fixed small number* of pieces — which is most real
   code, including `System.out.println("a" + x + "b")` — there is nothing to fix. The
   quadratic behaviour needs an **unbounded loop**.
2. **`char[]` gives up everything `String` provides.** No `length()` that reads `O(1)`
   without a field, no `substring` that does not copy, no `equals`, no null-check, no
   immutability safety. You only want `char[]` when you are building a buffer *and* you
   own its whole lifetime — the `n = 20_000` loop in §2.1, not an editor's text buffer.

The counter-example that keeps it honest: **the `String` result of the `char[]` loop is
still a `String`.** The mutation was a temporary implementation detail; the *value* handed
to the next layer is immutable, which is what made the code safe to pass around. Speed did
not require giving up the type's guarantees — it required using the right object *while*
building, then returning to the right type.

## 4. Anti-message

> **Anti-message:** "`==` is faster than `equals`, so use `==` for strings."

Measured, this is not even a speed argument — both are $O(1)$ for a length check plus a
comparison, and `equals` short-circuits on the first differing character. It is a
**correctness** argument, and the anti-message is dangerous because it frames a
correctness bug as an optimisation opportunity.

> **Anti-message:** "A `String` is a `char[]` with methods, so use `char[]` for
> performance."

Wrong, per §3. The right statement is narrower: `StringBuilder` for unbounded building,
`char[]` only when you need a buffer you can fill in place *and* you own its lifetime, and
`+` for everything else.

## 5. What the book does not say out loud

1. **The book teaches `s += c` and never mentions that it is quadratic inside a loop.**
   The syntax is identical in the loop and out of it, and the cost is not. A student who
   has only read the syntax will write a $O(n^2)$ string builder in a judged solution and
   have no idea why it times out at $n = 10^5$ when the algorithm is $O(n)$.

2. **The book does not distinguish "interning" from "constant folding",** and the Ch 04
   lab exists partly because I conflated them myself (§2.3). `"a" + "b"` folding to
   `"ab"` is `javac` doing arithmetic on constants. `"" + x` with a runtime `x` is a real
   runtime `StringBuilder` and a real new object. Both are "concatenation"; only one is
   folded. **The general form: constant expressions are evaluated by the compiler, so any
   property that holds for constants can hold for literals in surprising ways.**

3. **The book does not connect `hashCode` to the hash map** — `hashCode()` in Ch 4 looks
   like a curiosity. It is the *first half* of the contract that `HashMap` (Ch 20) depends
   on, and the reason "override `equals` without `hashCode`" is a bug. Reading Ch 4's
   `hashCode` as a throwaway detail is what makes Ch 20 feel arbitrary later.

4. **The book does not say that a `String` cannot be a key that changes.** Because the
   contents cannot change, a `String` key can be cached in a hash bucket forever. Mutable
   "keys" must be removed and re-added, which is the whole reason for `remove` in a
   `HashMap` API. **Immutability is not a limitation here; it is what makes the data
   structure work.**

## 6. 🔄 What changed in Java 21 (delta vs the book's Java-8 baseline)

| Old (book) | Java 21 | Why it matters |
|---|---|---|
| `new String("x")` used without thought | still exactly as wrong — `new String(String)` is a **no-op copy** and the JDK does not optimise it away for you | the trap in §2.3 is unchanged; do not "optimise" by removing a `new String` you thought was expensive |
| `s == t` and `s.equals(t)` | unchanged, still two different questions | the §2.3 trap is a language rule, not a version detail |
| `String` in a `switch` | compiles to `hashCode` + `lookupswitch` + `equals` (Ch 03 §2.4) | the two questions come back at the `switch` level, resolved in your favour |
| `String.chars()`, `String.codePointAt` intuition | `Character.isAlphabetic(int)` and code-point iteration (`for (int cp : s.codePoints().toArray())`) | Ch 04's ASCII assumption breaks on emoji/CJK; the correction is code *points*, not `char` |
| `String` used as a `switch` selector with a `null` | NPE at the `hashCode()` call | a failure **boundary** (M4): `switch` on a `String` does not tolerate `null` |
| `new String(byte[], Charset)` | `StandardCharsets` constants; the 2-arg `getBytes(Charset)` is still required for explicitness | encoding is a **boundary**, not a detail; the default charset differs between machines, which is a real source of WA |

**What has NOT changed:** immutability, `$O(n^2)` for `+=` in a loop, `StringBuilder`'s
amortised $O(1)$, the `hashCode` polynomial, and the interning of literals. **Every
statement in §2 is a Java-8-true, Java-21-true fact.** The delta rows are about *what you
should do instead*, not about the mechanism.

## 7. 🔗 Diary link

- [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)
  — the `String`/`char[]` decision reappears in every Java DSA solution as the
  "build the output buffer" question; §2.1's 59× is the number that decides it.

## 8. Compiled and run — actual output

```
$ ./java-course/00-toolchain/run.sh \
    java-course/p1-fundamentals/ch04-functions-characters-strings/lab/Ch04StringImmutability.java

--- `s += x` allocates a NEW string every iteration ---
  iteration 0: length 0 -> 1, new object, old one is garbage
  iteration 1: length 1 -> 2, new object, old one is garbage
  iteration 2: length 2 -> 3, new object, old one is garbage
  iteration 3: length 3 -> 4, new object, old one is garbage
  iteration 4: length 4 -> 5, new object, old one is garbage
  final: 01234
  the loop did 4 copies of a growing string: 1 + 2 + 3 + 4 = 10 char copies
  to produce a 4-char result. It works. It is just O(n^2).

--- three ways to build a 20000-char string ---
  String s += 'x'            76.8 ms
  StringBuilder append        1.3 ms   59x faster
  char[] + new String         0.5 ms   140x faster

  all three: 20000, 20000, 20000
  the char[] version is not 'better Java', it is a different data structure:
  it can be filled in place because the buffer is mutable and owned by us.

--- StringBuilder is amortised, which is not the same as free ---
  new StringBuilder()             0.6 ms   (model predicts 11 reallocations)
  new StringBuilder(20000)        0.5 ms   (0 reallocations)
  MEASURED: no difference. The model predicted a win and there was none.
  Why: 11 reallocations copy at most ~36k chars in total, against 20,000
  appends. The loop is bound by the appends, not the copies, so the
  amortisation is already doing its job. Pre-sizing pays when the
  final length is unknown or the appends are expensive — not here.
```

```
$ ./java-course/00-toolchain/run.sh \
    java-course/p1-fundamentals/ch04-functions-characters-strings/lab/Ch04StringIdentity.java

--- literals are interned, so `==` appears to work ---
  a = "hello", b = "hello"   a == b -> true    a.equals(b) -> true
  same identity? true   (a.hashCode() = 99162322)
  c = new String("hello")      c == a -> false   c.equals(a) -> true
  same identity? false   (c.hashCode() = 99162322, EQUAL to a's)
  -> the one character-for-character operator gives the WRONG answer,
     and both objects have the same hashCode, because hash is about
     CONTENT and == is about IDENTITY. Two different questions.

--- anything built at runtime is a fresh object ---
  "ab" == "a" + "b"                  -> true   <- TRUE, and that is a trap
  "ab" == new StringBuilder("ab")    -> false
  "ab" == String.valueOf(char[])     -> false
  all three equal()? true / true / true   (content is identical)
  -> the FIRST one is true because javac constant-folds "a" + "b" into
     the single interned literal "ab" before it ever runs. It is not
     because string concatenation interns its result — it does not.
     So `==` looks reliable for a season, then fails on the one input
     that came from a scanner: the classic "0 on mine, WA on theirs".

--- hashCode() is a polynomial, so collisions are constructible ---
  "Aa".hashCode() = 2112
  "BB".hashCode() = 2112
  equal content? false   same hash? true

  the formula is sum(s[i] * 31^(n-1-i)):
    Aa     -> 2112
    BB     -> 2112
    hello  -> 99162322
    AaAa   -> 2031744
    BBBB   -> 2031744
  and by hand:  'A'*31 + 'a' = 65*31 + 97 = 2112
                 'B'*31 + 'B' = 66*31 + 66 = 2112
  both are 2112. Any two chars with codes c1, c2 and
  c1 + c2 equal collide, so two-char collisions are trivial to build.
  (HashMap survives this because it falls back to equals() on the
  bucket; collisions cost time, they do not cost correctness.)

--- a HashMap needs BOTH hashCode and equals ---
  put(new String("key"), ...) then map.get("key") -> value
  size = 1   (the new String and the literal are the same key)
  HashMap called hashCode (equal), found the bucket, then called
  equals (equal) -> hit. If equals were identity, this would MISS.
  A class that overrides equals WITHOUT hashCode still breaks the map,
  which is why the compiler warns on exactly that combination.
```

## 9. What I Got Wrong

- **I fabricated arithmetic in a comment.** The first version of `Ch04StringIdentity`
  explained the `Aa`/`BB` collision with the line `2012 + 97 = 2059; 2046 + 66 = 2052`,
  and followed it with "…close, but the real check is computed above". That is three
  wrong numbers printed with the word "close" as a hedge, in a file whose entire purpose is
  to show that the arithmetic is exact. The correct values are `65*31 + 97 = 2112` and
  `66*31 + 66 = 2112`. The program had printed 2112 all along — **I had a correct number
  on screen and still wrote a wrong explanation next to it.** The lab now computes the two
  products in the `printf` instead of hard-coding them, so the prose cannot drift from the
  output again.
- **I generalised a `true` result into a false rule.** My first version of
  `builtStringsAreNot()` concluded "`==` is false in every case" — while the run above it
  showed `"ab" == "a" + "b"` printing `true`. I had not read my own output. The truth is
  more interesting than my rule: it is `true` because of *compile-time constant folding*,
  which is a completely different mechanism from interning and does not generalise. Fixed,
  and the card now leads §2.3 with that line because it is the actual trap.
- **My `StringBuilder` capacity model used the wrong growth formula.** I wrote
  `capacity <<= 1` and called it "grows by doubling". The JDK uses
  `(old * 2) + 2`. The reallocation count came out the same, which is precisely why it
  survived a run — and it will not survive the next size. A model that produces the right
  answer for the wrong reason is worse than no model, because it will be reused.
- **My own prediction of a speedup was wrong, and the honest thing was to keep the
  measurement.** I predicted pre-sizing a `StringBuilder` would win (§2.2). It did not:
  0.6 ms vs 0.5 ms, i.e. nothing. I was tempted to drop the experiment, but "the advice I
  was about to give is not measurably true" is the single most useful thing this chapter
  produced. It stayed in, with the reason the model was irrelevant.
- **I nearly wrote a card section that asserted "amortised means you feel it".** That is
  the opposite of what amortised means, and I only caught it by re-reading the definition
  after the measurement contradicted me.

## 10. Open questions (to measure, not to guess)

- [ ] How many appends does it take for a *single* `StringBuilder.append` to dominate the
      cost of the buffer copy? Instrument `String.length()` inside the loop, or use JFR's
      allocation profile to count the 11 reallocations directly instead of modelling them.
- [ ] Is `s.concat(t)` slower than `s + t`? Both allocate once; the question is whether
      `concat` is even in the JDK's fast path or a leftover for compatibility.
- [ ] The `Aa`/`BB` collision is free to construct. How many keys can you put into a
      `HashMap` with a *chosen* 2-character alphabet before it degenerates? That is the
      real reason `HashMap` randomises or salts bucket indices in adversarial settings.
- [ ] Does `String.hashCode` on a `null`-tolerant wrapper (e.g. `Objects.hashCode(s)`)
      actually protect a `HashMap` key, or only avoid the NPE? What breaks downstream?
- [ ] Count allocations for the three approaches in §2.1 with
      `com.sun.management.ThreadMXBean.getThreadAllocatedBytes` and check whether the
      *times* in §2.1 line up with the *bytes*. If they do not, the bottleneck is not
      allocation and the card's explanation is incomplete.

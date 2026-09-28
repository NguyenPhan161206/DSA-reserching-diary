# Ch 03 — Conditional Statements

> **Claim of the chapter:** a condition is not a place to store a value, and it is not
> one boolean either. It is a *narrowing point* where Java decides a type, and the
> decision is made by the compiler, silently, before your program ever runs.

**Status:** 🟢 green — every number below was produced by the two lab files in `lab/`
**Messages:** M2 (types are a tool that moves errors earlier), M1 (every line has a cost),
M3 (complexity is the real spec)
**Labs:** `Ch03ShortCircuit.java`, `Ch03ConditionalTypes.java`
**Diary:** [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

---

## 1. The claim (derived, not recalled)

Java has **two** boolean operators and they are not interchangeable, and the textbook
treats the difference as "one is faster". That framing is wrong, and it is *dangerously*
wrong, because the faster one is the one that can crash your program.

The real claim is:

> `&&` and `||` are **conditional evaluation** — the right operand is only evaluated
> when the left operand does not already determine the answer. `&` and `|` are **bitwise**
> — both operands are always evaluated, and *the result is always a boolean*.

So `&&` is not "the slow version of `&`". It is a different operator with a different
contract, and the contract is about **safety**, not speed.

The consequence for a DSA student: `i < arr.length && arr[i] > 0` is correct code.
`i < arr.length & arr[i] > 0` is not a slower version of it — it is a program that
*looks* like it has a bounds check and does not have one.

## 2. Evidence

### 2.1 The semantics difference, as a crash (not a timing)

`lab/Ch03ShortCircuit.java`, `semanticsFirst()`, with `data = {1, 2, 3}` and `i = 3`:

```
data.length = 3
i < data.length && data[i] > 0   -> false
i < data.length & data[i] > 0    -> ArrayIndexOutOfBoundsException  <-- the guard did nothing
```

`&&` returns `false` and never touches `data[3]`. `&` evaluates `data[3] > 0`, throws, and
the crash happens **on the same line that contains the bound check**. A reader scanning
the source sees a guard. The program has no guard.

This is the single most useful thing in the chapter, and it has nothing to do with
performance.

### 2.2 The cost difference is not a constant

Same lab, `timingSecond()`, 50,000,000 iterations, guard `k < 1` always false, so the
right operand is always skipped under `&&`:

```
  (a) cheap predicate — 1 inner step
    k < 1 && predicate      22.1 ms   (hits 0)
    k < 1 &  predicate      87.8 ms   (hits 0)  4.0x

  (b) costly predicate — 200 inner steps
    k < 1 && predicate      26.1 ms   (hits 0)
    k < 1 &  predicate    6909.2 ms   (hits 0)  264.8x
```

| right-hand operand cost | ratio `&` / `&&` |
|---|---|
| 1 inner step | 4.0× |
| 200 inner steps | 264.8× |

**The ratio tracks the cost of the skipped operand, not the loop.** So the rule that
survives is *"the right side must be safe to skip"*, not *"`&&` is faster than `&`"*.
Anyone who memorises the second statement will write `&` in the one place it is illegal.

### 2.3 The ternary operator decides your type for you

`lab/Ch03ConditionalTypes.java`, `ternaryPromotes()`:

```
  true ? 1 : 2.0      -> 1.0      (Double)
  false ? 'a' : 98   -> code point 98   (Character)
  true ? 1 : 'a'      -> code point 1   (Character)

  even ? 1/2 : 1.0/2   -> 0.0      (Double)
```

Three separate traps in four lines:

1. `true ? 1 : 2.0` is a `Double`. Write `int x = cond ? 1 : 2.0;` and it will not
   compile; write `Object x = ...` and it compiles, and `x` is a `Double`.
2. `false ? 'a' : 98` is a `Character`. The `int` 98 narrowed to `char` 98.
3. `true ? 1 : 'a'` is **code point 1** — a control character, unprintable. This is not
   "1 vs 'a'"; JLS 15.25 says that when one operand is `byte`/`short`/`char` and the
   *other is a constant expression representable in that type*, the result type is that
   narrow type. `1` is representable as `char`, so the whole expression becomes `char`.

**The thing the book does not say out loud:** the ternary operator is the only place in
Java where a *literal* can silently change the type of a larger expression. `int i = 1;
long j = 1;` — no drama. But `1` alone in a conditional position carries type information
that the compiler will use against you. If a conditional expression is not doing what you
expect, **print the static type before printing the value.**

### 2.4 A String switch is a hash lookup, not a chain of `==`

`javap -c` on `dayType(String)`, and this is the actual bytecode, not a paraphrase:

```
  private static java.lang.String dayType(java.lang.String);
       4: aload_1
       5: invokevirtual #125    // Method java/lang/String.hashCode:()I
       8: lookupswitch  { // 7
                 69885: 160
                 76524: 104
                 81862: 76
```

and the hashes it switches on are visible at runtime:

```
  "MON".hashCode() = 76524
  "TUE".hashCode() = 83428
  "SAT".hashCode() = 81862
```

76524 appears in both. The compiler computed the hash of every case label at compile
time, emitted a `lookupswitch` on the bucket, and keeps an `equals` for confirmation. This
is why `switch` on strings does **not** suffer the `==`-on-strings trap of Ch 04: it never
uses reference identity.

## 3. Counter-example — where the intuition breaks

`&` is *not* the "unconditional double" in the sense of being useless. The moment you are
working with `boolean` **arrays** or `boolean` **fields**, the bitwise form is the *only*
form, and it is the one you want:

```java
boolean[] flags = {true, false, true};
int mask = 0;
mask |= flags[0] ? 1 : 0;      // set bit 0
boolean allOnes = (mask & 0b111) == 0b111;   // test three bits at once
```

Here `&&` cannot be used at all: `&&` yields a `boolean`, it does not fold two bits
together. `&` is the *set* operation, and it is the correct tool. **The operator is not
"the slow one"; it is the one that operates on representation rather than on truth.**

The chapter collapses into one sentence: *reach for `&&` when you are guarding work, and
for `&` when you are working on bits — and never trade the first for the second.*

## 4. Anti-message

> **Anti-message:** "`&&` is faster than `&`, so use `&&` everywhere to optimise."

This is wrong twice. It is wrong in the *safe* direction (it will tempt you into `&` where
`&&` is required, and you get a crash rather than a slowdown), and it is wrong in the
*useless* direction (in a well-written program the right operand of `&&` is either
skipped or genuinely needed, so there is nothing to optimise).

The correct anti-message is:

> **Anti-message:** "`&` is just a slower `&&`, so the choice is a style question."

It is a **semantics** question. `&` has a different meaning. Style and correctness are not
on the same axis.

## 5. What the book does not say out loud

1. **The book teaches the ternary operator as syntax; the real content is type
   inference.** Every conditional expression in Java has a static type computed by the
   compiler from *both* branches, and in one direction (narrowing to `byte`/`short`/`char`)
   it is chosen by a *constant expression* in the other branch. That is the whole
   mechanism behind `true ? 1 : 'a'` being a `char`.

2. **The book does not connect the ternary operator to the `char`-vs-`int` family from
   Ch 02.** The narrowing you learned in Ch 02 happens *implicitly* here, without a cast,
   which is why it is more dangerous: a cast announces itself, a ternary does not.

3. **The book never mentions that the old `switch` can be wrong rather than merely ugly.**
   Forgetting a `break` produces a *correctly compiling, silently wrong* program. This is a
   better argument for the new syntax than "it is more readable" — it is about the class
   of bug you can no longer write. (The book, being older than Java 14, does not have the
   new syntax at all; a reader needs a source that does.)

4. **The book does not tell you that `switch` on a `String` is a hash lookup**, which
   matters when someone writes a 200-case switch over `String` and wonders why it is
   slower than they expected — the `equals` calls on the hash bucket are still real.

## 6. 🔄 What changed in Java 21 (delta vs the book's Java-8 baseline)

| Old (book) | Java 21 | Why it matters |
|---|---|---|
| `switch` with `case X: ... break;` and accidental fall-through | `switch` **expression** with `->` arms and `yield` | fall-through is no longer expressible, so "forgot a `break`" stops being a bug class |
| missing arm silently falls to `default` (or to nothing) | arrow `switch` over an enum/sealed type is **exhaustiveness-checked at compile time** | an unhandled case becomes a *compile error*, so the compiler moves the error earlier (M2) |
| `switch (s) { case "SAT": ... }` with string literals | same syntax, but the compiler emits `hashCode` + `lookupswitch` + `equals` (see §2.4) | dispatch is a hash lookup; a long `String` switch is not free |
| `cond ? a : b` as a statement | same operator, but with a target type it can be used in an assignment context | still the same static-type rules; the traps in §2.3 are unchanged |
| pattern matching for `instanceof` (`obj instanceof String s`) | stable since Java 16 | `if (o instanceof String s && s.length() > 0)` — the binding is in scope, and `&&` is what makes it safe to dereference on the next line |

**What has NOT changed:** `&` and `|` on `boolean` still evaluate both operands; `&&` and
`||` still short-circuit; the ternary operator's type rules in §2.3 are exactly the Java 8
rules. Java 21 did not touch this machinery. **Any code that relies on short-circuit
semantics is portable unchanged** — which is the useful thing to know before you write
`i < n && a[i] != 0` in a judged DSA solution.

## 7. 🔗 Diary link

- [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)
  — short-circuit evaluation is the mechanism behind the *skip the guard* optimisation
  in graph/search notes; the `&&` rule in §2.1 is the same rule that keeps a
  `Heap.index < heap.size && heap.heap[Heap.index] > target` bounds check sound.

## 8. Compiled and run — actual output

```
$ ./java-course/00-toolchain/run.sh \
    java-course/p1-fundamentals/ch03-conditional-statements/lab/Ch03ShortCircuit.java

--- semantics: the guard that prevents a crash ---
data.length = 3
i < data.length && data[i] > 0   -> false
i < data.length & data[i] > 0    -> ArrayIndexOutOfBoundsException  <-- the guard did nothing
  the right operand is EVALUATED for `&`, so the bound check was wasted work
  that still crashed. `&&` is correct here; `&` is not a 'faster &&'.

--- timing: 50,000,000 iterations, guard always false ---
    predicate target read at runtime = -2147483648

  (a) cheap predicate — 1 inner step
    k < 1 && predicate      22.1 ms   (hits 0)
    k < 1 &  predicate      87.8 ms   (hits 0)  4.0x

  (b) costly predicate — 200 inner steps
    k < 1 && predicate      26.1 ms   (hits 0)
    k < 1 &  predicate    6909.2 ms   (hits 0)  264.8x

  -> the ratio tracks the cost of the skipped operand, not the loop.
     '&& is faster than &' is a useless thing to remember;
     'the right side must be safe to skip' is the rule that matters.
```

```
$ ./java-course/00-toolchain/run.sh \
    java-course/p1-fundamentals/ch03-conditional-statements/lab/Ch03ConditionalTypes.java

--- the ternary operator promotes (or NARROWS) both branches ---
  true ? 1 : 2.0      -> 1.0      (Double)
  false ? 'a' : 98   -> code point 98   (Character)
  true ? 1 : 'a'      -> code point 1   (Character)

  even ? 1/2 : 1.0/2   -> 0.0      (Double)
  both arms compute an int-ish thing; 1/2 is 0, and 0 promotes to 0.0.
  no warning. The bug is in the CONSTANT, not the operator.

--- & on booleans: not the slow &&, the one that folds bits ---
  flags = [true, false, true]  ->  mask = 5 = 101
  (mask & 0b111) == 0b111 -> false
  && cannot do this: it yields a boolean and never combines bits.
  &  is the SET operation, so it is the correct tool here.

--- the arrow switch forces you to enumerate or reject ---
  dayType("SAT") = weekend
  dayType("xyz") -> IllegalArgumentException: not a day: xyz
  'xyz' has no arm and no default, so the `default ->` arm throws.
  with the colon/break switch, forgetting the break would instead
  fall through to the next case SILENTLY and return the wrong answer.

--- a String switch is a hashCode switch + equals ---
  "MON".hashCode() = 76524
  "TUE".hashCode() = 83428
  "SAT".hashCode() = 81862
  javap -c shows: invoke hashCode, lookupswitch on the bucket index,
  then .equals for confirmation. It is a HASH LOOKUP, not a chain of ==.
```

```
$ javap -c -p .java-course-build/Ch03ConditionalTypes.class | sed -n '/dayType/,/report/p'
  private static java.lang.String dayType(java.lang.String);
       4: aload_1
       5: invokevirtual #125    // Method java/lang/String.hashCode:()I
       8: lookupswitch  { // 7
                 69885: 160
                 76524: 104
                 81862: 76
```

## 9. What I Got Wrong

- **My first micro-benchmark measured nothing, and it failed loudly.** I wrote the
  predicate as `acc == Integer.MIN_VALUE` over a loop whose trip count came from a
  parameter. With `innerSteps = 0` the JIT proved the whole predicate false, deleted the
  work, and I got the nonsense result `k < 1 & predicate → 2.2 ms (0.1x slower)` — the
  `&` version *faster* than the `&&` version. My own assertion (`expected exactly 2 hits,
  got 0`) caught it. Fix: the target the predicate compares against is now read from
  `args[0]`, so it is not a compile-time constant and cannot be folded.
  **The lesson is bigger than the bug: a benchmark whose answer the JIT can prove is not a
  benchmark.** When a micro-benchmark produces a result you do not believe, the first
  hypothesis is not "the machine is noisy" — it is "the compiler deleted my code".
- **I let the demo crash again.** `dayType("xyz")` threw out of `main` in the first
  version of `Ch03ConditionalTypes.java`, exactly as the locale lab in Ch 02 threw
  `InputMismatchException`. I had already written that lesson down and then repeated it in
  the very next chapter. A lab whose job is to *demonstrate* failure has to catch the
  failure and report it — otherwise the reader only sees a stack trace.
- **I "discovered" `true ? 1 : 'a'` by accident.** I wrote the line to demo promotion, and
  the output was a blank space. I assumed a formatting bug and nearly moved on. It was
  `Character(1)` — code point 1, unprintable. **If a program prints something invisible,
  the value is more interesting than the format string.** The lab now prints code points for
  `char` instead of `%c`.
- **I mis-typed my own helper signature.** `report(String, Supplier<String>)` was called
  with `dayType("SAT")` — already evaluated, so the exception was thrown *before* `report`
  could catch it. Passing `() -> dayType("SAT")` moved the throw inside the lambda. This is
  the same class of mistake as passing a value where you meant a computation: the
  eagerness of Java's argument passing defeats the intent of the signature.
- **I wrote "`tableswitch`" in the card before checking.** The actual instruction for
  seven spread-out hash buckets is a **`lookupswitch`**. Small error, but it is exactly the
  kind of thing that gets copied into six later chapters, so the card was corrected to
  paste the real `javap` output instead of a paraphrase.

## 10. Open questions (to measure, not to guess)

- [ ] How expensive is the `equals` confirmation after the `lookupswitch`? Build a
      `switch` over 200 `String` cases with deliberately colliding `hashCode` values and
      measure how much worse the dispatch gets. (This is a real attack surface: an attacker
      who controls the switched-on string controls the bucket.)
- [ ] `switch` on an `enum` — does it compile to `ordinal()` + `tableswitch`, and what
      happens if a `null` enum reaches it? (Prediction: NPE, but *where*?)
- [ ] Does the JIT's ability to fold a provably-false condition mean that in a real judged
      solution, `if (falseCondition && expensive())` costs literally zero? Measure with a
      `volatile` flag the compiler must re-read.
- [ ] Rewrite `Ch03ShortCircuit` using `Integer.bitCount` and `&` masks, and check whether
      the `boolean[] flags` bit-folding version in §3 is actually faster than a plain
      second loop. If it is not, the counter-example is only a type-safety argument.

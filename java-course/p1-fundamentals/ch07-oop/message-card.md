# Ch 07 — Object-Oriented Programming

> **Claim of the chapter:** polymorphism is not "calling a method on an object". It is a
> **two-step decision about which code runs**, and the first step is made at *compile
> time* from the type you wrote. Everything in this chapter — the dispatch cost, the
> `equals` contract, field hiding, why `static` cannot be virtual — is that one
> distinction, unpacked.

**Status:** 🟢 green — every number and every output below was produced by the four lab
files in `lab/`
**Messages:** M2 (types move errors earlier), M1 (every line has a cost), M5 (abstraction
is a ladder), M6 (you do not know it is right until a test says so)
**Labs:** `Ch07EqualsHashCode.java`, `Ch07Polymorphism.java` (card evidence),
`Ch07OpenAddressing.java`, `Ch07SentinelFix.java` (exercises L3)
**Diary:** [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

---

## 1. The claim (derived, not recalled)

The book introduces a class as "a template that contains data and methods" and shows
`super`, `this`, and an abstract class. The missing idea is the one that makes the whole
chapter hang together:

> A Java call has **two** decisions, made at **two different times**, from **two different
> pieces of information**.
>
> - Which *method signature* exists → decided at **compile time** from the **static type**
>   (the type of the expression you wrote).
> - Which *body* executes → decided at **run time** from the object's **dynamic type**.
>
> Virtual dispatch is the second decision. There is no such thing as a virtual `static`
> method, and that is not a limitation of Java — it is a consequence of the split.

Everything else in this chapter is a consequence:

| Observation | Which decision it is |
|---|---|
| `s.area()` runs the subclass body | dynamic |
| `Shape.describe()` vs `Circle.describe()` | **static** — resolved by the call site |
| `s.PREFIX` vs `c.PREFIX` are different fields | **static** — fields are not inherited, they are shadowed |
| `ev.equals(car)` ≠ `car.equals(ev)` | dynamic, so the contract is a *runtime* obligation |
| `HashSet` needs both `hashCode` and `equals` | dynamic, and `hashCode` is called *before* dispatch happens at all |

## 2. Evidence

### 2.1 One call site, three runtime types

`Ch07Polymorphism.java`, `oneLoopManyTypes()`:

```java
List<Shape> shapes = List.of(new Circle(2), new Square(3), new Rect(2, 5));
double total = 0;
for (Shape s : shapes) {
    total += s.area();
}
```

Actual output:

```
  Circle   area() =  12.57   (the loop does not know which one)
  Square   area() =   9.00   (the loop does not know which one)
  Rect     area() =  10.00   (the loop does not know which one)
  total = 31.57
```

The loop body is one line. The alternative is an `if (s instanceof Circle) ... else if
(s instanceof Square) ...` chain, which is `O(types)` instead of `O(1)` per element and
must be edited every time a shape is added. That edit is the actual cost polymorphism
removes — not the nanoseconds in §2.5.

### 2.2 Static methods and fields ignore the object entirely

Same object, two different results, decided by the *reference's* type:

```
  Shape s = Circle;   Shape.describe()    -> Shape.describe (static)
  Circle c = Circle; Circle.describe()   -> Circle.describe (static, NOT an override)
  the object is a Circle in both lines; the CALL SITE type decided.
  the Circle object, reached through a Shape ref: s.describe() -> Shape.describe (static)
  (javac warns: 'static method should be qualified by type name, Shape,
   instead of by an expression'. The warning is correct and the fix is
   cosmetic — the result is the same either way, which is the point.)
```

Fields behave the same way, and this one is a real trap:

```
  Circle.PREFIX via Circle-typed ref = Circle
  Circle.PREFIX via Shape-typed  ref = Shape
```

`Circle` declares its own `PREFIX`, which *hides* `Shape.PREFIX`. These are two unrelated
fields holding two unrelated values. Nothing is overridden, because a field is not a
behaviour.

The **hiding itself is completely silent** — no error, no warning. `javac` does print two
warnings here, but they are about a different mistake (`[static] static variable should
be qualified by type name, Shape, instead of by an expression`, for reading a static
through a variable rather than through the type). A compiler that flags the *access
style* is silent about the *shadowed field*, which is the thing that costs you an hour.

### 2.2b A `static` "override" — what the compiler actually does

Compiling `@Override static String who()` on a method that hides a `static` one:

```
error: static methods cannot be annotated with @Override
class Sub extends Base { @Override static String who() { return "Sub"; } }
                         ^
```

Remove the annotation and it compiles as **hiding**:

```
Sub2.who()   = Sub
Base2.who()  = Base
b.who()      = Base   <- object is a Sub2
```

`b` is declared `Base2` and holds a `Sub2`, yet the parent's body runs. A `static`
method cannot be virtual because the object plays no part in the decision — the
reference's *compile-time* type is the only input.

### 2.3 `equals` and `hashCode` are a contract, and it is checkable

`Ch07EqualsHashCode.java`, `caseInsensitiveOnlyEquals()` — a class that overrides
`equals` and forgets `hashCode`:

```
  HashSet.size() = 2   (two equals objects, so the answer is 1)
  HashMap.get(equal key) = null
  -> the get returned null. The put and the get landed in different
     buckets, so the map never even compared them.
  -> add the same object twice:
     same instance added twice -> size = 1   (works! equal to itself)
```

Two things to take from this:

1. `javac` **does** warn here (`[overrides] Class CaseInsensitive overrides equals, but
   neither it nor any superclass overrides hashCode method`). It catches this one.
2. The test that catches it is *two objects built separately*. Adding the same instance
   twice always works, because identity is trivially consistent. A unit test that builds
   one fixture and reuses it passes, and production parses — which is why this ships.

The opposite error is **not** caught. `PtBadHash` compares `x` but hashes `y`:

```
    p1.equals(p2) = true (x matches)
    p1.hashCode() = 2, p2.hashCode() = 99 (y differs)
    set.size() = 2   <- 2 EQUAL objects, and the set kept both
    this is a contract VIOLATION, not a design choice
```

Two objects that are `equal` are both kept, because they hash to different buckets.
`javac` is silent: `hashCode` *is* overridden, it just disagrees with `equals`.

### 2.4 A broken `hashCode` *hides* a broken `equals`

`CaseInsensitive.equals` accepts any `CharSequence`, so it is asymmetric:

```
  java.equals(plain) = true   (we control this side)
  plain.equals(java) = false  (String.equals, and it says false)
```

Two lookups, both returning `null`, for **two different reasons**:

```
  map.put(java);   map.get(plain)   = null
    reason: HashMap asks key.equals(stored), key = plain (String),
    so String.equals says false. The asymmetry is visible here.

  map2.put(plain); map2.get(java)   = null
    reason: DIFFERENT and more instructive. Here key = java, so
    CaseInsensitive.equals(plain) would say TRUE — but the two never
    met, because CaseInsensitive does not override hashCode, so the
    two keys hash into different buckets and equals is never called.
```

`HashMap.get(key)` calls `key.equals(stored)`. In the first map the search key is a
`String`, so the asymmetry shows. In the second map the search key is a
`CaseInsensitive`, whose `equals` *would* return `true` — but the two keys were never in
the same bucket, so `equals` was never called. Fix `hashCode` first and the asymmetry
becomes the visible failure. **The two bugs mask each other**, which is exactly why this
class of bug survives to production.

### 2.5 The dispatch cost is ~3 ns, and my first measurement of it was wrong

`Ch07Polymorphism.java`, `dispatchIsNotFree()`. Four types with byte-identical bodies
(`return k * 2.0 + 1.0;`), one array all-`WorkA` and one array with all four types, both
call sites warmed before timing, and the order of measurement stated and reversed:

```
  measuring the POLYMORPHIC loop first, so it cannot be blamed on cold code:
    monomorphic       49.0 ms
    polymorphic     250.2 ms   5.11x

  now the MONOMORPHIC loop first:
    monomorphic       49.0 ms
    polymorphic     267.1 ms   4.75x

  best-of-2: mono 49.0 ms, poly 250.2 ms -> 5.11x
  that is 4.02 ns per call
```

(one run; the five-run spread is in the table below)

A call site that has only ever seen one receiver type can be inlined and the whole
dispatch disappears — which is why the monomorphic loop is ~5× faster. A site that sees
four types is *megamorphic*, the JIT gives up on inlining, and each call costs a real
lookup.

Five full runs of the lab, each reporting best-of-2 in both orders:

| run | mono | poly | ratio | ns/call |
|---|---|---|---|---|
| 1 | 37.8 ms | 191.5 ms | 5.06× | 3.07 |
| 2 | 38.3 ms | 205.5 ms | 5.37× | 3.35 |
| 3 | 38.1 ms | 195.6 ms | 5.14× | 3.15 |
| 4 | 41.2 ms | 188.8 ms | 4.58× | 2.95 |
| 5 | 42.6 ms | 239.9 ms | 5.64× | 3.95 |

The **ratio** is the stable quantity (4.6–5.6×); the absolute ns/call is noisier
(2.9–4.0 ns) because it depends on the absolute speeds of both loops. I quote
**~3.3 ns per call** as the central value and the ratio as the claim that would survive
a different machine. Single-run numbers are reported in §8 because that is the lab's raw
output, but they are not the finding.

So the honest statement is neither "polymorphism is free" nor "abstraction has a big
tax". It is: **a few nanoseconds per call, in exchange for deleting an `O(types)`
if-else chain.** For a virtual call in a hot loop that is a trade worth making; for one
in a request handler, the ~3 ns is irrelevant and the if-else chain is what you should
delete on readability grounds alone.

### 2.6 "Just return a constant from `hashCode`" is legal, correct, and quadratic

`Ch07EqualsHashCode.java`, `constantHashIsCorrectAndQuadratic()`. Two classes with a
**byte-identical `equals`**, differing only in `hashCode` — one `return 0;`, one
`return v;`. Both maps are built with `n` keys and then queried with all `n` keys, and
the lab `throw`s if a single key is lost, so *correctness is not in question*:

```
             n const-hash total      ns/lookup   real-hash total
          1000          5.43 ms         5433.3      0.20 ms
          2000          8.28 ms         4141.7      0.19 ms
          4000         31.81 ms         7953.2      0.27 ms
          8000        145.72 ms        18214.7      0.50 ms
         16000        922.28 ms        57642.4      1.04 ms

  at n=16,000 the total differs by 890x, and the constant-hash map needs
  57.6 microseconds for ONE lookup.
```

Every key hashes to `0`, so the whole table is one bucket — a linked list — and a lookup
costs `O(n)` `equals` calls. `n` lookups cost `O(n²)`. The ratios between consecutive
rows (1.5×, 3.8×, 4.6×, 6.3× for a 2× increase in `n`) are drifting towards 4×, which is
the signature of the quadratic term, not of any constant factor.

This is the failure mode worth remembering, because it is the *attractive* fix: when a
`HashSet` does not deduplicate, `return 0;` makes the test pass on the next run, compiles
cleanly, and has no warning attached. It converts a correctness bug into a performance
cliff that only shows up under load.

## 3. Counter-example — where the intuition breaks

The naive model is "a method call is one decision: which method do I run?" Every
surprise above is the model being incomplete. The sharpest counter-example is the
subclass that breaks its parent's `equals`:

```
  two Cars, same model: car.equals(car2) = true
    ev.equals(car)  = false   (the Car is not an ElectricCar)
    car.equals(ev)  = true    (an ElectricCar IS a Vehicle)
  -> ASYMMETRIC. ElectricCar.equals added a `instanceof ElectricCar`
     check, and that check has no counterpart in Vehicle.equals.
```

`Vehicle.equals` is perfectly correct in isolation. `ElectricCar.equals` is perfectly
correct in isolation. Together they violate the contract, and **no compiler can catch
it** — the obligation is on the pair. This is the chapter's one real lesson: the
contract is not a method you implement, it is a property a *family* of classes must
keep.

The two legal repairs are different designs, not different qualities of code:

| Repair | `car.equals(ev)` | `ev.equals(car)` | `HashSet{Car, ElectricCar}` |
|---|---|---|---|
| base uses `getClass()` | `false` | `false` | 2 — a different vehicle |
| base uses `instanceof`, subclass matches the base test | `true` | `true` | 1 — the same vehicle |
| (do nothing) | `true` | `false` | **broken** |

## 4. Anti-message

| The trap | What actually happens | Repro |
|---|---|---|
| "If I override `equals`, the set will dedupe my objects" | The set uses `hashCode` first. Objects with different hashes are never compared, so a correct `equals` does nothing | `Ch07EqualsHashCode.java` → `set.size() = 2` |
| "I'll fix the failing test by overriding `hashCode` to return a constant" | Legal, correct, and it makes every lookup `O(n)`: **890× slower** at n = 16,000, with no warning | §2.6 |
| "A `static` method can be overridden if the subclass declares the same name" | `@Override` on a method that hides a `static` one is a **compile error** (`static methods cannot be annotated with @Override`). Remove the annotation and it is *legal hiding*, and the parent's body still runs through a parent-typed reference | `Ch07Polymorphism.java` → `staticIsNotPolymorphic()`; `s.describe()` prints `Shape.describe` while the object is a `Circle` |
| "Interfaces can't have any implementation" | False since Java 8 (`default`) and Java 9 (`private`). What an interface still cannot have is **instance state** | `Ch07Polymorphism.java` → `whatAnInterfaceCannotDo()` |
| "Abstract class and interface are two syntaxes for the same thing" | They split on **state**. An abstract class can hold a field and chain constructors; an interface cannot | reflection field count in the same method |

## 5. What the book does not say out loud

1. **The dispatch cost is bimodal, not gradual.** Either a call site has one receiver
   type and costs ~0, or it has several and costs a few ns. There is no smooth middle
   worth thinking about. The consequence is that "make it polymorphic to make it fast" is
   backwards, and that a call site's *profile* is part of its design.
2. **Fixing a `hashCode` bug can make a different bug appear.** §2.4 — the asymmetry was
   invisible *because* `hashCode` was also broken. Repairing bugs in a fixed order can
   change which failure you see, so "it was passing" is not the same as "it was right".
3. **`equals` is a domain decision with a performance consequence.** Whether a `Car` and
   an `ElectricCar` with the same model are equal is a question about the *domain*, and
   the answer changes the size of a `HashSet`. It is not a technicality that can be
   deferred to a code reviewer.
4. **Interface default methods are the real reason Java 8 lambdas were possible.** An
   interface with a single abstract method is a function type, and adding `default` made
   it possible to *grow* an interface without breaking every implementor. That is a
   language-design trade, not a syntax feature.

## 6. 🔄 What changed in Java 21 (delta vs the book's Java-8 baseline)

| Book | Java 21 | Why it matters here |
|---|---|---|
| "An interface contains only abstract methods" | `default` (Java 8), `static` and **`private`** (Java 9) methods | An interface can now hold shared helper code, so "no state" is the *only* remaining real restriction. §2.4's `prefix()` demo |
| `instanceof Type x` needs a cast after the test | `if (o instanceof CaseInsensitive c)` binds `c` in scope (Java 16) | Removes the classic "test then cast, twice" bug; the pattern variable is checked by the compiler, so the cast cannot be forgotten |
| `switch` on `String`/`enum`, arrow syntax | Same, plus pattern matching in `switch` (Java 21, final) | `equals` and `hashCode` are the last places where a `switch` on an object would be nicer than a chain of `instanceof` — still not available, which is *why* these two methods are still hand-written everywhere |
| Subclassing for code reuse | Records, sealed types, `sealed`/`permits` (Java 17) | `sealed` makes the set of subclasses **closed and checked**. §3's "add a new shape, forget to update the chain" failure is a *compile error* instead of a silent bug |
| No guidance on `hashCode`/`equals` | Records generate a consistent pair | The compiler now guarantees what §2.3 shows you can get wrong by hand — but only if you let it |

**What the book got right, for a reason I now understand:** it tells you to always call
`super.equals(o)` and `super.hashCode()` in an override. At the time that read as
superstitious politeness. §3 is the reason: your override is a *replacement*, and
`super` is the only reference to the contract the parent was satisfying. Skipping it is
not a style violation, it is a broken contract.

## 7. 🔗 Diary link

- [reaserching-diary → dev_foundation/dsa/01-learning-roadmap.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/01-learning-roadmap.md)

## 8. Compiled and run — actual output

```
==> javac java-course/p1-fundamentals/ch07-oop/lab/Ch07EqualsHashCode.java
Ch07EqualsHashCode.java:123: warning: [overrides] Class CaseInsensitive overrides
  equals, but neither it nor any superclass overrides hashCode method
1 warning
==> java Ch07EqualsHashCode
```

```
--- a String subclass that overrides equals but not hashCode ---
  HashSet.size() = 2   (two equals objects, so the answer is 1)
  HashMap.get(equal key) = null
  -> the get returned null. The put and the get landed in different
     buckets, so the map never even compared them.
  -> add the same object twice:
     same instance added twice -> size = 1   (works! equal to itself)
  -> so a test using one object passes, and a test using two passes,
     while real data built by parsing fails. That is why this bug ships.

--- an asymmetric equals, and a broken hashCode that HIDES it ---
  java.equals(plain) = true   (we control this side)
  plain.equals(java) = false   (String.equals, and it says false)
  map.put(java);   map.get(plain)   = null
  map2.put(plain); map2.get(java)   = null

--- instanceof-based equals, and what a subclass breaks ---
  two Cars, same model: car.equals(car2) = true
  two ElectricCars, same model+battery: equals = true
    ev.equals(car)  = false   (the Car is not an ElectricCar)
    car.equals(ev)  = true    (an ElectricCar IS a Vehicle)
  -> ASYMMETRIC. ElectricCar.equals added a `instanceof ElectricCar`
     check, and that check has no counterpart in Vehicle.equals.
  HashSet of two equal ElectricCars: size = 1   (dedup works)
  HashSet of {Car, ElectricCar} with the same model: size = 2

  records get equals+hashCode from the compiler, consistently.
    p1.equals(p2) = true (x matches)
    p1.hashCode() = 2, p2.hashCode() = 99 (y differs)
    set.size() = 2   <- 2 EQUAL objects, and the set kept both

--- 'just return a constant' fixes dedup and wrecks the map ---
  IDENTICAL equals() in both classes. The only difference is hashCode().

             n const-hash total      ns/lookup   real-hash total
          1000          5.43 ms         5433.3      0.20 ms
          2000          8.28 ms         4141.7      0.19 ms
          4000         31.81 ms         7953.2      0.27 ms
          8000        145.72 ms        18214.7      0.50 ms
         16000        922.28 ms        57642.4      1.04 ms

  at n=16,000 the total differs by 890x, and the constant-hash map needs
  57.6 microseconds for ONE lookup.
```

```
==> javac java-course/p1-fundamentals/ch07-oop/lab/Ch07Polymorphism.java
Ch07Polymorphism.java:56: warning: [static] static method should be qualified by
  type name, Shape, instead of by an expression
Ch07Polymorphism.java:66: warning: [static] static variable should be qualified by
  type name, Circle, instead of by an expression
Ch07Polymorphism.java:67: warning: [static] static variable should be qualified by
  type name, Shape, instead of by an expression
3 warnings
==> java Ch07Polymorphism
```

```
--- one call site, three runtime types ---
  Circle   area() =  12.57   (the loop does not know which one)
  Square   area() =   9.00   (the loop does not know which one)
  Rect     area() =  10.00   (the loop does not know which one)
  total = 31.57

--- static methods and fields are chosen at compile time ---
  Shape s = Circle;   Shape.describe()    -> Shape.describe (static)
  Circle c = Circle; Circle.describe()   -> Circle.describe (static, NOT an override)
  the object is a Circle in both lines; the CALL SITE type decided.
  the Circle object, reached through a Shape ref: s.describe() -> Shape.describe (static)
  (javac warns: 'static method should be qualified by type name, Shape,
   instead of by an expression'. The warning is correct and the fix is
   cosmetic — the result is the same either way, which is the point.)
  writing @Override on a static method that hides a static one is an
  ERROR ('static methods cannot be annotated with @Override'); dropping
  the annotation makes it legal hiding, not overriding.

  field hiding, which is the same mistake with different syntax:
    Circle.PREFIX via Circle-typed ref = Circle
    Circle.PREFIX via Shape-typed  ref = Shape
  same object, two different fields, chosen by the ref type.
  (Circle declares its own PREFIX, HIDING Shape's. The hiding
   itself is completely silent — no error, no warning. The two
   warnings javac DOES print are unrelated: they are about reading
   a static through an expression instead of via the type name.)

--- the interface/abstract-class split is a STATE split, not a syntax one ---
  Circle has 1 instance fields, an interface cannot have any.
  interface: only public abstract (or default/static/private) methods.
  abstract class: may also have state, constructors, and package-private
  methods, which is what lets it enforce an invariant across subclasses.
  and the modern rule of thumb, measured by what each one can hold:
    need shared STATE / constructor chaining  -> abstract class
    need only a CAPABILITY set               -> interface
  private interface method, called from a default method: shape shape
  that is how you share code across implementors without exposing it,
  and it only became possible in Java 9.

--- how much does a virtual call cost? ---
  METHOD NOTE: the first attempt at this benchmark used Shape, where
  Triangle.area() calls Math.sqrt and Circle.area() does two
  multiplies. It reported ~5.8x, and I wrote that down as 'the cost of
  dispatch'. That was wrong: the shapes were not doing the same work,
  so the measurement was comparing a square root to a multiply.
  Below, the 4 types do IDENTICAL work, so the only variable left is
  WHICH method runs.
  measuring the POLYMORPHIC loop first, so it cannot be blamed on cold code:
    monomorphic       49.0 ms
    polymorphic     250.2 ms   5.11x
  now the MONOMORPHIC loop first:
    monomorphic       49.0 ms
    polymorphic     267.1 ms   4.75x
  best-of-2: mono 49.0 ms, poly 250.2 ms -> 5.11x
  that is 4.02 ns per call, on a loop that also does an add and a store
  -> a few NANOSECONDS. The loop is still O(n). That is the entire
     argument for OOP: nanoseconds to delete an O(types) if-else chain.

(Reproduced 5 times: ratio 4.58x-5.64x, ns/call 2.95-3.95. The single run
 above is the lab's raw output; the spread is the real evidence.)

## 9. What I Got Wrong

1. **I predicted `HashSet.size() = 1` and the run printed `2`.** I wrote the expected
   output into the lab *before* running it, based on the assumption that a correct
   `equals` is enough. It is not — `hashCode` is consulted first and never overridden.
   Lesson: write the prediction, run it, then the *difference* is the lesson. I left the
   wrong prediction in the code next to the right answer.
2. **My first asymmetry demo demonstrated nothing.** `equals` tested
   `other instanceof CaseInsensitive`, and a `String` is not a `CaseInsensitive`, so
   both directions returned `false` — symmetric, and useless as a demo. The section was
   titled "equals is not symmetric" and proved the opposite. Fixed by testing
   `instanceof CharSequence`, which is the mistake the section is about.
3. **My `PtBad` record was not bad.** I wrote `hashCode() { return x; }` while
   commenting "hashing y" — so the two objects had the same hash, dedup worked, size
   was `1`, and the demo showed a *correct* implementation. The bug appeared only after
   changing it to `return y`. Reading my own comment instead of my own code is the
   failure mode here.
4. **I wrote "both lookups succeed" when both printed `null`.** I had reasoned that
   `HashMap` calls `key.equals(stored)`, which is true, and then written a conclusion
   that contradicted the output I had just seen. The actual explanation is more
   interesting and is now what the lab says: the two `null`s have *different* causes,
   and the second one is hidden by the `hashCode` bug.
5. **My dispatch benchmark compared a `Math.sqrt` to a multiply.** `Shape` subclasses
   did different amounts of work, so the ~5.8× I measured was not dispatch cost. I
   attributed the whole difference to polymorphism and computed "3.71 ns per call" from
   it. The fix was to make all four `Work` classes byte-identical; the honest number is
   **~3.3 ns** (range 2.9–4.0 over five runs) on a call site with 4 receiver types. The
   number barely moved, which is how the error could survive — a wrong reason can produce
   a right-looking number.
6. **I ran the benchmark in a fixed order and nearly blamed the JIT.** Whichever loop
   runs first pays for interpretation. The lab now warms both call sites, measures in
   both orders, and prints the best-of-2.
7. **I claimed "getClass() says no" in a comment above code that used `instanceof`.**
   The comment described the textbook version of the example; the code was the
   `instanceof` version. The `Car`/`ElectricCar` result is genuinely asymmetric, but for
   the reason §3 gives, not the one the comment claimed.
8. **I asserted the `static`-override rule from memory and got it half wrong.** I wrote
   "Java forbids it: *cannot override* / *overrides static*". Actually compiling it:
   `@Override` on a static method is an error, but dropping the annotation makes it
   perfectly legal *hiding*, and the parent's body still runs through a parent-typed
   reference. The corrected claim is in §4's table.
9. **I asserted the constant-`hashCode` trap from memory, then wrote the measured
   numbers into the code by hand.** The lab now derives the ratio from the timings it
   actually took. The claim survived the check, but only because I measured it — the
   first version of the line had `998.84` and `1.00` typed in as literals, which is the
   same class of error as #5: a number that looks measured but is not.
10. **The quadratic signature is weaker evidence than it looks.** The per-row ratios in
    §2.6 are 1.5×, 3.8×, 4.6×, 6.3× for a 2× increase in `n` — noisy, and the low-end
    rows are dominated by cache and JIT warmup rather than by the asymptote. I claim
    `O(n²)` because the mechanism is known (one bucket, one list), not because the first
    four rows prove it. A longer series would be needed to see it cleanly.

## 10. Open questions (to measure, not to guess)

- [ ] At what receiver count does a call site become megamorphic, and is the threshold
      exactly 2, 3 or 4 on this JVM? Measure with `WorkA` plus one extra type added
      incrementally, and check whether the 2.9–4.0 ns figure is a step function or a ramp.
- [ ] `String.hashCode()` caches its value in a field; does an `int`/`double`-keyed
      record or a plain class without a cache behave differently under the same load?
- [ ] Does `equals` asymmetry *ever* surface as a `HashSet` bug (as opposed to a
      `HashMap` lookup returning `null`)? Construct a same-bucket case.
- [ ] Is the "constant `hashCode` → `O(n)` lookup" claim measurable at realistic `n`?
      Time `HashMap` with `return 0;` vs a real hash at n = 1e3, 1e4, 1e5.
- [ ] What does `sealed` + pattern-matching `switch` remove from §3's failure mode —
      can a sealed hierarchy now generate a correct `equals` for free?

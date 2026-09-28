# Ch 04 — Functions, Characters, and Strings — Exercises

> Book: *Introduction to Java Programming and Data Structures, Comprehensive Version*
> (Y. Daniel Liang) — Chapter 4, "Functions, Characters, and Strings".
>
> **Boundary rule:** book exercises stay here. Nothing from this chapter has been
> submitted to a judge, so nothing is promoted to `solutions/`. The **L3 tier carries
> the full Section D analysis inline**.

---

## L1 — Recall (close the book)

1. Why can a method take an array parameter and "modify" it, but cannot modify a `String`
   parameter the same way?
2. What does `==` compare for two `String`s? What does it compare for two `int`s?
3. Why are string *literals* equal by `==` in the same program, most of the time?
4. What does `String.hashCode()` compute, and what is it used for?
5. `StringBuilder` vs `StringBuffer` — what is the difference, and which one is safe in a
   multithreaded program? (Chapter 4 introduces the single-threaded one only.)
6. Why is `char` a 16-bit type, and what breaks in a `char`-based loop over an emoji?

## L2 — Apply (predict, run, then compare)

**E2.1 — Write down your predictions FIRST.** For each, write the predicted output and
*the reason*, then run:

```java
String a = "ab";
System.out.println(a == "a" + "b");        // 1
System.out.println(a == new String("ab")); // 2
System.out.println(a.equals(new String("ab")));
System.out.println(a.hashCode() == new String("ab").hashCode());
System.out.println(a == new StringBuilder("ab").toString());
```

Which of the five is `true` for a reason that does **not** generalise? That is the one to
remember.

**E2.2 — The collision hunt.** Write a loop that finds *all* pairs of distinct two-character
ASCII strings with the same `hashCode`, and print the first 10. Then extend it to
three-character strings. Report how many distinct 3-char strings collide in total
(there are 128³ ≈ 2.1M of them if you use `char`, but restrict to lowercase letters:
26³ = 17,576, and you can build the whole table in memory). How many 3-letter lowercase
words from a dictionary collide with each other?

**E2.3 — Prove the quadratic by counting, not by timing.** In a loop that appends `n`
characters with `+=`, count the total number of character copies by using a subclass of
`StringBuilder`… except you cannot subclass it usefully because `String` is final. So
instead: instrument with a counter inside a helper that mimics the copy, or simply *prove*
it by summing $1 + 2 + \dots + n$ for $n = 10, 100, 1000$ and check the ratio between
successive values. Then time it for $n = 10^4, 10^5$ and show the time roughly
**quadruples** each time n is multiplied by 10. That is the fingerprint of $O(n^2)$ and
it is the only evidence you actually need.

**E2.4 — `String` as a `switch` selector with a `null`.** What happens, and at which
line? Find the answer from the Ch 03 bytecode (`hashCode()` is the first call) and then
confirm by running it inside a `try`. (This is a M4 boundary case, not a syntax question.)

**E2.5 — Fix a real bug.** This compiles and prints the wrong thing. Find out why, then
fix it *and* explain why `s = s + "b"` would have the same problem:

```java
String s = "a";
s += "b";
if (s == "ab") {
    System.out.println("found");
}
```

## L3 — Derive (full Section D analysis)

### L3.1 — LeetCode 28 · Find the Index of the First Occurrence in a String

**Problem, restated in one sentence.** Given `haystack` and `needle`, return the smallest
index at which `needle` occurs inside `haystack`, or `-1` if it does not occur.

**Constraints, quoted.** `1 ≤ haystack.length, needle.length ≤ 10^4`;
`0 ≤ needle.length ≤ haystack.length` (newer versions); the characters are lowercase
letters (newer versions) or arbitrary characters (the original version).

Both constraint sets matter. `needle.length ≤ haystack.length` immediately removes the
"needle longer than haystack" case, which would otherwise need a separate answer. The
"lowercase letters only" version is what makes the 26-symbol alphabet legal, and that
matters for the improvement.

**Brute force, and why it is too slow.**

```java
int strStr(String haystack, String needle) {
    int n = haystack.length(), m = needle.length();
    for (int i = 0; i + m <= n; i++) {
        boolean match = true;
        for (int j = 0; j < m; j++) {
            if (haystack.charAt(i + j) != needle.charAt(j)) { match = false; break; }
        }
        if (match) return i;
    }
    return -1;
}
```

Time $O(nm)$, space $O(1)$. At $n = m = 10^4$ that is $10^8$ character comparisons —
roughly a second, and a `TLE` on most judges. **Why it is too slow is not "it's nested":
it is that the inner scan restarts from scratch at every position, so the same characters
are compared again and again.** The overlap is the waste.

**The improvement, derived from the failure pattern.** Ask: *when a comparison fails at
position $j$, what do we already know?*

We know `needle[0..j-1]` matched `haystack[i..i+j-1]`. Throwing that away is the waste.
But we know more: the matched prefix of `needle` is also a *prefix* that might appear
inside `needle` itself. So if we precompute, for every prefix length of `needle`, the
length of the longest proper prefix that is also a suffix, then after a mismatch at $j$ we
do not restart at $i+1$ — we restart at $i + \pi[j-1]$ and keep the $\pi[j-1]$ characters
already matched.

That is KMP. The precomputation is $O(m)$ and the search is $O(n+m)$ because the
**failure function** is exactly what stops the restart from going backwards.

**Correctness argument.** With $\pi[k]$ = length of the longest proper prefix of
`needle[0..k]` that is also a suffix of `needle[0..k]`:

> **Invariant.** At the top of each iteration of the search loop, every index in
> `[i - j, i)` holds `haystack[t] == needle[t - i + j]` for the current match length $j$ —
> that is, the last $j$ characters of `haystack[0..i)` equal `needle[0..j)`.

- *Base case.* $j = 0$, nothing to match. Holds.
- *Maintenance.* If `haystack[i] == needle[j]`, then $j \leftarrow j+1$ and the invariant
  extends by the one character just matched. If they differ, we set $j \leftarrow \pi[j-1]$.
  The new $j$ is the largest value such that `needle[0..j)` is a suffix of what we have
  already matched — and because the invariant says what we have matched *is*
  `needle[0..j_old)`, the corresponding characters in `haystack` are also there. So the
  invariant is preserved. Crucially $j$ strictly decreases on a mismatch, so $i$ never
  has to move backwards.
- *Termination.* Each iteration increases $i$ by exactly 1. When $j = m$ we return $i - m$
  and it is the first such index because $i$ only increases and we check every $i$. If
  $i$ reaches $n$ with $j < m$, no occurrence exists, and $-1$ is correct. ∎

**Complexity, with the dominant term.**

| | time | space |
|---|---|---|
| brute force | $O(nm)$ | $O(1)$ |
| KMP | $O(n + m)$ — the failure function is $O(m)$, the search is $O(n)$ because $i$ is monotone | $O(m)$ for the $\pi$ array |

The space is a genuine trade: KMP **buys** time with memory, unlike binary search which
bought both. That is a different kind of improvement, and it is worth naming as such.
(Note: the naive/prefix-function distinction also appears in the book as "string matching",
without the complexity derivation.)

**The table was checked, not asserted.** All 10 rows were run against the brute force, and
then 200,000 random pairs were fuzzed with the same two implementations:

```
h="a"            n="a"      kmp=0   brute=0   ok
h="a"            n="aaaa"   kmp=-1  brute=-1  ok
h="mississippi"  n="issip"  kmp=4   brute=4   ok
h="mississippi"  n="issi"   kmp=1   brute=1   ok
h="aaa"          n="aaaa"   kmp=-1  brute=-1  ok
h="aaaaa"        n="bba"    kmp=-1  brute=-1  ok
h="abababc"      n="abab"   kmp=0   brute=0   ok
h="abababc"      n="abc"    kmp=4   brute=4   ok
h=""             n=""       kmp=0   brute=0   ok
h=""             n="a"      kmp=-1  brute=-1  ok
table mismatches + fuzz: 0
```

**Zero mismatches over 200,000 random pairs.** The `issi` row is where the verification
earned its keep: I wrote `4` in the first draft and the run said `1`. A hand-written table
that is never executed is a table of my beliefs, not of the program's behaviour.

**Test table.** Written by hand, then executed against the brute force.

| # | `haystack` | `needle` | expected | why it is interesting |
|---|---|---|---|---|
| 1 | `"a"` | `"a"` | `0` | both at the stated minimum length |
| 2 | `"a"` | `"aaaa"` | `-1` | **breaks** `needle.length ≤ haystack.length` — a longer needle must return `-1`, not crash |
| 3 | `"mississippi"` | `"issip"` | `4` | the classic overlapping case; a naive "skip to i+1" fix fails here |
| 4 | `"mississippi"` | `"issi"` | `1` | **`1`, not `4`.** I wrote `4` in the first draft of this table; the verified run says `1`, because `issi` occurs at index 1 (`i`,`s`,`s`,`i`) and the later `issi` at index 4 is a *second* occurrence, not the first. This is exactly the "smallest index" requirement, and my draft failed it. |
| 5 | `"aaa"` | `"aaaa"` | `-1` | pure overlap, the case the failure function exists for |
| 6 | `"aaaaa"` | `"bba"` | `-1` | no match at all, forces the full search |
| 7 | `"abababc"` | `"abab"` | `0` | match at the very start |
| 8 | `"abababc"` | `"abc"` | `4` | match at the very end, the loop must terminate on the last index |
| 9 | `""` | `""` | `0` | both empty (below the stated bounds) — **both implementations of the invariant must be checked for this** |
| 10 | `""` | `"a"` | `-1` | empty haystack, non-empty needle |

**What I Got Wrong (before running).** My first instinct was to return `i` when the match
completes, not `i - m`. In the inner loop of the brute force, $i$ is the *start*, but in
KMP's loop $i$ is the *current scan position* and the match ends one past it. The two
implementations disagree about what `i` means, which is exactly the kind of bug that a
"looks right on the examples" test misses. **Same variable name, different meaning across
two algorithms is a silent-WA generator.**

**Open questions.**
- [ ] Extend to `haystack` of length $10^5$ and `needle` of length $10^5$ — at what
      length does the brute force exceed one second, and does KMP stay under the limit?
- [ ] Rewrite the brute force with the *obvious* optimisation: on a mismatch, advance `i`
      by $j$ instead of 1 (this is correct — it is the "Sunday"/`strchr` trick). Why is
      it still $O(nm)$ in the worst case? Find the input that forces it.
- [ ] The problem now allows `.` and `*` wildcards (LeetCode 44). Which part of the
      $\pi$ argument breaks, and what is the state you now need? (Hint: it is a set, not
      an integer.)
- [ ] Replace `String` with `char[]` and re-measure. If it is faster, is that a statement
      about `String` or about your loop? Check with the allocation counter from card §10.

### L3.2 — Derive `String.hashCode()` from its spec, and find where it actually breaks

**Statement.** Implement `hash(String)` from the specification
$h_0 = 0,\ h_{i+1} = 31\,h_i + s[i]$, show it agrees with the JDK on every string you can
think of, and then **find the exact length at which collisions appear for
lowercase-only strings**.

**Part 1 — the hand implementation.** The lab's `manualHash()` matches the JDK on 100,000
random lowercase strings of length up to 11, and exhaustively on every lowercase string of
length ≤ 5. That part is mechanical: the spec is five lines of code.

**Part 2 — the interesting part: where do collisions actually come from?** I *predicted*
in the first draft of this exercise that base-31 hashing "produces collisions anyway" for
3-letter lowercase strings, on the grounds that a polynomial is not automatically uniform.
**The exhaustive measurement says the opposite: there are none.**

| length | strings enumerated | colliding strings | distinct buckets |
|---|---|---|---|
| 1 | 26 | **0** | 26 |
| 2 | 676 | **0** | 676 |
| 3 | 17,576 | **0** | 17,576 |
| 4 | 456,976 | **0** | 456,976 |
| 5 | 11,881,376 | **0** | 11,881,376 |

**Why zero — the derivation.** Two strings collide iff their *difference polynomial* is
zero, i.e. $\sum_i d_i 31^{n-1-i} = 0$ where $d_i$ is the difference of the $i$-th code
points. The cheapest non-trivial solution is $d_i = 1$, $d_{i+1} = -31$, because
$1 \cdot 31^{k} - 31 \cdot 31^{k-1} = 0$. That is exactly the `Aa`/`BB` collision, and it
requires one character to move **down 31** and its left neighbour **up 1**.

Now the observation that makes the table above: **that shift is impossible inside
`a`–`z`**, whose width is $122 - 97 = 25 < 31$. So no pair of lowercase strings can ever
satisfy the cheapest collision equation. The rule is verified constructively on four
mixed-case examples, where the shift *is* available:

```
  "Aa"  (2112)   -> "BB"  (2112)     equal: true
  "ABc" (64610)  -> "B#c" (64610)    equal: true
  "ABcd"(2003010) -> "B#cd"(2003010)  equal: true
  "xyz" (119193) -> "yZz" (119193)   equal: true
```

**Part 3 — so where DOES it break?** Not through the polynomial, but through `int`
overflow. The largest hash value for a string of length $L$ is
$122 \cdot \frac{31^L - 1}{30}$:

| length | max hash value | relation to $2^{32}$ |
|---|---|---|
| 5 | 116,425,210 | fits |
| 6 | 3,609,181,632 | fits |
| 7 | 111,884,630,714 | **exceeds $2^{32}$** — wraparound begins |
| 8 | 3,468,423,552,256 | exceeds $2^{32}$ |

And collisions do appear there, all-lowercase, found by random search:

```
len=7 LOWERCASE collision: "gcrynnl" and "ustwjxx" both hash to -133120087
len=8 LOWERCASE collision: "xhnvvsnq" and "uipjokuc" both hash to -243243528
```

**The real lesson, which is not the one I set out to teach.** A hash function is not
"probably uniform" and it is not "uniform by construction". It is *injective on a domain
you can bound* and *colliding outside it*, and you have to know **which** regime you are
in. For LeetCode-style DSA problems, `String.hashCode` on short lowercase identifiers is
effectively a perfect hash, and writing collision-avoidance code for it is wasted effort.
For user passwords, session tokens, or anything adversarial, `String.hashCode` is
catastrophic — and `Aa`/`BB` shows you can construct a collision **by hand, with a
calculator, in under a minute**, which is the difference between "collisions are
unlucky" and "collisions are chosen".

**Space/time.** $O(n)$ per hash, $O(1)$ space. The exhaustive tables cost
$O(26^L)$ time and $O(26^L)$ space for a `HashSet<Integer>` — at $L = 5$ that is 11.9M
boxed integers, which is why the $L = 5$ run needs `-Xmx2g`. A sort-based variant costs
$O(26^L \log 26^L)$ time and the same space; counting sort over $2^{31}$ buckets is
impossible, so the `HashSet` is the right structure and the memory is the real constraint.
Say which of the two you would use and why.

**What I Got Wrong (three separate times, on this one exercise).**
1. I predicted collisions at length 3. Exhaustive measurement found **zero** at length 5.
2. My first measurement script **reported 8 duplicate hashes among 26 single-character
   strings**, which is impossible — a 1-character hash *is* the character. The bug: I
   computed `total = 26^len` and then drew that many *random* strings **with replacement**
   instead of enumerating them. Sampling with replacement is not enumeration, and the
   first thing to do with a surprising measurement is look for a mechanism that makes the
   surprise impossible.
3. I then stated the collision rule as "+1 here, −31 anywhere". It only holds for
   **adjacent** positions, because the weights differ. My three test pairs were built
   wrong and all three disagreed. The correct construction is `$d_i = +1$` at position
   $i$ and `$d_{i+1} = -31$` at position $i+1$, which cancels exactly.

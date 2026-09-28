// Ch03 — Lab 01: short-circuit evaluation is semantics, not an optimisation.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch03-conditional-statements/lab/Ch03ShortCircuit.java
//
// Claims under test:
//   1. `&&`/`||` skip the right operand; `&`/`|` never do — and that is a *correctness*
//      difference, not only a speed difference
//   2. the speed ratio is not a constant: it tracks the cost of the skipped operand
//   3. a benchmark whose result the JIT can prove is measuring NOTHING (see the note
//      in the card — this lab was rewritten once because of exactly that)
public class Ch03ShortCircuit {

    private static final int N = 50_000_000;
    private static final int[] data = {1, 2, 3};

    // Read from the command line so the JIT cannot constant-fold the predicate.
    // 0 means "no target", i.e. the predicate is always false.
    private static int target = Integer.MIN_VALUE;

    public static void main(String[] args) {
        if (args.length > 0) {
            target = Integer.parseInt(args[0]);
        }
        semanticsFirst();
        timingSecond();
    }

    private static void semanticsFirst() {
        System.out.println("--- semantics: the guard that prevents a crash ---");

        int i = 3;
        System.out.println("data.length = " + data.length);

        System.out.print("i < data.length && data[i] > 0   -> ");
        System.out.println(i < data.length && data[i] > 0);

        System.out.print("i < data.length & data[i] > 0    -> ");
        try {
            System.out.println(i < data.length & data[i] > 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getClass().getSimpleName() + "  <-- the guard did nothing");
        }
        System.out.println("  the right operand is EVALUATED for `&`, so the bound check was wasted work");
        System.out.println("  that still crashed. `&&` is correct here; `&` is not a 'faster &&'.");
    }

    private static void timingSecond() {
        System.out.println();
        System.out.printf("--- timing: %,d iterations, guard always false ---%n", N);
        System.out.printf("    predicate target read at runtime = %d%n%n", target);

        System.out.println("  (a) cheap predicate — 1 inner step");
        runPair(1);
        System.out.println("  (b) costly predicate — 200 inner steps");
        runPair(200);
        System.out.println();
        System.out.println("  -> the ratio tracks the cost of the skipped operand, not the loop.");
        System.out.println("     '&& is faster than &' is a useless thing to remember;");
        System.out.println("     'the right side must be safe to skip' is the rule that matters.");
    }

    private static void runPair(int innerSteps) {
        long start = System.nanoTime();
        int hitsAnd = 0;
        for (int k = 0; k < N; k++) {
            if (k < 1 && predicate(k, innerSteps)) {
                hitsAnd++;
            }
        }
        long withAndAnd = System.nanoTime() - start;

        start = System.nanoTime();
        int hitsAmp = 0;
        for (int k = 0; k < N; k++) {
            if (k < 1 & predicate(k, innerSteps)) {
                hitsAmp++;
            }
        }
        long withAmpersand = System.nanoTime() - start;

        System.out.printf("    k < 1 && predicate  %8.1f ms   (hits %d)%n", withAndAnd / 1e6, hitsAnd);
        System.out.printf("    k < 1 &  predicate  %8.1f ms   (hits %d)  %.1fx%n",
                withAmpersand / 1e6, hitsAmp, (double) withAmpersand / withAndAnd);
        if (hitsAnd != hitsAmp) {
            throw new AssertionError("&& and & disagreed on the result: " + hitsAnd + " vs " + hitsAmp);
        }
    }

    private static boolean predicate(int k, int innerSteps) {
        int acc = k;
        for (int s = 0; s < innerSteps; s++) {
            acc = acc * 31 + s;
        }
        return acc == target;
    }
}

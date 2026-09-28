// Ch01 — Lab 01: the JVM does not run your bytecode the whole time.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch01-computers-programs-java/lab/Ch01JitWarmup.java
// Then: java -Xint -cp .java-course-build Ch01JitWarmup     (interpreter only, no JIT)
//
// Claim under test: the first batches run far slower than the last ones, because the
// JVM starts in the interpreter and promotes hot methods to compiled code.
public class Ch01JitWarmup {

    private static final int BATCHES = 10;
    private static final int OPS_PER_BATCH = 2_000_000;

    public static void main(String[] args) {
        System.out.printf("%,d ops per batch, %,d batches%n%n", OPS_PER_BATCH, BATCHES);
        System.out.printf("%-8s %12s %12s%n", "batch", "ms", "ns/op");
        System.out.println("  " + "-".repeat(34));

        long firstBatch = -1;
        long lastBatch = -1;

        for (int batch = 1; batch <= BATCHES; batch++) {
            long start = System.nanoTime();
            long total = 0;
            for (int i = 0; i < OPS_PER_BATCH; i++) {
                total += i;          // the hot loop
            }
            long elapsed = System.nanoTime() - start;
            if (total == -1) {
                System.out.println("unreachable, keeps the loop from being optimised away");
            }
            if (firstBatch < 0) {
                firstBatch = elapsed;
            }
            lastBatch = elapsed;

            double nsPerOp = (double) elapsed / OPS_PER_BATCH;
            System.out.printf("%-8d %12.2f %12.2f%s%n", batch, elapsed / 1_000_000.0, nsPerOp,
                    batch == 1 ? "   <- cold" : "");
        }

        System.out.printf("%nlast batch is %.1fx faster than the first%n",
                (double) firstBatch / lastBatch);
    }
}

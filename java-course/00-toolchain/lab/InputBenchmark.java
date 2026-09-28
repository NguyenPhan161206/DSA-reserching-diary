// Ch01/Ch04 — Lab 03: the same algorithm, three input strategies, one measured number.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/00-toolchain/lab/InputBenchmark.java [tokenCount]
//
// All three readers do exactly the same job: read N whitespace-separated integers and
// return their sum. The only difference is how the bytes are turned into values.
// If the input method is not part of the algorithm, the timing difference is pure waste.
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InputBenchmark {

    private static final int DEFAULT_TOKENS = 1_000_000;
    private static final int ROUNDS = 2;   // best-of: the first round pays JIT compilation

    public static void main(String[] args) throws IOException {
        int tokens = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_TOKENS;

        byte[] data = generateInput(tokens);
        long expected = expectedSum(tokens);

        System.out.printf("tokens: %,d   input size: %,d bytes%n", tokens, data.length);
        System.out.printf("expected sum: %d%n%n", expected);

        long scannerNs = time(() -> sumWithScanner(data));
        long bufferedNs = time(() -> sumWithBufferedReader(data));
        long fastNs = time(() -> sumWithFastScanner(data));

        System.out.printf("%-22s %8d ms   %5.1fx%n", "Scanner.nextInt()", scannerNs / 1_000_000, (double) scannerNs / fastNs);
        System.out.printf("%-22s %8d ms   %5.1fx%n", "BufferedReader", bufferedNs / 1_000_000, (double) bufferedNs / fastNs);
        System.out.printf("%-22s %8d ms   %5.1fx  (baseline)%n", "FastScanner", fastNs / 1_000_000, 1.0);
    }

    // ── readers ────────────────────────────────────────────────────────────────────

    /** The textbook reader. One String allocation per token, regex-based parsing. */
    private static long sumWithScanner(byte[] data) {
        System.setIn(new ByteArrayInputStream(data));
        long sum = 0;
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextInt()) {
                sum += scanner.nextInt();
            }
        }
        return sum;
    }

    /** The competitive-programming reader: chars in, value accumulated in place. */
    private static long sumWithBufferedReader(byte[] data) throws IOException {
        System.setIn(new ByteArrayInputStream(data));
        long sum = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.US_ASCII), 1 << 16)) {
            int c = reader.read();
            while (c != -1) {
                if (c > ' ') {
                    boolean negative = false;
                    if (c == '-') {
                        negative = true;
                        c = reader.read();
                    }
                    long value = 0;
                    while (c > ' ') {
                        value = value * 10 + (c - '0');
                        c = reader.read();
                    }
                    sum += negative ? -value : value;
                } else {
                    c = reader.read();
                }
            }
        }
        return sum;
    }

    /** The reusable helper from FastScanner.java. */
    private static long sumWithFastScanner(byte[] data) throws IOException {
        System.setIn(new ByteArrayInputStream(data));
        return new FastScanner().sumAll();
    }

    // ── harness ────────────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface Timed {
        long run() throws IOException;
    }

    private static long time(Timed task) throws IOException {
        long best = Long.MAX_VALUE;
        long sum = 0;
        for (int i = 0; i < ROUNDS; i++) {
            long start = System.nanoTime();
            sum = task.run();
            best = Math.min(best, System.nanoTime() - start);
        }
        if (sum != expectedSum(cache)) {
            throw new AssertionError("reader disagrees with the expected sum");
        }
        return best;
    }

    private static int cache;   // set by expectedSum, reused by the sanity check above

    private static byte[] generateInput(int tokens) {
        StringBuilder sb = new StringBuilder(tokens * 7);
        for (int i = 0; i < tokens; i++) {
            sb.append(i % 1000).append(' ');
        }
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static long expectedSum(int tokens) {
        long sum = 0;
        for (int i = 0; i < tokens; i++) {
            sum += i % 1000;
        }
        cache = tokens;
        return sum;
    }
}

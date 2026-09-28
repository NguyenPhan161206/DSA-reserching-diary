// Ch01/Ch04 — a reusable, allocation-light integer reader.
//
// Why this class exists: java.util.Scanner parses with a regular expression per token
// and allocates a String per token. On a judge-sized input (10^6 tokens) that is the
// difference between Accepted and Time Limit Exceeded. This class reads bytes and
// builds the value in the int itself — no String, no regex, no garbage.
//
// It is a copy-paste library class, not a teaching class: it has no main().
// Measured against Scanner in ./InputBenchmark.java.
public class FastScanner {

    private final byte[] buffer = new byte[1 << 16];
    private int ptr = 0;
    private int len = 0;

    /** Fills the buffer if empty. Returns -1 at end of stream. */
    private int read() throws java.io.IOException {
        if (ptr >= len) {
            len = System.in.read(buffer);
            ptr = 0;
            if (len <= 0) {
                return -1;
            }
        }
        return buffer[ptr++];
    }

    /** Next whitespace-delimited integer, or -1 when the input is exhausted. */
    public int nextInt() throws java.io.IOException {
        int c = read();
        while (c != -1 && c <= ' ') {
            c = read();
        }
        if (c == -1) {
            return -1;
        }
        int sign = 1;
        if (c == '-') {
            sign = -1;
            c = read();
        }
        int value = 0;
        while (c > ' ') {
            value = value * 10 + (c - '0');
            c = read();
        }
        return value * sign;
    }

    /** Reads every remaining token and sums it — used to prove the reader is complete. */
    public long sumAll() throws java.io.IOException {
        long sum = 0;
        int v;
        while ((v = nextInt()) != -1) {
            sum += v;
        }
        return sum;
    }
}

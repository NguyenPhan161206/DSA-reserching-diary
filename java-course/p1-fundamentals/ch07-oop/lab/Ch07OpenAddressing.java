// Ch07 — Lab 03 (exercises L3): an open-addressing set, and where O(1) stops being true.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch07-oop/lab/Ch07OpenAddressing.java
//
// The L3 exercise in ../exercises.md. Deliberately contains a BUG: the empty-slot
// sentinel is 0, so 0 cannot be stored. Two rows of the test table fail, and a
// brute-force reference in the same harness is correct on every row. Reading the
// disagreement is the exercise.

import java.util.Random;


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
        int i = key & mask;
        while (table[i] != 0) {
            if (table[i] == key) return false;
            i = (i + 1) & mask;
        }
        table[i] = key;
        size++;
        return true;
    }

    int size() { return size; }
}

public class Ch07OpenAddressing {
    static int distinct(int[] a, int capPow2) {
        IntSet s = new IntSet(capPow2);
        for (int v : a) s.add(v);
        return s.size();
    }

    static int brute(int[] a) {
        int c = 0;
        for (int i = 0; i < a.length; i++) {
            boolean seen = false;
            for (int j = 0; j < i; j++) if (a[i] == a[j]) { seen = true; break; }
            if (!seen) c++;
        }
        return c;
    }

    public static void main(String[] args) {
        int cap = 1 << 20;
        String[] names = {"[]","[7]","[1,1,1,1]","[1,2,3,4]","[0,0]","[0,0,1]","[0,1]","[4,8,12]","[1,5,9,13]","[3,1,3,1,3]"};
        int[][] ins = {{}, {7}, {1,1,1,1}, {1,2,3,4}, {0,0}, {0,0,1}, {0,1}, {4,8,12}, {1,5,9,13}, {3,1,3,1,3}};
        int[] exp = {0, 1, 1, 4, 1, 2, 2, 3, 4, 2};
        System.out.printf("%-14s %8s %8s %8s  %s%n", "input", "expected", "IntSet", "brute", "verdict");
        int fail = 0;
        for (int r = 0; r < names.length; r++) {
            int got = distinct(ins[r], cap);
            int br = brute(ins[r]);
            boolean ok = got == exp[r];
            if (!ok) fail++;
            System.out.printf("%-14s %8d %8d %8d  %s%n", names[r], exp[r], got, br,
                ok ? "ok" : "*** MISMATCH *** (brute correct: " + (br == exp[r]) + ")");
        }
        System.out.println("\n" + fail + " mismatches against the expected column");

        int n = 1_000_000, m = 1_000_000;
        Random rnd = new Random(42);
        int[] big = new int[n];
        for (int i = 0; i < n; i++) big[i] = rnd.nextInt(m);
        int got = distinct(big, 1 << 21);
        double e = m * (1 - Math.exp(-(double) n / m));
        System.out.printf("%nrow 10: 1e6 random ints in [0,1e6) -> got %d, Poisson expects %.0f%n", got, e);
        System.out.printf("  relative error %.3f%%  -> %s%n", 100.0 * Math.abs(got - e) / e,
            Math.abs(got - e) / e < 0.01 ? "ok" : "off");
    }
}

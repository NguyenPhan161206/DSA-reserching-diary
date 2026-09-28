// Ch02 — Lab 02: `+=` is not `= +` for a byte, and narrowing is not checked at runtime.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch02-elementary-programming/lab/Ch02Narrowing.java
//
// Claim under test: the compound assignment `b += 1` compiles on a byte while
// `b = b + 1` does not — one is a language rule, the other is a compiler convenience.
public class Ch02Narrowing {

    public static void main(String[] args) {
        wideningIsFree();
        narrowingNeedsACast();
        compoundVsBinary();
        overflowIsSilent();
        floatCannotHoldEveryInt();
    }

    private static void wideningIsFree() {
        System.out.println("--- widening: implicit, always exact ---");
        byte b = 100;
        int i = b;          // no cast needed, no data lost
        long l = i;
        double d = l;
        System.out.printf("byte %d -> int %d -> long %d -> double %.1f%n", b, i, l, d);
        System.out.println("  widening from int to double is exact only up to 2^53");
        System.out.printf("  2^53+1 as long then as double: %d -> %.0f  (lost! in Java 8, fixed in Java 9)%n",
                9007199254740993L, (double) 9007199254740993L);
    }

    private static void narrowingNeedsACast() {
        System.out.println();
        System.out.println("--- narrowing: explicit, and lossy ---");
        double big = 1e20;
        int truncated = (int) big;      // defined behaviour, NOT an exception
        System.out.printf("(int) %.0e = %d%n", big, truncated);
        System.out.println("  narrowing a double to int saturates and discards the fraction;");
        System.out.println("  NaN becomes 0. It never throws, so it never warns you.");
    }

    private static void compoundVsBinary() {
        System.out.println();
        System.out.println("--- `b += 1` compiles, `b = b + 1` does not ---");
        byte b = 127;
        b += 1;                       // compiles: implicit cast (int) back to byte
        System.out.printf("byte b = 127; b += 1;  ->  b == %d%n", b);
        System.out.println("  b = b + 1; would be a COMPILE ERROR, and it is on purpose:");
        System.out.println("  the language refuses to narrow implicitly, but the compound form");
        System.out.println("  is defined as b = (byte)(b + 1) — the cast is added for you.");
    }

    private static void overflowIsSilent() {
        System.out.println();
        System.out.println("--- overflow wraps, it does not throw ---");
        int max = Integer.MAX_VALUE;
        System.out.printf("Integer.MAX_VALUE          = %d%n", max);
        System.out.printf("Integer.MAX_VALUE + 1      = %d%n", max + 1);
        System.out.printf("Integer.MIN_VALUE - 1      = %d%n", Integer.MIN_VALUE - 1);
        System.out.printf("1_000_000_000 * 3 (int)    = %d%n", 1_000_000_000 * 3);
        System.out.printf("1_000_000_000L * 3 (long)   = %d%n", 1_000_000_000L * 3);
        System.out.println("  -> 3_000_000_000 fits in a long but not an int. The first line is wrong;");
        System.out.println("     nothing warns you, and the judge returns Wrong Answer.");
    }

    private static void floatCannotHoldEveryInt() {
        System.out.println();
        System.out.println("--- float loses precision where int does not ---");
        System.out.printf("(float) 16_777_217   = %.0f   (int max 2^31-1 is fine, but float has 24 bits)%n",
                (float) 16_777_217);
        System.out.printf("(double) 16_777_217  = %.0f   (double has 53 bits)%n",
                (double) 16_777_217);
        System.out.println("  -> never use float for money. Use long for cents, BigDecimal for money.");
    }
}

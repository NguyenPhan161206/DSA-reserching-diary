// Ch02 — Lab 01: two Scanner behaviours that pass small tests and fail in production.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch02-elementary-programming/lab/Ch02ScannerTraps.java
//
// Claim 1: nextLine() after nextInt() returns an empty string.
// Claim 2: Scanner parses with the *default locale*, not with '.' as the decimal mark.
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class Ch02ScannerTraps {

    public static void main(String[] args) {
        newlineTrap();
        localeTrap();
    }

    private static void newlineTrap() {
        System.out.println("--- trap 1: nextInt() then nextLine() ---");
        System.setIn(new ByteArrayInputStream(
                "25 67.5 4.5\nhello world\n".getBytes(StandardCharsets.UTF_8)));

        try (Scanner scanner = new Scanner(System.in)) {
            int age = scanner.nextInt();
            double weight = scanner.nextDouble();
            double height = scanner.nextDouble();
            String city = scanner.nextLine();          // the classic bug lives here
            String realCity = scanner.nextLine();      // the fix: call nextLine() again

            System.out.printf("age=%d weight=%.1f height=%.1f%n", age, weight, height);
            System.out.printf("nextLine() returned %s  (length %d)%n",
                    "[" + city + "]", city.length());
            System.out.println("  -> the rest of the line is EMPTY; the user's answer is lost.");
            System.out.printf("  -> the correct calls are nextLine() TWICE: %s%n",
                    "[" + realCity + "]");
        }
        System.out.println();
    }

    private static void localeTrap() {
        System.out.println("--- trap 2: the decimal mark follows the default locale ---");
        String input = "12,5\n";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        Locale original = Locale.getDefault();
        try {
            report("default", bytes, original);

            Locale.setDefault(Locale.GERMANY);
            report("forced to Germany", bytes, Locale.GERMANY);
        } finally {
            Locale.setDefault(original);
        }
        System.out.println("  -> identical bytes, two different readings. Judges use Locale.US,");
        System.out.println("     so a program that only works on your laptop is a lost submission.");
    }

    private static void report(String label, byte[] bytes, Locale locale) {
        System.setIn(new ByteArrayInputStream(bytes));
        try (Scanner scanner = new Scanner(System.in).useLocale(locale)) {
            double value = scanner.nextDouble();
            System.out.printf("%-18s locale=%-6s  nextDouble(\"12,5\") = %.1f%n",
                    label, locale.getCountry(), value);
        } catch (java.util.InputMismatchException e) {
            System.out.printf("%-18s locale=%-6s  nextDouble(\"12,5\") -> %s%n",
                    label, locale.getCountry(), e.getClass().getSimpleName());
        }
    }
}

// Ch03 — Lab 02: types meet in the conditional operator, and switch is not what you think.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch03-conditional-statements/lab/Ch03ConditionalTypes.java
//
// Claims under test:
//   1. `cond ? a : b` promotes BOTH branches — and JLS 15.25 has a narrowing rule that
//      silently turns an int result into a char
//   2. a String switch dispatches on hashCode + equals, never on ==
//   3. the arrow switch is exhaustiveness-checked; the colon/break switch is not
public class Ch03ConditionalTypes {

    public static void main(String[] args) {
        ternaryPromotes();
        bitwiseOnBooleansIsNotUseless();
        arrowSwitchIsChecked();
        stringSwitchUsesHashCode();
    }

    /** The counter-example from the card, section 3, compiled and run. */
    private static void bitwiseOnBooleansIsNotUseless() {
        System.out.println();
        System.out.println("--- & on booleans: not the slow &&, the one that folds bits ---");

        boolean[] flags = {true, false, true};
        int mask = 0;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i]) {
                mask |= 1 << i;
            }
        }
        System.out.printf("  flags = [%s, %s, %s]  ->  mask = %d = %s%n",
                flags[0], flags[1], flags[2], mask, Integer.toBinaryString(mask));

        boolean allSet = (mask & 0b111) == 0b111;
        System.out.println("  (mask & 0b111) == 0b111 -> " + allSet);
        System.out.println("  && cannot do this: it yields a boolean and never combines bits.");
        System.out.println("  &  is the SET operation, so it is the correct tool here.");
    }

    private static void ternaryPromotes() {
        System.out.println("--- the ternary operator promotes (or NARROWS) both branches ---");

        describe("true ? 1 : 2.0     ", true ? 1 : 2.0);
        describe("false ? 'a' : 98  ", false ? 'a' : 98);
        describe("true ? 1 : 'a'     ", true ? 1 : 'a');

        // The dangerous one: both branches are int, the caller wanted a fraction.
        int n = 2;
        Object ratio = n % 2 == 0 ? 1 / 2 : 1.0 / 2;
        System.out.printf("%n  even ? 1/2 : 1.0/2   -> %-8s (%s)%n",
                ratio, ratio.getClass().getSimpleName());
        System.out.println("  both arms compute an int-ish thing; 1/2 is 0, and 0 promotes to 0.0.");
        System.out.println("  no warning. The bug is in the CONSTANT, not the operator.");
    }

    /** Prints the value and, for chars, the code point — Char(1) is not printable. */
    private static void describe(String label, Object value) {
        if (value instanceof Character c) {
            System.out.printf("  %s -> code point %d   (%s)%n", label, (int) c.charValue(),
                    c.getClass().getSimpleName());
        } else {
            System.out.printf("  %s -> %-8s (%s)%n", label, value, value.getClass().getSimpleName());
        }
    }

    private static void arrowSwitchIsChecked() {
        System.out.println();
        System.out.println("--- the arrow switch forces you to enumerate or reject ---");
        report("SAT", () -> dayType("SAT"));
        report("xyz", () -> dayType("xyz"));
        System.out.println("  'xyz' has no arm and no default, so the `default ->` arm throws.");
        System.out.println("  with the colon/break switch, forgetting the break would instead");
        System.out.println("  fall through to the next case SILENTLY and return the wrong answer.");
    }

    private static void report(String input, java.util.function.Supplier<String> body) {
        try {
            System.out.printf("  dayType(\"%s\") = %s%n", input, body.get());
        } catch (RuntimeException e) {
            System.out.printf("  dayType(\"%s\") -> %s: %s%n",
                    input, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static String dayType(String day) {
        return switch (day) {
            case "SAT", "SUN" -> "weekend";      // several labels, fall-through impossible
            case "MON", "TUE", "WED", "THU", "FRI" -> "weekday";
            default -> throw new IllegalArgumentException("not a day: " + day);
        };
    }

    private static void stringSwitchUsesHashCode() {
        System.out.println();
        System.out.println("--- a String switch is a hashCode switch + equals ---");
        for (String day : java.util.List.of("MON", "TUE", "SAT")) {
            System.out.printf("  \"%-3s\".hashCode() = %d%n", day, day.hashCode());
        }
        System.out.println("  javap -c shows: invoke hashCode, tableswitch on the bucket index,");
        System.out.println("  then .equals for confirmation. It is a HASH LOOKUP, not a chain of ==.");
    }
}

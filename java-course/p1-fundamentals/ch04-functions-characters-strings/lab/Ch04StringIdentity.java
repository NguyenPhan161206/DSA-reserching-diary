// Ch04 — Lab 02: `==` on String is a reference compare, and interning makes it "work".
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch04-functions-characters-strings/lab/Ch04StringIdentity.java
//
// Claims under test:
//   1. `==` on String compares references, not characters
//   2. string *literals* are interned, so `==` "works" on literals and fails everywhere else
//   3. `String.hashCode()` is a fixed polynomial, so collisions are *constructible*
import java.util.HashMap;
import java.util.Map;

public class Ch04StringIdentity {

    public static void main(String[] args) {
        literalsAreInterned();
        builtStringsAreNot();
        hashCollisionsAreReal();
        whatHashMapActuallyDoes();
    }

    private static void literalsAreInterned() {
        System.out.println("--- literals are interned, so `==` appears to work ---");

        String a = "hello";
        String b = "hello";
        System.out.printf("  a = \"hello\", b = \"hello\"   a == b -> %-5s   a.equals(b) -> %s%n",
                a == b, a.equals(b));
        System.out.printf("  same identity? %s   (a.hashCode() = %d)%n",
                a == b, a.hashCode());

        String fromNew = new String("hello");
        System.out.printf("  c = new String(\"hello\")      c == a -> %-5s   c.equals(a) -> %s%n",
                fromNew == a, fromNew.equals(a));
        System.out.printf("  same identity? %s   (c.hashCode() = %d, EQUAL to a's)%n",
                fromNew == a, fromNew.hashCode());
        System.out.println("  -> the one character-for-character operator gives the WRONG answer,");
        System.out.println("     and both objects have the same hashCode, because hash is about");
        System.out.println("     CONTENT and == is about IDENTITY. Two different questions.");
    }

    private static void builtStringsAreNot() {
        System.out.println();
        System.out.println("--- anything built at runtime is a fresh object ---");

        String x = "ab";
        String constantFolded = "a" + "b";
        String fromBuilder = new StringBuilder("ab").toString();
        String fromChars = String.valueOf(new char[]{'a', 'b'});

        System.out.printf("  \"ab\" == \"a\" + \"b\"                   -> %-5s  <- TRUE, and that is a trap%n",
                x == constantFolded);
        System.out.printf("  \"ab\" == new StringBuilder(\"ab\")     -> %-5s%n", x == fromBuilder);
        System.out.printf("  \"ab\" == String.valueOf(char[])      -> %-5s%n", x == fromChars);
        System.out.printf("  all three equal()? %s / %s / %s   (content is identical)%n",
                x.equals(constantFolded), x.equals(fromBuilder), x.equals(fromChars));
        System.out.println("  -> the FIRST one is true because javac constant-folds \"a\" + \"b\" into");
        System.out.println("     the single interned literal \"ab\" before it ever runs. It is not");
        System.out.println("     because string concatenation interns its result — it does not.");
        System.out.println("     So `==` looks reliable for a season, then fails on the one input");
        System.out.println("     that came from a scanner: the classic \"0 on mine, WA on theirs\".");
    }

    private static void hashCollisionsAreReal() {
        System.out.println();
        System.out.println("--- hashCode() is a polynomial, so collisions are constructible ---");

        System.out.printf("  \"Aa\".hashCode() = %d%n", "Aa".hashCode());
        System.out.printf("  \"BB\".hashCode() = %d%n", "BB".hashCode());
        System.out.printf("  equal content? %-5s   same hash? %s%n",
                "Aa".equals("BB"), "Aa".hashCode() == "BB".hashCode());

        // The polynomial: s[0]*31^(n-1) + s[1]*31^0
        System.out.println();
        System.out.println("  the formula is sum(s[i] * 31^(n-1-i)):");
        for (String s : new String[]{"Aa", "BB", "hello", "AaAa", "BBBB"}) {
            System.out.printf("    %-6s -> %d%n", s, manualHash(s));
        }
        System.out.println("  and by hand:  'A'*31 + 'a' = 65*31 + 97 = " + (65 * 31 + 97));
        System.out.println("                 'B'*31 + 'B' = 66*31 + 66 = " + (66 * 31 + 66));
        System.out.println("  both are " + (65 * 31 + 97) + ". Any two chars with codes c1, c2 and");
        System.out.println("  c1 + c2 equal collide, so two-char collisions are trivial to build.");
        System.out.println("  (HashMap survives this because it falls back to equals() on the");
        System.out.println("  bucket; collisions cost time, they do not cost correctness.)");
    }

    /** String.hashCode() from the JLS/JDK spec, written by hand. */
    private static int manualHash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 31 * h + s.charAt(i);
        }
        return h;
    }

    private static void whatHashMapActuallyDoes() {
        System.out.println();
        System.out.println("--- a HashMap needs BOTH hashCode and equals ---");

        Map<String, String> map = new HashMap<>();
        map.put(new String("key"), "value");

        System.out.printf("  put(new String(\"key\"), ...) then map.get(\"key\") -> %s%n", map.get("key"));
        System.out.printf("  size = %d   (the new String and the literal are the same key)%n", map.size());
        System.out.println("  HashMap called hashCode (equal), found the bucket, then called");
        System.out.println("  equals (equal) -> hit. If equals were identity, this would MISS.");
        System.out.println("  A class that overrides equals WITHOUT hashCode still breaks the map,");
        System.out.println("  which is why the compiler warns on exactly that combination.");
    }
}

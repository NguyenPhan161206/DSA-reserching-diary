// Ch07 — Lab 01: equals and hashCode are a CONTRACT, and half of it is optional.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch07-oop/lab/Ch07EqualsHashCode.java
//
// Claims under test:
//   1. overriding equals WITHOUT hashCode compiles, and silently breaks every hash container
//   2. the reverse (hashCode without equals) is not a correctness bug for HashSet
//      membership, but it breaks dedup by iteration order — measure it, do not guess
//   3. a symmetric-violating equals (a.equals(b) != b.equals(a)) breaks HashMap lookups
//      in a way that depends on WHICH key you inserted
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Ch07EqualsHashCode {

    public static void main(String[] args) {
        caseInsensitiveOnlyEquals();
        asymmetricEquals();
        inheritedContract();
        recordsAndBrokenPairs();
        constantHashIsCorrectAndQuadratic();
    }

    /** The "fix" people reach for when dedup misbehaves: return a constant. It is
     *  correct, and it turns every lookup into a linked-list scan. */
    static class ConstHash {
        final int v;

        ConstHash(int v) {
            this.v = v;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ConstHash c && c.v == v;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    static class RealHash {
        final int v;

        RealHash(int v) {
            this.v = v;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof RealHash r && r.v == v;
        }

        @Override
        public int hashCode() {
            return v;
        }
    }

    private static void constantHashIsCorrectAndQuadratic() {
        System.out.println();
        System.out.println("--- 'just return a constant' fixes dedup and wrecks the map ---");
        System.out.println("  IDENTICAL equals() in both classes. The only difference is hashCode().");
        System.out.println();
        System.out.printf("  %12s %16s %14s   %s%n", "n", "const-hash total", "ns/lookup", "real-hash total");
        long lastConst = 0, lastReal = 0;
        int lastN = 0;
        for (int n : new int[]{1_000, 2_000, 4_000, 8_000, 16_000}) {
            long constNs = timeConst(n);
            long realNs = timeReal(n);
            System.out.printf("  %12d %13.2f ms %14.1f   %7.2f ms%n",
                    n, constNs / 1e6, (double) constNs / n, realNs / 1e6);
            lastConst = constNs;
            lastReal = realNs;
            lastN = n;
        }
        System.out.println();
        System.out.println("  both maps are CORRECT — every key is found, which the lab asserts.");
        System.out.println("  But every key hashes to 0, so the map is one long linked list and a");
        System.out.println("  lookup costs O(n) 'equals' calls. Doubling n quadruples the time.");
        System.out.println("  'return 0' is a legal, compiling, test-passing way to make a map");
        System.out.println("  quadratic. It is worse than the bug it was meant to fix.");
        System.out.printf("  at n=%,d the total differs by %.0fx, and the constant-hash map needs%n",
                lastN, (double) lastConst / lastReal);
        System.out.printf("  %.1f microseconds for ONE lookup.%n", lastConst / (double) lastN / 1e3);
    }

    private static long timeConst(int n) {
        Map<ConstHash, Integer> m = new HashMap<>();
        for (int i = 0; i < n; i++) {
            m.put(new ConstHash(i), i);
        }
        long t0 = System.nanoTime();
        int found = 0;
        for (int i = 0; i < n; i++) {
            if (m.get(new ConstHash(i)) != null) {
                found++;
            }
        }
        long dt = System.nanoTime() - t0;
        if (found != n) {
            throw new AssertionError("const-hash map lost a key: " + found + " of " + n);
        }
        return dt;
    }

    private static long timeReal(int n) {
        Map<RealHash, Integer> m = new HashMap<>();
        for (int i = 0; i < n; i++) {
            m.put(new RealHash(i), i);
        }
        long t0 = System.nanoTime();
        int found = 0;
        for (int i = 0; i < n; i++) {
            if (m.get(new RealHash(i)) != null) {
                found++;
            }
        }
        long dt = System.nanoTime() - t0;
        if (found != n) {
            throw new AssertionError("real-hash map lost a key: " + found + " of " + n);
        }
        return dt;
    }

    /** The bug that compiles. This is the one that reaches production. */
    private static void caseInsensitiveOnlyEquals() {
        System.out.println("--- a String subclass that overrides equals but not hashCode ---");

        Set<CaseInsensitive> set = new HashSet<>();
        set.add(new CaseInsensitive("java"));
        set.add(new CaseInsensitive("JAVA"));
        System.out.printf("  HashSet.size() = %d   (two equals objects, so the answer is 1)%n", set.size());

        Map<CaseInsensitive, String> map = new HashMap<>();
        map.put(new CaseInsensitive("java"), "found");
        String got = map.get(new CaseInsensitive("JAVA"));
        System.out.printf("  HashMap.get(equal key) = %s%n", got);
        System.out.println("  -> the get returned null. The put and the get landed in different");
        System.out.println("     buckets, so the map never even compared them.");
        System.out.println("  -> add the same object twice:");
        set.clear();
        CaseInsensitive one = new CaseInsensitive("java");
        set.add(one);
        set.add(one);
        System.out.printf("     same instance added twice -> size = %d   (works! equal to itself)%n",
                set.size());
        System.out.println("  -> so a test using one object passes, and a test using two passes,");
        System.out.println("     while real data built by parsing fails. That is why this bug ships.");
    }

    private static void asymmetricEquals() {
        System.out.println();
        System.out.println("--- an asymmetric equals, and a broken hashCode that HIDES it ---");

        // An equals that accepts a String as well as its own type. This is the common
        // mistake: `instanceof CharSequence` instead of `instanceof MyType`.
        CaseInsensitive java = new CaseInsensitive("java");
        String plain = "JAVA";

        System.out.printf("  java.equals(plain) = %s   (we control this side)%n", java.equals(plain));
        System.out.printf("  plain.equals(java) = %s   (String.equals, and it says false)%n",
                plain.equals(java));
        System.out.println("  same two objects, opposite answers. The contract requires symmetry.");
        System.out.println("  (With `instanceof CaseInsensitive` both sides are false, which is");
        System.out.println("   symmetric but useless — the run above is the interesting one.)");

        Map<Object, String> map = new HashMap<>();
        map.put(java, "value-from-subclass-key");
        System.out.printf("%n  map.put(java);   map.get(plain)   = %s%n", map.get(plain));
        System.out.printf("    reason: HashMap asks key.equals(stored), key = plain (String),");
        System.out.println("    so String.equals says false. The asymmetry is visible here.");

        Map<Object, String> other = new HashMap<>();
        other.put(plain, "value-from-string-key");
        System.out.printf("  map2.put(plain); map2.get(java)   = %s%n", other.get(java));
        System.out.println("    reason: DIFFERENT and more instructive. Here key = java, so");
        System.out.println("    CaseInsensitive.equals(plain) would say TRUE — but the two never");
        System.out.println("    met, because CaseInsensitive does not override hashCode, so the");
        System.out.println("    two keys hash into different buckets and equals is never called.");
        System.out.println("    -> an asymmetric equals that ALSO breaks the hash contract is");
        System.out.println("       invisible through the map entirely. Fix hashCode first, then");
        System.out.println("       the asymmetry becomes the visible failure. The two bugs mask");
        System.out.println("       each other, which is why this class of bug is hard to see.");
    }

    private static void inheritedContract() {
        System.out.println();
        System.out.println("--- instanceof-based equals, and what a subclass breaks ---");

        Vehicle car = new Car("Toyota");
        Vehicle car2 = new Car("Toyota");
        System.out.printf("  two Cars, same model: car.equals(car2) = %s%n", car.equals(car2));

        ElectricCar ev = new ElectricCar("Toyota", 75);
        ElectricCar ev2 = new ElectricCar("Toyota", 75);
        System.out.printf("  two ElectricCars, same model+battery: equals = %s%n", ev.equals(ev2));

        System.out.println();
        System.out.println("  now the interesting pair: an ElectricCar and a Car, where the");
        System.out.println("  ElectricCar has a field the Car does not:");
        System.out.printf("    ev.equals(car)  = %s   (the Car is not an ElectricCar)%n", ev.equals(car));
        System.out.printf("    car.equals(ev)  = %s   (an ElectricCar IS a Vehicle)%n", car.equals(ev));
        System.out.println("  -> ASYMMETRIC. ElectricCar.equals added a `instanceof ElectricCar`");
        System.out.println("     check, and that check has no counterpart in Vehicle.equals.");
        System.out.println("     The subclass broke the contract its parent was satisfying.");
        System.out.println("  the two legal repairs, and they are DIFFERENT designs:");
        System.out.println("    (a) use getClass() in the base class -> both sides false (strict,");
        System.out.println("        but a Car and an ElectricCar with the same model stop being equal);");
        System.out.println("    (b) keep instanceof in the base and make ElectricCar use the same");
        System.out.println("        test -> both sides true, battery ignored (loose, but symmetric).");
        System.out.println("  neither is 'the right one'. the CONTRACT is the requirement;");
        System.out.println("  the test is a design decision you have to make on purpose.");

        Set<Vehicle> set = new HashSet<>();
        set.add(ev);
        set.add(ev2);
        System.out.printf("%n  HashSet of two equal ElectricCars: size = %d   (dedup works)%n",
                set.size());
        Set<Vehicle> mixed = new HashSet<>();
        mixed.add(car);
        mixed.add(ev);
        System.out.printf("  HashSet of {Car, ElectricCar} with the same model: size = %d%n",
                mixed.size());
        System.out.println("  -> 1 means 'the same vehicle', 2 means 'a different vehicle'.");
        System.out.println("     The set size changed because of a DESIGN decision, not a bug. That");
        System.out.println("     is the point: equals is not a technicality, it is a domain decision.");
    }

    private static void recordsAndBrokenPairs() {
        // and the correct, boring way
        record Pt(int x, int y) { }
        record PtBadHash(int x, int y) {
            // equals compares x, hashCode hashes y. EQUAL OBJECTS, DIFFERENT HASHES.
            @Override public boolean equals(Object o) { return o instanceof PtBadHash p && p.x == x; }
            @Override public int hashCode() { return y; }
        }
        System.out.println();
        System.out.println("  records get equals+hashCode from the compiler, consistently.");
        System.out.println("  a hand-written pair that compares one field and hashes another:");
        PtBadHash p1 = new PtBadHash(1, 2), p2 = new PtBadHash(1, 99);
        Set<PtBadHash> pts = new HashSet<>();
        pts.add(p1);
        pts.add(p2);
        System.out.printf("    p1.equals(p2) = %s (x matches)%n", p1.equals(p2));
        System.out.printf("    p1.hashCode() = %d, p2.hashCode() = %d (y differs)%n",
                p1.hashCode(), p2.hashCode());
        System.out.printf("    set.size() = %d   <- 2 EQUAL objects, and the set kept both%n",
                pts.size());
        System.out.println("    this is a contract VIOLATION, not a design choice: if a == b then");
        System.out.println("    a.hashCode() == b.hashCode() is REQUIRED, and a set that keeps two");
        System.out.println("    equal objects is broken. Note javac does NOT warn here, because");
        System.out.println("    hashCode IS overridden -- it just disagrees with equals.");
    }

    // ---------- the classes under test ----------

    static class CaseInsensitive {
        private final String name;

        CaseInsensitive(String name) {
            this.name = name;
        }

        // CharSequence, not CaseInsensitive — this is what makes it asymmetric.
        @Override
        public boolean equals(Object other) {
            return other instanceof CharSequence cs
                    && name.equalsIgnoreCase(cs.toString());
        }

        @Override
        public String toString() {
            return "CI(" + name + ")";
        }
    }

    static class Vehicle {
        protected final String model;

        Vehicle(String model) {
            this.model = model;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Vehicle v && model.equals(v.model);
        }

        @Override
        public int hashCode() {
            return Objects.hash(model);
        }
    }

    static class Car extends Vehicle {
        Car(String model) {
            super(model);
        }
    }

    static class ElectricCar extends Car {
        private final int battery;

        ElectricCar(String model, int battery) {
            super(model);
            this.battery = battery;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ElectricCar e && battery == e.battery && super.equals(o);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + battery;
        }
    }
}

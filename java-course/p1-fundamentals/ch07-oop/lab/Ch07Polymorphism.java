// Ch07 — Lab 02: what "polymorphic" actually buys, and what it costs at runtime.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch07-oop/lab/Ch07Polymorphism.java
//
// Claims under test:
//   1. a call site has ONE type, but the executed method is chosen by the OBJECT's
//      runtime class — and that is what makes a list of mixed subclasses one loop
//   2. static methods and fields are chosen by the COMPILE-TIME type, so they are not
//      polymorphic, and overriding them is impossible
//   3. an interface method is virtual by default, a private interface method is not,
//      and an interface cannot have instance state
//   4. the cost of virtual dispatch is measurable and small — "no abstraction tax" is
//      nearly true, and "nearly" is the interesting part
import java.util.ArrayList;
import java.util.List;

public class Ch07Polymorphism {

    public static void main(String[] args) {
        oneLoopManyTypes();
        staticIsNotPolymorphic();
        whatAnInterfaceCannotDo();
        dispatchIsNotFree();
    }

    private static void oneLoopManyTypes() {
        System.out.println("--- one call site, three runtime types ---");

        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Circle(2));
        shapes.add(new Square(3));
        shapes.add(new Rect(2, 5));

        double total = 0;
        for (Shape s : shapes) {
            System.out.printf("  %-8s area() = %6.2f   (the loop does not know which one)%n",
                    s.getClass().getSimpleName(), s.area());
            total += s.area();
        }
        System.out.printf("  total = %.2f%n", total);
        System.out.println("  area() is declared once, on Shape. The `if (s instanceof Circle)");
        System.out.println("  ... else if ...` version is what you write when you do not have");
        System.out.println("  polymorphism, and it is O(types) instead of O(1) per element.");
    }

    private static void staticIsNotPolymorphic() {
        System.out.println();
        System.out.println("--- static methods and fields are chosen at compile time ---");

        Shape s = new Circle(2);
        Circle c = new Circle(5);
        System.out.printf("  Shape s = Circle;   Shape.describe()    -> %s%n", Shape.describe());
        System.out.printf("  Circle c = Circle; Circle.describe()   -> %s%n", Circle.describe());
        System.out.println("  the object is a Circle in both lines; the CALL SITE type decided.");
        System.out.printf("  the Circle object, reached through a Shape ref: s.describe() -> %s%n",
                s.describe());
        System.out.println("  (javac warns: 'static method should be qualified by type name, Shape,");
        System.out.println("   instead of by an expression'. The warning is correct and the fix is");
        System.out.println("   cosmetic — the result is the same either way, which is the point.)");
        System.out.println("  writing @Override on a static method that hides a static one is an");
        System.out.println("  ERROR ('static methods cannot be annotated with @Override'); dropping");
        System.out.println("  the annotation makes it legal hiding, not overriding.");

        System.out.println();
        System.out.println("  field hiding, which is the same mistake with different syntax:");
        System.out.printf("    Circle.PREFIX via Circle-typed ref = %s%n", c.PREFIX);
        System.out.printf("    Circle.PREFIX via Shape-typed  ref = %s%n", s.PREFIX);
        System.out.println("  same object, two different fields, chosen by the ref type.");
        System.out.println("  (Circle declares its own PREFIX, HIDING Shape's. The hiding");
        System.out.println("   itself is completely silent — no error, no warning. The two");
        System.out.println("   warnings javac DOES print are unrelated: they are about reading");
        System.out.println("   a static through an expression instead of via the type name.)");
    }

    private static void whatAnInterfaceCannotDo() {
        System.out.println();
        System.out.println("--- the interface/abstract-class split is a STATE split, not a syntax one ---");

        System.out.printf("  Circle has %d instance fields, an interface cannot have any.%n",
                countInstanceFields(new Circle(1)));
        System.out.println("  interface: only public abstract (or default/static/private) methods.");
        System.out.println("  abstract class: may also have state, constructors, and package-private");
        System.out.println("  methods, which is what lets it enforce an invariant across subclasses.");
        System.out.println();
        System.out.println("  and the modern rule of thumb, measured by what each one can hold:");
        System.out.println("    need shared STATE / constructor chaining  -> abstract class");
        System.out.println("    need only a CAPABILITY set               -> interface");
        System.out.println("    Java 21 lets an interface have private methods and static methods,");
        System.out.println("    so 'interfaces cannot have implementation' is now FALSE. What they");
        System.out.println("    still cannot have is instance state.");

        Greetable g = new Circle(1);
        System.out.println();
        System.out.printf("  private interface method, called from a default method: %s%n",
                g.greetTwice());
        System.out.println("  that is how you share code across implementors without exposing it,");
        System.out.println("  and it only became possible in Java 9.");
    }

    private static void dispatchIsNotFree() {
        System.out.println();
        System.out.println("--- how much does a virtual call cost? ---");
        System.out.println("  METHOD NOTE: the first attempt at this benchmark used Shape, where");
        System.out.println("  Triangle.area() calls Math.sqrt and Circle.area() does two");
        System.out.println("  multiplies. It reported ~5.8x, and I wrote that down as 'the cost of");
        System.out.println("  dispatch'. That was wrong: the shapes were not doing the same work,");
        System.out.println("  so the measurement was comparing a square root to a multiply.");
        System.out.println("  Below, the 4 types do IDENTICAL work, so the only variable left is");
        System.out.println("  WHICH method runs.");

        int n = 50_000_000;
        Work[] monoArr = {new WorkA(1), new WorkA(2), new WorkA(3), new WorkA(4)};
        Work[] polyArr = {new WorkA(1), new WorkB(2), new WorkC(3), new WorkD(4)};

        // Warm BOTH call sites before timing either one.
        double sink = 0;
        for (int i = 0; i < 2_000_000; i++) {
            sink += monoArr[i & 3].run();
            sink += polyArr[i & 3].run();
        }

        System.out.println("  measuring the POLYMORPHIC loop first, so it cannot be blamed on cold code:");
        long p1 = timePoly(polyArr, n);
        long m1 = timeMono(monoArr, n);
        System.out.printf("    monomorphic   %,8.1f ms%n", m1 / 1e6);
        System.out.printf("    polymorphic  %,8.1f ms   %.2fx%n", p1 / 1e6, (double) p1 / m1);

        System.out.println("  now the MONOMORPHIC loop first:");
        long m2 = timeMono(monoArr, n);
        long p2 = timePoly(polyArr, n);
        System.out.printf("    monomorphic   %,8.1f ms%n", m2 / 1e6);
        System.out.printf("    polymorphic  %,8.1f ms   %.2fx%n", p2 / 1e6, (double) p2 / m2);

        long bestMono = Math.min(m1, m2), bestPoly = Math.min(p1, p2);
        System.out.printf("%n  best-of-2: mono %.1f ms, poly %.1f ms -> %.2fx%n",
                bestMono / 1e6, bestPoly / 1e6, (double) bestPoly / bestMono);
        System.out.printf("  that is %.2f ns per call, on a loop that also does an add and a store%n",
                (bestPoly - bestMono) / (double) n);
        System.out.println("  -> a few NANOSECONDS. The loop is still O(n). That is the entire");
        System.out.println("     argument for OOP: nanoseconds to delete an O(types) if-else chain.");
        if (sink == Double.NEGATIVE_INFINITY) {
            System.out.println("unreachable");
        }
    }

    private static long timeMono(Work[] w, int n) {
        long t0 = System.nanoTime();
        double acc = 0;
        for (int i = 0; i < n; i++) {
            acc += w[i & 3].run();
        }
        long dt = System.nanoTime() - t0;
        if (acc < 0) {
            System.out.print("");
        }
        return dt;
    }

    private static long timePoly(Work[] w, int n) {
        long t0 = System.nanoTime();
        double acc = 0;
        for (int i = 0; i < n; i++) {
            acc += w[i & 3].run();
        }
        long dt = System.nanoTime() - t0;
        if (acc < 0) {
            System.out.print("");
        }
        return dt;
    }

    private static int countInstanceFields(Object o) {
        int count = 0;
        for (java.lang.reflect.Field f : o.getClass().getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                count++;
            }
        }
        return count;
    }

    // ---------- the hierarchy ----------

    static abstract class Shape implements Greetable {
        static final String PREFIX = "Shape";

        double area() {
            return 0;
        }

        @Override
        public String greet() {
            return "I am a " + getClass().getSimpleName();
        }

        static String describe() {
            return "Shape.describe (static)";
        }
    }

    static class Circle extends Shape {
        private final double r;
        static final String PREFIX = "Circle";

        Circle(double r) {
            this.r = r;
        }

        @Override
        double area() {
            return Math.PI * r * r;
        }

        static String describe() {
            return "Circle.describe (static, NOT an override)";
        }
    }

    static class Square extends Shape {
        private final double side;

        Square(double side) {
            this.side = side;
        }

        @Override
        double area() {
            return side * side;
        }
    }

    static class Rect extends Shape {
        private final double w, h;

        Rect(double w, double h) {
            this.w = w;
            this.h = h;
        }

        @Override
        double area() {
            return w * h;
        }
    }

    static class Triangle extends Shape {
        private final double a, b, c;

        Triangle(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        double area() {
            double s = (a + b + c) / 2;
            return Math.sqrt(s * (s - a) * (s - b) * (s - c));
        }
    }

    // Four types with IDENTICAL bodies, so the only difference between the two
    // benchmarks is which method the call site dispatches to.
    interface Work {
        double run();
    }

    static class WorkA implements Work {
        private final double k;

        WorkA(double k) {
            this.k = k;
        }

        @Override
        public double run() {
            return k * 2.0 + 1.0;
        }
    }

    static class WorkB implements Work {
        private final double k;

        WorkB(double k) {
            this.k = k;
        }

        @Override
        public double run() {
            return k * 2.0 + 1.0;
        }
    }

    static class WorkC implements Work {
        private final double k;

        WorkC(double k) {
            this.k = k;
        }

        @Override
        public double run() {
            return k * 2.0 + 1.0;
        }
    }

    static class WorkD implements Work {
        private final double k;

        WorkD(double k) {
            this.k = k;
        }

        @Override
        public double run() {
            return k * 2.0 + 1.0;
        }
    }

    interface Greetable {
        String greet();

        private String prefix() {                 // Java 9+
            return "shape";
        }

        default String greetTwice() {
            return prefix() + " " + prefix();
        }
    }
}

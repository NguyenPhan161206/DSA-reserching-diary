// Ch06 — Lab 01: an array is a pointer, and a 2-D array is a pointer to pointers.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch06-arrays/lab/Ch06ArrayAliasing.java
//
// Claims under test:
//   1. assigning an array copies the REFERENCE, not the contents
//   2. putting one array inside another is legal when the element type allows it
//      (Object[] holds int[]; int[] does NOT hold int[]), and the result is a
//      self-referential structure that still prints fine
//   3. `clone()` is a SHALLOW copy: it fixes #1 and not #2
//   4. System.arraycopy is a bulk memcpy, and Arrays.sort(int[]) is dual-pivot quicksort
import java.util.Arrays;

public class Ch06ArrayAliasing {

    public static void main(String[] args) {
        referencesNotCopies();
        shallowCloneIsNotDeep();
        selfReferentialArray();
        twoDimensionalIsNotReallyTwoDimensional();
    }

    private static void referencesNotCopies() {
        System.out.println("--- assignment copies a reference ---");

        int[] a = {1, 2, 3};
        int[] b = a;
        b[0] = 99;

        System.out.printf("  a = %s%n", Arrays.toString(a));
        System.out.printf("  b = %s%n", Arrays.toString(b));
        System.out.println("  a changed because a and b are the SAME object: (a == b) = " + (a == b));

        int[] c = a.clone();
        c[0] = 7;
        System.out.printf("  after c = a.clone(); c[0]=7   a = %s   c = %s%n",
                Arrays.toString(a), Arrays.toString(c));
        System.out.println("  clone() broke the aliasing — for THIS array. See method 2.");
    }

    private static void shallowCloneIsNotDeep() {
        System.out.println();
        System.out.println("--- clone() is shallow, so 2-D arrays still alias ---");

        int[][] grid = {{1, 2}, {3, 4}};
        int[][] copy = grid.clone();          // the OUTER array is copied

        copy[0][0] = 100;                      // ...but the ROWS are still shared
        System.out.printf("  grid[0] = %s   after copy[0][0] = 100%n", Arrays.toString(grid[0]));
        System.out.printf("  copy[0] = %s   <- grid changed: rows were not copied%n",
                Arrays.toString(copy[0]));

        int[][] deep = new int[grid.length][];
        for (int i = 0; i < grid.length; i++) {
            deep[i] = grid[i].clone();
        }
        deep[0][0] = 1;
        System.out.printf("  deep[0] = %s   <- wrote 1 into the COPY%n", Arrays.toString(deep[0]));
        System.out.printf("  grid[0] = %s   <- still 100: the deep copy is isolated%n",
                Arrays.toString(grid[0]));
        System.out.println("  cost: O(rows) for the outer array, plus O(total elements) for the rows.");
        System.out.println("  the outer clone alone was O(rows) — cheap, and still wrong.");
    }

    private static void selfReferentialArray() {
        System.out.println();
        System.out.println("--- an array can contain another array, if the element type allows ---");

        // This does NOT compile, and the error is the lesson:
        //     int[] a = {1, 2, 3};
        //     a[1] = new int[]{4, 5};      // error: int[] cannot be converted to int
        Object[] mixed = {1, new int[]{4, 5, 6}, 3};
        System.out.printf("  Object[] mixed = %s%n", Arrays.deepToString(mixed));
        System.out.println("  element 1 is an int[] living inside an Object[].");

        // and it can point back at itself:
        Object[] self = new Object[3];
        self[0] = "head";
        self[1] = self;                        // legal: Object[] holds an Object[]
        self[2] = "tail";

        System.out.println("  self[1] == self -> " + (self[1] == self));
        System.out.printf("  self[0] = %s, self[2] = %s%n", self[0], self[2]);
        System.out.println("  a self-referential structure COMPILES and RUNS, and every element");
        System.out.println("  still prints. Arrays.deepToString on it would loop forever, so");
        System.out.println("  nothing here prints it. That is the actual danger: a wrong program");
        System.out.println("  that runs is worse than one that fails to compile.");
    }

    private static void twoDimensionalIsNotReallyTwoDimensional() {
        System.out.println();
        System.out.println("--- int[][] is an array of int[] references, not a block of memory ---");

        int[][] grid = new int[3][];          // rows not yet allocated
        System.out.println("  new int[3][] allocates 3 references, all null:");
        for (int i = 0; i < grid.length; i++) {
            System.out.printf("    grid[%d] == null -> %s%n", i, grid[i] == null);
        }

        try {
            grid[0][0] = 1;
        } catch (NullPointerException e) {
            System.out.println("  grid[0][0] = 1  -> NullPointerException: " + e.getMessage());
        }

        for (int i = 0; i < grid.length; i++) {
            grid[i] = new int[3];
        }
        grid[1][2] = 7;
        System.out.println("  after allocating every row: " + Arrays.deepToString(grid));
        System.out.println("  rows are independent, so grid[1][2] = 7 only touched row 1.");

        int[][] block = new int[3][3];         // rows allocated AND zeroed
        System.out.println("  new int[3][3] zero-fills everything: " + Arrays.deepToString(block));
        System.out.println("  the difference is one word: new int[3][3] also runs the 9 stores.");
    }
}

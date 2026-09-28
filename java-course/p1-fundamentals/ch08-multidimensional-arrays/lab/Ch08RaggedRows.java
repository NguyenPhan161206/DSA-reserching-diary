// Ch08 — Lab 01: what int[][] really is, and what that forces.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch08-multidimensional-arrays/lab/Ch08RaggedRows.java
//
// Claims under test:
//   1. int[][] is an array OF int[] OBJECTS: the outer length and the inner lengths
//      are independent. Ragged rows are not a special case, they are the default.
//   2. a row that was never allocated is null, and touching it is a NullPointerException
//      raised at the exact line of the inner access — not "somewhere in the loop".
//   3. toString() and equals() on a 2-D array are useless, and deepToString/deepEquals
//      are the answer. The reason is claim 1, again.
import java.util.Arrays;

public class Ch08RaggedRows {

    public static void main(String[] args) {
        raggedRows();
        nullRows();
        stringAndEquals();
    }

    private static void raggedRows() {
        System.out.println("--- ragged rows are the default, not a special case ---");

        int[][] triangle = new int[5][];
        for (int i = 0; i < triangle.length; i++) {
            triangle[i] = new int[i + 1];      // row i has i+1 cells
        }
        int cell = 0;
        for (int i = 0; i < triangle.length; i++) {
            for (int j = 0; j < triangle[i].length; j++) {
                triangle[i][j] = ++cell;
            }
        }
        System.out.println("  5-row triangle, row lengths 1..5:");
        for (int[] row : triangle) {
            System.out.println("    " + Arrays.toString(row));
        }
        System.out.println("  the OUTER length is 5; the INNER lengths are 1,2,3,4,5.");
        System.out.println("  a[i].length is per-row, which is why summing a ragged matrix");
        System.out.println("  is a[i][j].length, not a[0].length, inside the loop.");
    }

    private static void nullRows() {
        System.out.println();
        System.out.println("--- an unallocated row is null, and it throws at the access ---");

        int[][] rows = new int[3][];
        rows[0] = new int[]{1, 2};
        rows[1] = new int[]{3, 4};
        // rows[2] is null — declared, but no array was ever created.

        try {
            System.out.println("  rows[2].length = " + rows[2].length);
        } catch (NullPointerException npe) {
            System.out.println("  rows[2].length -> NullPointerException: " + npe.getMessage());
        }
        System.out.println("  The message is 'Cannot read the array length because");
        System.out.println("  \"rows[2]\" is null'. The JVM tells you WHICH access was null.");
        System.out.println("  A 2-D array in Java is never a contiguous block, so a 'missing'");
        System.out.println("  row is exactly as legal as a missing pointer anywhere else.");
    }

    private static void stringAndEquals() {
        System.out.println();
        System.out.println("--- toString() and equals() tell the truth about the wrong level ---");

        int[][] a = {{1, 2, 3}, {4, 5, 6}};
        int[][] b = {{1, 2, 3}, {4, 5, 6}};

        System.out.println("  a.toString() = " + a);
        System.out.println("  a.equals(b)  = " + a.equals(b) + "   <- same outer refs? reference identity");
        System.out.println("  Arrays.deepToString(a) = " + Arrays.deepToString(a));
        System.out.println("  Arrays.deepEquals(a, b) = " + Arrays.deepEquals(a, b));
        System.out.println();
        System.out.println("  a.equals(b) calls Object.equals, which is reference identity on the");
        System.out.println("  OUTER array. Two outer arrays are never identical, so it is always");
        System.out.println("  false. deepEquals recurses into each row. Same reasoning as last");
        System.out.println("  chapter's equals/hashCode: equality must mean the same thing at the");
        System.out.println("  level the caller thinks about.");
    }
}
// Ch44 preview — the code under test. One obvious off-by-one at the boundary.
package dev.diary.toolchain;

public class Triangle {

    /**
     * Number of cells in a right triangle of {@code size} rows.
     * Contract: 1 + 2 + ... + size = size * (size + 1) / 2.
     */
    public static int area(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be >= 0, was " + size);
        }
        int total = 0;
        for (int row = 1; row <= size; row++) {   // FIXED: the boundary is inclusive
            total += row;
        }
        return total;
    }
}

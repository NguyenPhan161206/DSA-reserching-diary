// Ch44 preview — JUnit 5 structure, not library trivia.
//
// Run `mvn test` on the red version first: a test harness that has never failed is
// not evidence of anything. The captured red output is in ../README.md.
package dev.diary.toolchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TriangleTest {

    private Triangle triangle;

    @BeforeEach
    void setUp() {
        triangle = new Triangle();
    }

    @Test
    @DisplayName("area(3) == 1+2+3 == 6")
    void areaOfThreeRows() {
        assertEquals(6, triangle.area(3));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 10, 100})
    @DisplayName("area(n) matches the closed form on the happy path")
    void matchesClosedForm(int size) {
        assertEquals(size * (size + 1) / 2, triangle.area(size));
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "1, 1", "3, 6", "4, 10", "10, 55"})
    @DisplayName("explicit expected values, stated independently of the implementation")
    void explicitValues(int size, int expected) {
        assertEquals(expected, triangle.area(size));
    }

    @Test
    @DisplayName("negative size is rejected by the contract, not by chance")
    void negativeSizeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> triangle.area(-1));
    }
}

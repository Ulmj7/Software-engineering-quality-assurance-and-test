package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

@DisplayName("GradeCalculator нэгжийн тестүүд")
class GradeCalculatorTest {

    // ---------- letterGrade: хязгаарын утгууд ----------

    @Test
    @DisplayName("90 оноо яг A дүн байх ёстой (хязгаарын тохиолдол)")
    void ninetyIsExactlyA() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        String grade = calc.letterGrade(90.0);
        // Assert
        assertEquals("A", grade);
    }

    @Test
    @DisplayName("89.99 оноо A биш B дүн байх ёстой (90-ээс яг доор)")
    void eightyNinePointNinetyNineIsB() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        String grade = calc.letterGrade(89.99);
        // Assert
        assertEquals("B", grade);
    }

    @Test
    @DisplayName("60 оноо яг D дүн байх ёстой (D-ийн доод хязгаар)")
    void sixtyIsExactlyD() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        String grade = calc.letterGrade(60.0);
        // Assert
        assertEquals("D", grade);
    }

    @Test
    @DisplayName("59.99 оноо D биш F дүн байх ёстой (60-аас яг доор)")
    void fiftyNinePointNinetyNineIsF() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        String grade = calc.letterGrade(59.99);
        // Assert
        assertEquals("F", grade);
    }

    @Test
    @DisplayName("0 оноо хүчинтэй бөгөөд F дүн байх ёстой (доод хязгаар)")
    void zeroIsValidAndF() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        String grade = calc.letterGrade(0.0);
        // Assert
        assertEquals("F", grade);
    }

    @Test
    @DisplayName("100 оноо хүчинтэй бөгөөд A дүн байх ёстой (дээд хязгаар)")
    void hundredIsValidAndA() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        String grade = calc.letterGrade(100.0);
        // Assert
        assertEquals("A", grade);
    }

    // ---------- letterGrade: ердийн утгууд ----------

    @Test
    @DisplayName("Ердийн оноонууд зөв дүнтэй харгалзана: 95→A, 85→B, 75→C, 65→D, 30→F")
    void typicalScoresMapToExpectedLetters() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        String g95 = calc.letterGrade(95);
        String g85 = calc.letterGrade(85);
        String g75 = calc.letterGrade(75);
        String g65 = calc.letterGrade(65);
        String g30 = calc.letterGrade(30);
        // Assert
        assertAll(
                () -> assertEquals("A", g95),
                () -> assertEquals("B", g85),
                () -> assertEquals("C", g75),
                () -> assertEquals("D", g65),
                () -> assertEquals("F", g30));
    }

    // ---------- letterGrade: буруу оролт ----------

    @Test
    @DisplayName("-1 оноо буруу оролт тул IllegalArgumentException шидэх ёстой")
    void negativeScoreIsRejected() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        Executable act = () -> calc.letterGrade(-1);
        // Assert
        assertThrows(IllegalArgumentException.class, act);
    }

    @Test
    @DisplayName("101 оноо буруу оролт тул IllegalArgumentException шидэх ёстой")
    void scoreAbove100IsRejected() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        Executable act = () -> calc.letterGrade(101);
        // Assert
        assertThrows(IllegalArgumentException.class, act);
    }

    // ---------- totalScore ----------

    @Test
    @DisplayName("Бүх хэсгийн дээд оноогоор нийлбэр 100 гарах ёстой (10+40+10+10+30)")
    void totalScoreOfMaximumPartsIs100() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        double total = calc.totalScore(10, 40, 10, 10, 30);
        // Assert
        assertEquals(100.0, total);
    }

    @Test
    @DisplayName("Ирц сөрөг (att = -5) бол totalScore IllegalArgumentException шидэх ёстой")
    void negativeAttendanceIsRejected() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        Executable act = () -> calc.totalScore(-5, 40, 10, 10, 30);
        // Assert
        assertThrows(IllegalArgumentException.class, act);
    }

    @Test
    @DisplayName("Лаб дээд хязгаараасаа хэтэрвэл (lab = 41) totalScore IllegalArgumentException шидэх ёстой")
    void labAboveMaximumIsRejected() {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        Executable act = () -> calc.totalScore(10, 41, 10, 10, 30);
        // Assert
        assertThrows(IllegalArgumentException.class, act);
    }
}

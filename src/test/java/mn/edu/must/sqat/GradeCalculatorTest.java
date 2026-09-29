package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    // ---------- Parameterized тестүүд ----------

    @ParameterizedTest(name = "{0} оноо → {1}")
    @DisplayName("letterGrade: ердийн болон хязгаарын оноо бүр зөв дүнтэй харгалзах ёстой")
    @CsvSource({
            "100,A", "95,A", "90,A",
            "89.99,B", "85,B", "80,B",
            "79.99,C", "75,C", "70,C",
            "69.99,D", "65,D", "60,D",
            "59.99,F", "30,F", "0,F"})
    void letterGradeBoundaries(double score, String expected) {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        String grade = calc.letterGrade(score);
        // Assert
        assertEquals(expected, grade);
    }

    @ParameterizedTest(name = "{0} оноо татгалзагдана")
    @DisplayName("letterGrade: 0-100-аас гадуурх (NaN, Infinity орно) оноо бүр IllegalArgumentException шидэх ёстой")
    @ValueSource(doubles = {-1, -0.01, 100.01, 101, Double.NaN, Double.POSITIVE_INFINITY})
    void letterGradeRejectsInvalidScores(double score) {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        Executable act = () -> calc.letterGrade(score);
        // Assert
        assertThrows(IllegalArgumentException.class, act);
    }

    @ParameterizedTest(name = "{0}+{1}+{2}+{3}+{4} = {5}")
    @DisplayName("totalScore: хязгаар доторх хэсгүүдийн нийлбэр зөв гарах ёстой")
    @CsvSource({
            "10,40,10,10,30,100",
            "0,0,0,0,0,0",
            "5,20,5,5,15,50",
            "9,35.5,8,7.5,25,85"})
    void totalScoreSumsValidParts(double att, double lab, double quiz1, double quiz2, double exam, double expected) {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        double total = calc.totalScore(att, lab, quiz1, quiz2, exam);
        // Assert
        assertEquals(expected, total, 1e-9);
    }

    @ParameterizedTest(name = "totalScore({0}, {1}, {2}, {3}, {4}) татгалзагдана")
    @DisplayName("totalScore: аль нэг хэсэг сөрөг эсвэл дээд хязгаараасаа хэтэрвэл IllegalArgumentException шидэх ёстой")
    @CsvSource({
            "-5,0,0,0,0", "0,-1,0,0,0", "0,0,-1,0,0", "0,0,0,-1,0", "0,0,0,0,-1",
            "10.01,0,0,0,0", "0,40.01,0,0,0", "0,0,10.01,0,0", "0,0,0,10.01,0", "0,0,0,0,30.01"})
    void totalScoreRejectsAnyPartOutOfRange(double att, double lab, double quiz1, double quiz2, double exam) {
        // Arrange
        GradeCalculator calc = new GradeCalculator();
        // Act
        Executable act = () -> calc.totalScore(att, lab, quiz1, quiz2, exam);
        // Assert
        assertThrows(IllegalArgumentException.class, act);
    }
}

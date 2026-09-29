package mn.edu.must.sqat;

public class GradeCalculator {

    // 90+ -> A, 80-89 -> B, 70-79 -> C, 60-69 -> D, <60 -> F
    // score нь 0-100 хязгаараас гарвал IllegalArgumentException шиднэ
    public String letterGrade(double score) {
        requireInRange("score", score, 100);
        if (score > 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    // Ирц(10), лаб+бие даалт(40), сорил1(10), сорил2(10), шалгалт(30)-ийн оноонуудаас нийлбэр оноог тооцно.
    // Аль нэг нь сөрөг эсвэл дээд хязгаараасаа хэтэрсэн бол IllegalArgumentException шиднэ.
    public double totalScore(double att, double lab, double quiz1, double quiz2, double exam) {
        requireInRange("att", att, 10);
        requireInRange("lab", lab, 40);
        requireInRange("quiz1", quiz1, 10);
        requireInRange("quiz2", quiz2, 10);
        requireInRange("exam", exam, 30);
        return att + lab + quiz1 + quiz2 + exam;
    }

    // 0 <= value <= max. Нөхцөлийг урвуугаар бичсэн тул NaN-ийг ч татгалзана (NaN-тай бүх харьцуулалт false).
    private static void requireInRange(String name, double value, double max) {
        if (!(value >= 0 && value <= max)) {
            throw new IllegalArgumentException(name + " must be in [0, " + max + "], got " + value);
        }
    }
}

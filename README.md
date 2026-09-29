# Лаборатори №4: Нэгжийн тестийн эхлэл — JUnit 5

**Оюутны нэр:** Г. Үлэмж
**Оюутны код:** B232270805
**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)

## Хувилбарууд

Ажлын орчин: Windows 11 дээрх WSL2 (Ubuntu).

`java -version`:

```
openjdk version "17.0.20.1" 2026-08-18
OpenJDK Runtime Environment (build 17.0.20.1+1-1-26.04-Ubuntu)
OpenJDK 64-Bit Server VM (build 17.0.20.1+1-1-26.04-Ubuntu, mixed mode, sharing)
```

`mvn -version`:

```
Apache Maven 3.9.12
Maven home: /usr/share/maven
Java version: 17.0.20.1, vendor: Ubuntu, runtime: /usr/lib/jvm/java-17-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.6.114.1-microsoft-standard-wsl2", arch: "amd64", family: "unix"
```

## Үр дүн

- Тестийн методын тоо: **16** (`@Test` 12 + `@ParameterizedTest` 4)
- `results/mvn-test.txt`-ийн `Tests run`: **47** — `Tests run: 47, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`
- Мутаци (`score >= 90` → `score > 90`), `results/mvn-test-mutant.txt`: `Tests run: 47, Failures: 2, Errors: 0, Skipped: 0`, `BUILD FAILURE`.
  Унасан тестүүд: `ninetyIsExactlyA` ба `letterGradeBoundaries`-ийн `90,A` мөр (`[3]`) — хоёулаа `expected: <A> but was: <B>`.

Ажиллуулах: `mvn test`

## Тайлбар

`GradeCalculator`-ийн `letterGrade`, `totalScore`-ыг хэрэгжүүлж 16 тестийн метод (12 `@Test` + 4 `@ParameterizedTest`) бичсэн; Surefire параметрийн мөр бүрийг тусад нь тоодог тул `results/mvn-test.txt`-д `Tests run: 47, Failures: 0, Errors: 0, Skipped: 0` ба `BUILD SUCCESS` гарсан.
Мутацид `letterGrade` доторх `score >= 90`-ийг `score > 90` болгоход 47 тестээс 2 нь унасан (`Failures: 2`, `BUILD FAILURE`): `ninetyIsExactlyA` ба `letterGradeBoundaries`-ийн `90,A` мөр, хоёулаа `expected: <A> but was: <B>` мэдээлэлтэй.
Үлдсэн 45 тест мутанттай үед ч давсан, тэдгээрийн дотор 95→A, 100→A зэрэг утгууд байгаа — учир нь эдгээр нь `>` ба `>=` хоёрт ижил хариу өгдөг.
Хамгийн сонирхолтой нь яг энэ юм: зөвхөн яг 90 оноог шалгасан тест л алдааг илрүүлсэн тул «ердийн утгуудаар бүх тест ногоон» гэдэг нь хязгаар зөв гэсэн нотолгоо биш.
Дараа нь `>= 90`-ийг буцааж засаад `mvn test`-ийг дахин ажиллуулахад бүх тест дахин ногоон болсон.
Мөн шалгалтыг `!(value >= 0 && value <= max)` хэлбэрээр бичсэн тул `NaN` буруу оролт гэж татгалзагддаг — `value < 0 || value > max` гэж бичвэл `NaN` шалгалтаас гарч `letterGrade(NaN)` нь «F» буцаах байсан тул `@ValueSource`-д `NaN`-ийг тусгайлан оруулсан.
`totalScore`-д хэсэг бүрийг сөрөг болон дээд хязгаараас яг дээш (`10.01`, `40.01`, `30.01`) утгаар тус тусад нь шалгасан тул аль нэг хэсгийн хязгаар буруу бичигдвэл тест унана.

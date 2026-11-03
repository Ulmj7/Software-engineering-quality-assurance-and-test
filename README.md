# Lab 08: Mockito mock objects

Student name: **Г. Үлэмж**

Student code: **B232270805**

This project tests `CourseRegistrationService` with JUnit 5 and Mockito. The repository root contains `pom.xml`, `src/main`, `src/test`, and `results`; run Maven from this directory. No database or email server is required.

## Environment

The recorded runs used Eclipse Temurin JDK 17 and Apache Maven 3.9.16, unpacked outside the repository. The machine initially had Android Studio's JBR 25 on its path and no Maven executable; the outputs below are from the environment actually used for every saved test run. The compiler plugin is explicitly pinned to 3.13.0 so Java 17's `release` setting also works with Maven installations whose default compiler plugin is older.

Actual `java -version` output:

```text
openjdk version "17.0.21" 2026-10-20
OpenJDK Runtime Environment Temurin-17.0.21+9 (build 17.0.21+9)
OpenJDK 64-Bit Server VM Temurin-17.0.21+9 (build 17.0.21+9, mixed mode, sharing)
```

Actual `mvn -version` output:

```text
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: /tmp/lab8-tools/apache-maven-3.9.16
Java version: 17.0.21, vendor: Eclipse Adoptium, runtime: /tmp/lab8-tools/jdk-17.0.21+9
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "7.2.11-arch1-1", arch: "amd64", family: "unix"
```

With JDK 17 and Maven available on the path:

```sh
mvn test
```

## Part A: eight test requirements, ten executed cases

The test class uses `@ExtendWith(MockitoExtension.class)`, two `@Mock` collaborators, and `@BeforeEach` constructor injection. `CourseRegistrationService`, `Student` records, and course lists are real objects.

| Requirement | Test method | Checked behavior and Mockito tools |
| --- | --- | --- |
| A1 | `register_activeStudent_returnsOkAndSavesRegistrationAndSendsEmail` | A student who completed the prerequisite receives `OK`; `when`/`thenReturn` supplies repository answers, and `verify` checks saving and the confirmation email. |
| A2 | `register_missingStudent_returnsNoStudentWithoutSavingOrEmail` | A missing student receives `ERROR_NO_STUDENT`; `never()` checks that neither saving nor email sending occurs. |
| A3 | `register_inactiveStudent_returnsInactiveWithoutSavingOrEmail` | An inactive student receives `ERROR_INACTIVE_STUDENT`; saving never occurs and `verifyNoInteractions` checks the email sender. |
| A4 | `register_missingPrerequisite_returnsPrerequisitesWithoutSavingOrEmail` | A missing completed prerequisite produces `ERROR_PREREQUISITES`; no registration is saved and no email interaction occurs. |
| A5 | `register_emailThrows_returnsOkAndSavesRegistration` | `doThrow` simulates an email failure; registration still returns `OK`, is saved, and attempts the email. |
| A6 | `register_eitherArgumentNull_throwsIllegalArgumentWithoutCollaboratorInteractions` | Each argument is separately null while the other is valid; each call throws `IllegalArgumentException` and leaves both collaborators untouched. |
| A7 | `register_successfulRegistration_emailBodyContainsCourseId` | Registration returns `OK`; `ArgumentCaptor<String>` captures the email body and an assertion checks that it contains `CS201`. |
| A8 | `register_invalidStudentOrCourse_returnsExpectedErrorWithoutSavingOrEmail` | One `@ParameterizedTest` with three `@CsvSource` rows checks missing student, inactive student, and missing course; every row checks its error result and absence of saving/email. |

A8's rows are `(studentExists, active, courseExists, expectedResult)`:

```text
false, true,  true,  ERROR_NO_STUDENT
true,  false, true,  ERROR_INACTIVE_STUDENT
true,  true,  false, ERROR_NO_COURSE
```

Only A8's `courseExists` stub uses `lenient()` because the missing-student and inactive-student rows return before that stub is used. The student lookup and all other stubs remain strict, preserving Mockito's unused-stub checks elsewhere.

A6 contains both `(null, "CS201")` and `("S001", null)` in one regular test method, with a separate `assertThrows` and `verifyNoInteractions(repository, emailSender)` for each call. Both checks were present before the mutation runs, so removing only the course-null guard was detected without a later test repair.

The exact final total is **10 executed test cases: seven regular tests plus three A8 invocations**. [results/mvn-test.txt](results/mvn-test.txt) records Maven exit status 0, `BUILD SUCCESS`, and:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```

## Part B: three independent mutations

Each change was applied separately to `CourseRegistrationService.java` and restored in a `finally` block before the next experiment. A clean build passed after every restoration, and SHA-256 comparisons confirmed that all four `src/main` files match their original bytes. The final saved green run was generated after the third restoration.

Each mutant run executed 10 cases, returned Maven exit status 1, and recorded `BUILD FAILURE`. The method and invocation names below match the saved results.

| Mutant | Temporary change | Failing methods or invocations | Failures / Errors / Skipped | Failure classification and evidence |
| --- | --- | --- | --- | --- |
| 1 | Remove `if (!student.active()) return "ERROR_INACTIVE_STUDENT";` | `register_inactiveStudent_returnsInactiveWithoutSavingOrEmail`; `register_invalidStudentOrCourse_returnsExpectedErrorWithoutSavingOrEmail(boolean, boolean, boolean, String)[2]` (inactive-student row) | 2 / 0 / 0 | JUnit result assertion failures; [mvn-test-mutant-1.txt](results/mvn-test-mutant-1.txt). |
| 2 | Remove `repository.saveRegistration(studentId, courseId);` | `register_emailThrows_returnsOkAndSavesRegistration`; `register_activeStudent_returnsOkAndSavesRegistrationAndSendsEmail` | 2 / 0 / 0 | Mockito verification assertion failures (`Wanted but not invoked`); [mvn-test-mutant-2.txt](results/mvn-test-mutant-2.txt). |
| 3 | Remove only `|| courseId == null` from the initial null check | `register_eitherArgumentNull_throwsIllegalArgumentWithoutCollaboratorInteractions` | 1 / 0 / 0 | JUnit exception assertion failure for the valid student ID with a null course ID; [mvn-test-mutant-3.txt](results/mvn-test-mutant-3.txt). |

There were no `UnnecessaryStubbingException` errors in these recorded runs: every detected problem was an assertion or verification failure, and all three error counts are zero. Each result file preserves the full combined Maven stdout/stderr, including Maven-generated whitespace, followed by the command and its captured exit status.

All three specified mutations were detected independently, demonstrating coverage of the inactive-student guard, required persistence, and the course-null guard. Restoring the original service returned the suite to 10 passing cases, so the recorded failures are attributable to the injected changes.

## Conclusion

Mockito mocks replaced `StudentRepository` and `EmailSender`, allowing the service's rules to run without a database or SMTP server. A mock supplies configured answers and records interactions, whereas a handwritten fake implements a small working collaborator with its own behavior and state. Mocks made individual error branches and email exceptions easy to isolate here, while a fake can be useful when several tests need realistic stateful behavior. The service under test and simple values such as `Student` records and lists should remain real so the tests exercise actual business logic. Assertions check returned statuses, exceptions, and email content, while verification checks consequential saves and messages. Verifying incidental calls or exact internal call order too aggressively makes tests brittle when valid refactoring changes those details. The three detected mutants and the final green run provide evidence for these specific behaviors in the recorded environment.

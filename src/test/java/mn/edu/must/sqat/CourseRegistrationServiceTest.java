package mn.edu.must.sqat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    private static final String STUDENT_ID = "S001";
    private static final String STUDENT_EMAIL = "student@example.com";
    private static final String COURSE_ID = "CS201";

    @Mock
    private StudentRepository repository;

    @Mock
    private EmailSender emailSender;

    private CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    @Test
    void register_activeStudent_returnsOkAndSavesRegistrationAndSendsEmail() {
        stubSuccessfulRegistration();

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(STUDENT_EMAIL), eq("Бүртгэл баталгаажлаа"), contains(COURSE_ID));
    }

    @Test
    void register_missingStudent_returnsNoStudentWithoutSavingOrEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void register_inactiveStudent_returnsInactiveWithoutSavingOrEmail() {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(new Student(STUDENT_ID, STUDENT_EMAIL, false, List.of("CS101")));

        assertEquals("ERROR_INACTIVE_STUDENT", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void register_missingPrerequisite_returnsPrerequisitesWithoutSavingOrEmail() {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(new Student(STUDENT_ID, STUDENT_EMAIL, true, List.of()));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS101"));

        assertEquals("ERROR_PREREQUISITES", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void register_emailThrows_returnsOkAndSavesRegistration() {
        stubSuccessfulRegistration();
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailSender).send(eq(STUDENT_EMAIL), anyString(), anyString());

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(STUDENT_EMAIL), anyString(), anyString());
    }

    @Test
    void register_eitherArgumentNull_throwsIllegalArgumentWithoutCollaboratorInteractions() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, COURSE_ID));
        verifyNoInteractions(repository, emailSender);

        assertThrows(IllegalArgumentException.class, () -> service.register(STUDENT_ID, null));
        verifyNoInteractions(repository, emailSender);
    }

    @Test
    void register_successfulRegistration_emailBodyContainsCourseId() {
        stubSuccessfulRegistration();
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(emailSender).send(eq(STUDENT_EMAIL), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID));
    }

    @ParameterizedTest(name = "{index}: studentExists={0}, active={1}, courseExists={2} -> {3}")
    @CsvSource({
            "false, true, true, ERROR_NO_STUDENT",
            "true, false, true, ERROR_INACTIVE_STUDENT",
            "true, true, false, ERROR_NO_COURSE"
    })
    void register_invalidStudentOrCourse_returnsExpectedErrorWithoutSavingOrEmail(
            boolean studentExists, boolean active, boolean courseExists, String expectedResult) {
        Student student = studentExists
                ? new Student(STUDENT_ID, STUDENT_EMAIL, active, List.of("CS101"))
                : null;
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        lenient().when(repository.courseExists(COURSE_ID)).thenReturn(courseExists);

        assertEquals(expectedResult, service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    private void stubSuccessfulRegistration() {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(new Student(STUDENT_ID, STUDENT_EMAIL, true, List.of("CS101")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS101"));
    }
}

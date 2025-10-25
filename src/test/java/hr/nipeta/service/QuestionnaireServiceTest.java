package hr.nipeta.service;

import hr.nipeta.model.Questionnaire;
import hr.nipeta.model.Question;
import hr.nipeta.model.Option;
import hr.nipeta.model.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionnaireServiceTest {

    private QuestionnaireService service;

    @BeforeEach
    public void setUp() {
        service = new QuestionnaireService();
    }

    @Test
    public void testLoadJsonFromResources() {

        Questionnaire q = invokeLoadJson("/questionnaires/test-all.json");
        assertNotNull(q, "Questionnaire should be parsed from JSON");
        assertEquals("test-all", q.getId());
        assertNotNull(q.getQuestions());
        assertFalse(q.getQuestions().isEmpty());

    }

    @Test
    public void testQuestionTypeMulti() {

        Questionnaire q = invokeLoadJson("/questionnaires/test-q-type-multi.json");
        assertNotNull(q, "Questionnaire should be parsed from JSON");
        assertEquals("test-q-type-multi", q.getId());

        List<Question> questions = q.getQuestions();
        assertNotNull(questions);
        assertFalse(questions.isEmpty());
        assertEquals(1, questions.size());

        Question question = questions.get(0);
        assertNotNull(question.getId());
        assertNotNull(question.getType());
        assertEquals(question.getType(), QuestionType.multi);

        List<Option> options = question.getOptions();
        assertNotNull(options);
        assertFalse(options.isEmpty());

    }

    @Test
    public void testGetAllCachesResults() {
        Map<String, Questionnaire> first = service.getAll();
        assertFalse(first.isEmpty());
        assertSame(first, service.getAll(), "Subsequent calls should return cached map");
    }

    @Test
    public void testByIdReturnsCorrectQuestionnaire() {
        Map<String, Questionnaire> all = service.getAll();
        Questionnaire q = service.byId("llm-current");
        assertNotNull(q);
        assertEquals("llm-current", q.getId());
        assertTrue(all.containsKey("llm-current"));
    }

    @Test
    public void testInvalidPathReturnsNull() {
        Questionnaire q = invokeLoadJson("/questionnaires/nonexistent.json");
        assertNull(q, "Nonexistent file should return null");
    }

    // Helper to access the private loadJson method via reflection
    private Questionnaire invokeLoadJson(String path) {
        try {
            Method method = QuestionnaireService.class.getDeclaredMethod("loadJson", String.class);
            method.setAccessible(true);
            return (Questionnaire) method.invoke(service, path);
        } catch (Exception e) {
            fail("Failed to invoke loadJson: " + e.getMessage());
            return null;
        }
    }
}
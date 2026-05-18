package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Game;

public class QuestionServiceTest {

    @Test
    public void getQuestionById_returnsCorrectQuestion() {
        QuestionService qs = new QuestionService();
        Map<String, Object> q = qs.getQuestionById(1L);
        assertNotNull(q);
        assertEquals("1", q.get("id").toString());
        assertTrue(q.get("question").toString().length() > 10);
        assertEquals("McDonald's", q.get("answer"));
    }

    @Test
    public void getRandomQuestion_addsUsedId() {
        QuestionService qs = new QuestionService();
        Game game = new Game();

        Map<String, Object> q = qs.getRandomQuestion(game);
        assertNotNull(q);
        Long id = Long.valueOf(q.get("id").toString());
        assertTrue(game.getUsedQuestionIds().contains(id));
        assertEquals(1, game.getUsedQuestionIds().size());
    }

    @Test
    public void getRandomQuestion_throwsWhenNoQuestions() throws Exception {
        QuestionService qs = new QuestionService();
        Game game = new Game();

        // load questions.json to know how many questions exist
        org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("questions.json");
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
        java.util.List<java.util.Map<String, Object>> questions = mapper.readValue(resource.getInputStream(), new tools.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {});

        int total = questions.size();

        for (int i = 0; i <= total; i++) {
            try {
                qs.getRandomQuestion(game);
            } catch (ResponseStatusException ex) {
                assertEquals(409, ex.getStatusCode().value());
                return;
            }
        }

        fail("Expected ResponseStatusException when no questions available");
    }
}

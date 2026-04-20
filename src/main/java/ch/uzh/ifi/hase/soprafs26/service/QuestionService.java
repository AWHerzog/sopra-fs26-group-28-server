package ch.uzh.ifi.hase.soprafs26.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class QuestionService {

    private List<Map<String, Object>> questions;

    public QuestionService() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("questions.json");
            questions = mapper.readValue(resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load questions.json", e);
        }
    }

    // Get a question by id
    public Map<String, Object> getQuestionById(Long id) {
        return questions.stream()
                .filter(q -> Long.valueOf(q.get("id").toString()).equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Question not found: " + id));
    }

    // Pick a random question that hasn't been used in this game yet
    public Map<String, Object> getRandomQuestion(Game game) {
        List<Map<String, Object>> available = questions.stream()
                .filter(q -> !game.getUsedQuestionIds().contains(
                        Long.valueOf(q.get("id").toString())))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No more questions available");
        }

        Map<String, Object> question = available.get(new Random().nextInt(available.size()));
        game.getUsedQuestionIds().add(Long.valueOf(question.get("id").toString()));
        return question;
    }
}
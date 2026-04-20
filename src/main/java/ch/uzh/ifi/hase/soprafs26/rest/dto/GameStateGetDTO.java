package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for retrieving comprehensive game state.
 * Exposes current round, stage, deadline, submitted flags, scores, and other metadata for frontend rendering.
 */
public class GameStateGetDTO {

    public static class QuestionDTO {
        private Long id;
        private String text;
        private String category;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class AnswerDTO {
        private Long id;
        private String text;
        private String authorUsername;
        private List<String> voters = new ArrayList<>();
        private Boolean isCorrect = false;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getAuthorUsername() { return authorUsername; }
        public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
        public List<String> getVoters() { return voters; }
        public void setVoters(List<String> voters) { this.voters = voters; }
        public Boolean getIsCorrect() { return isCorrect; }
        public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    }

    private Long id;
    private String code;
    private String hostname;
    private GameStatus status;
    private GameStatus stage;
    private Integer currentRound;
    private Integer maxRounds;
    private Long currentQuestionId;
    private LocalDateTime stageDeadline;
    private Boolean answerSubmitted;
    private Boolean voteSubmitted;
    private Map<String, Integer> players = new HashMap<>();
    private Map<String, Integer> scores = new HashMap<>();
    private QuestionDTO question;
    private List<AnswerDTO> answers = new ArrayList<>();
    private List<String> submittedUsernames = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameStatus getStage() {
        return stage;
    }

    public void setStage(GameStatus stage) {
        this.stage = stage;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public Integer getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(Integer maxRounds) {
        this.maxRounds = maxRounds;
    }

    public Long getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(Long currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }

    public LocalDateTime getStageDeadline() {
        return stageDeadline;
    }

    public void setStageDeadline(LocalDateTime stageDeadline) {
        this.stageDeadline = stageDeadline;
    }

    public Boolean getAnswerSubmitted() {
        return answerSubmitted;
    }

    public void setAnswerSubmitted(Boolean answerSubmitted) {
        this.answerSubmitted = answerSubmitted;
    }

    public Boolean getVoteSubmitted() {
        return voteSubmitted;
    }

    public void setVoteSubmitted(Boolean voteSubmitted) {
        this.voteSubmitted = voteSubmitted;
    }

    public Map<String, Integer> getPlayers() {
        return players;
    }

    public void setPlayers(Map<String, Integer> players) {
        this.players = players;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public QuestionDTO getQuestion() { return question; }
    public void setQuestion(QuestionDTO question) { this.question = question; }

    public List<AnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDTO> answers) { this.answers = answers; }

    public List<String> getSubmittedUsernames() { return submittedUsernames; }
    public void setSubmittedUsernames(List<String> submittedUsernames) { this.submittedUsernames = submittedUsernames; }
}

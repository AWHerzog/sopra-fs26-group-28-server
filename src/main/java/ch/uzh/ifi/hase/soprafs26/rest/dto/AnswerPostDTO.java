package ch.uzh.ifi.hase.soprafs26.rest.dto;

import tools.jackson.annotation.JsonProperty;

/**
 * DTO for submitting an answer during the answering stage.
 * Contains the user's answer payload for a specific round question.
 */
public class AnswerPostDTO {
    @JsonProperty("questionId")
    private Long questionId;

    @JsonProperty("answerText")
    private String answerText;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}

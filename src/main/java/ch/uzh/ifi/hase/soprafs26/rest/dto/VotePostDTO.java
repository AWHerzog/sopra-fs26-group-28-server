package ch.uzh.ifi.hase.soprafs26.rest.dto;

import tools.jackson.annotation.JsonProperty;

/**
 * DTO for submitting a vote during the voting stage.
 * Contains the user's vote on a specific answer/user combination.
 */
public class VotePostDTO {
    @JsonProperty("answerId")
    private Long answerId;

    @JsonProperty("votedUserId")
    private Long votedUserId;

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getVotedUserId() {
        return votedUserId;
    }

    public void setVotedUserId(Long votedUserId) {
        this.votedUserId = votedUserId;
    }
}

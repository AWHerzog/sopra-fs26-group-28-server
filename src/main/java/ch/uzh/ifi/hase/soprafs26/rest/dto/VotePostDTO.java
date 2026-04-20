package ch.uzh.ifi.hase.soprafs26.rest.dto;

/**
 * DTO for submitting a vote during the voting stage.
 * Contains the user's vote on a specific answer/user combination.
 */
public class VotePostDTO {
    private Long answerId;
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

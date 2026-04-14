package ch.uzh.ifi.hase.soprafs26.rest.dto;

/**
 * DTO for game start requests.
 * Contains payload required to initiate a game and transition from waiting to answering round.
 */
public class GameStartPostDTO {
    private Integer maxRounds;
    private Integer stageDurationSeconds;

    public Integer getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(Integer maxRounds) {
        this.maxRounds = maxRounds;
    }

    public Integer getStageDurationSeconds() {
        return stageDurationSeconds;
    }

    public void setStageDurationSeconds(Integer stageDurationSeconds) {
        this.stageDurationSeconds = stageDurationSeconds;
    }
}

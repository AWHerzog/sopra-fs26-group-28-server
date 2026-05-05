package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class LeaderboardEntryDTO {

    private int rank;
    private String username;
    private int points;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}

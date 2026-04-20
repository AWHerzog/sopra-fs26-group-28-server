package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Answer represents a player's answer submission during the ANSWERING phase.
 * 
 * Constraints:
 * - Exactly one answer per user per round (enforced via UNIQUE(round_id, user_id))
 * - Each answer belongs to one round and one user
 * 
 * Relationships:
 * - One Answer belongs to one Round (round_id FK)
 * - One Answer is created by one User (user_id FK)
 * - One Answer can receive multiple Votes
 */
@Entity
@Table(
    name = "answer",
    uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "user_id"})
)
public class Answer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long roundId;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean isCorrect = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Boolean getIsCorrect() {
    return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

}

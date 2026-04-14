package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Vote represents a player's vote during the VOTING phase.
 * Each vote is cast on an answer submitted by another player.
 * 
 * Constraints:
 * - Exactly one vote per user per round (enforced via UNIQUE(round_id, voter_id))
 * - voter_id must not equal answer.user_id (optional: enforced in service layer)
 * 
 * Relationships:
 * - One Vote belongs to one Round (round_id FK)
 * - One Vote is cast by one User (voter_id FK)
 * - One Vote targets one Answer (answer_id FK)
 */
@Entity
@Table(
    name = "vote",
    uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "voter_id"})
)
public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long roundId;

    @Column(nullable = false)
    private Long voterId;

    @Column(nullable = false)
    private Long answerId;

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

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

    public Long getVoterId() {
        return voterId;
    }

    public void setVoterId(Long voterId) {
        this.voterId = voterId;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}

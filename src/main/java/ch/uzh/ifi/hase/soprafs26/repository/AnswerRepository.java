package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.Answer;
import java.util.List;
import java.util.Optional;

/**
 * Data access layer for Answer entities.
 * Provides persistence operations for round answers with uniqueness constraints
 * enforced at the database level (UNIQUE(round_id, user_id)).
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    /**
     * Find a specific answer by round and user.
     * This enforces validation: check if user already answered in this round.
     */
    Optional<Answer> findByRoundIdAndUserId(Long roundId, Long userId);
    
    /**
     * Find all answers for a specific round.
     */
    List<Answer> findByRoundId(Long roundId);
    
    /**
     * Check if a user has already answered in a round.
     */
    boolean existsByRoundIdAndUserId(Long roundId, Long userId);
    
    /**
     * Count answers submitted for a round.
     */
    long countByRoundId(Long roundId);
}

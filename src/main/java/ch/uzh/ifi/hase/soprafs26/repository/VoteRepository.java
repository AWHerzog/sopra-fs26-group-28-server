package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.Vote;
import java.util.List;
import java.util.Optional;

/**
 * Data access layer for Vote entities.
 * Provides persistence operations for round votes with uniqueness constraints
 * enforced at the database level (UNIQUE(round_id, voter_id)).
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    /**
     * Find a specific vote by round and voter.
     * This enforces validation: check if user already voted in this round.
     */
    Optional<Vote> findByRoundIdAndVoterId(Long roundId, Long voterId);
    
    /**
     * Find all votes for a specific round.
     */
    List<Vote> findByRoundId(Long roundId);
    
    /**
     * Find all votes for a specific answer.
     * Useful for counting how many votes an answer received.
     */
    List<Vote> findByAnswerId(Long answerId);
    
    /**
     * Check if a user has already voted in a round.
     */
    boolean existsByRoundIdAndVoterId(Long roundId, Long voterId);
    
    /**
     * Count votes submitted for a round.
     */
    long countByRoundId(Long roundId);
}

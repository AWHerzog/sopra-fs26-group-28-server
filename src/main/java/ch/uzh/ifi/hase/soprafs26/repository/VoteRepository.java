package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.Vote;

/**
 * Data access layer for Vote entities.
 * Provides persistence operations for round votes with uniqueness constraints.
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    // TODO: Add custom query methods (e.g., findByRoundAndUser for validation)
}

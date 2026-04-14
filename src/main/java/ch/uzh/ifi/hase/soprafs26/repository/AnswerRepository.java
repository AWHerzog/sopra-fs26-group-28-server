package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.Answer;

/**
 * Data access layer for Answer entities.
 * Provides persistence operations for round answers with uniqueness constraints.
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // TODO: Add custom query methods (e.g., findByRoundAndUser for validation)
}

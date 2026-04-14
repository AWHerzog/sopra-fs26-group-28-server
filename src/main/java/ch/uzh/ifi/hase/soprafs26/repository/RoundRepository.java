package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.Round;

/**
 * Data access layer for Round entities.
 * Provides persistence operations for game rounds.
 */
@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    // TODO: Add custom query methods as needed
}

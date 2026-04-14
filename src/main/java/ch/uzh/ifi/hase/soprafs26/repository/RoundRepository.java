package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.Round;
import java.util.List;
import java.util.Optional;

/**
 * Data access layer for Round entities.
 * Provides persistence operations for game rounds including queries
 * for finding rounds by game and round number.
 */
@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    
    /**
     * Find all rounds for a specific game, ordered by round number.
     */
    List<Round> findByGameIdOrderByRoundNumberAsc(Long gameId);
    
    /**
     * Find a specific round by game ID and round number.
     */
    Optional<Round> findByGameIdAndRoundNumber(Long gameId, Integer roundNumber);
    
    /**
     * Find the most recent round for a game.
     */
    @Query(value = "SELECT * FROM round WHERE game_id = :gameId ORDER BY round_number DESC LIMIT 1", nativeQuery = true)
    Optional<Round> findLatestRoundByGameId(@Param("gameId") Long gameId);
}

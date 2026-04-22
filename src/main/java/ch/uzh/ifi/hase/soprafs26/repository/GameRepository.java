package ch.uzh.ifi.hase.soprafs26.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Game;

@Repository("gameRepository")
public interface GameRepository extends JpaRepository<Game, Long> {
	Game findByCode(String code);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT g FROM Game g WHERE g.code = :code")
	Game findByCodeForUpdate(@Param("code") String code);
}

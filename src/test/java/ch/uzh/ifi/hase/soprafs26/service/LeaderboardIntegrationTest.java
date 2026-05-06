package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardEntryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserService.class)
public class LeaderboardIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private User savedUser(String username, int points) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("pw");
        u.setToken(username + "-token");
        u.setStatus(UserStatus.OFFLINE);
        u.setCreationDate(LocalDateTime.now());
        u.setPoints(points);
        return userRepository.saveAndFlush(u);
    }

    @Test
    void leaderboard_orderedByPointsDescending() {
        savedUser("alice", 30);
        savedUser("bob", 50);
        savedUser("carol", 10);

        List<LeaderboardEntryDTO> board = userService.getLeaderboard();

        assertEquals(3, board.size());
        assertEquals("bob", board.get(0).getUsername());
        assertEquals("alice", board.get(1).getUsername());
        assertEquals("carol", board.get(2).getUsername());
    }

    @Test
    void leaderboard_correctRanksAssigned() {
        savedUser("alice", 30);
        savedUser("bob", 50);
        savedUser("carol", 10);

        List<LeaderboardEntryDTO> board = userService.getLeaderboard();

        assertEquals(1, board.get(0).getRank()); // bob
        assertEquals(2, board.get(1).getRank()); // alice
        assertEquals(3, board.get(2).getRank()); // carol
    }

    @Test
    void leaderboard_tiedPlayersShareRank() {
        savedUser("alice", 50);
        savedUser("bob", 50);
        savedUser("carol", 10);

        List<LeaderboardEntryDTO> board = userService.getLeaderboard();

        assertEquals(1, board.get(0).getRank());
        assertEquals(1, board.get(1).getRank()); // same points → same rank
        assertEquals(3, board.get(2).getRank()); // next rank skips to 3
    }

    @Test
    void leaderboard_correctPointsReturned() {
        savedUser("alice", 42);

        List<LeaderboardEntryDTO> board = userService.getLeaderboard();

        assertEquals(1, board.size());
        assertEquals(42, board.get(0).getPoints());
    }

    @Test
    void leaderboard_emptyWhenNoUsers() {
        List<LeaderboardEntryDTO> board = userService.getLeaderboard();
        assertTrue(board.isEmpty());
    }
}

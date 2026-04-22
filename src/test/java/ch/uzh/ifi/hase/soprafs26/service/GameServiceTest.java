package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.repository.GameRepository;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameService gameService;

    private Game testGame;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testGame = new Game();
        testGame.setCode("abc123");
        testGame.setHostname("hostUser");
        testGame.setStatus(GameStatus.WAITING);

        Mockito.when(gameRepository.save(Mockito.any())).thenReturn(testGame);
    }

    @Test
    public void joinGame_validCode_success() {
        // given
        Mockito.when(gameRepository.findByCode("abc123")).thenReturn(testGame);

        // when
        Game joinedGame = gameService.joinGame("abc123", "newPlayer");

        // then
        assertTrue(joinedGame.getPlayers().containsKey("newPlayer"));
    }

    @Test
    public void joinGame_invalidCode_throwsException() {
        // given
        Mockito.when(gameRepository.findByCode(Mockito.any())).thenReturn(null);

        // then
        assertThrows(ResponseStatusException.class, () -> gameService.joinGame("wrongCode", "newPlayer"));
    }

    @Test
    public void joinGame_gameAlreadyStarted_throwsException() {
        // given
        testGame.setStatus(GameStatus.ANSWERING);
        Mockito.when(gameRepository.findByCode("abc123")).thenReturn(testGame);

        // then
        assertThrows(ResponseStatusException.class, () -> gameService.joinGame("abc123", "newPlayer"));
    }
}
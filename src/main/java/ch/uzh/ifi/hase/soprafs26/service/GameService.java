package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.repository.GameRepository;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class GameService {

	private final Logger log = LoggerFactory.getLogger(GameService.class);

	private final GameRepository gameRepository;
	private final SimpMessagingTemplate messagingTemplate;

	public GameService(GameRepository gameRepository, SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.messagingTemplate = messagingTemplate;
	}

	public Game createGame(String hostname) {
        Game game = new Game();
        game.setHostname(hostname);
        game.setCode(UUID.randomUUID().toString().substring(0, 6));
        game.setStatus(GameStatus.WAITING);
        game.addPlayer(hostname, 0);

        game = gameRepository.save(game);
		gameRepository.flush();

        sendGameUpdate(game);
        return game;
    }

	public Game joinGame(String code, String username) {
        Game game = gameRepository.findByCode(code);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        if (game.getStatus() != GameStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not in waiting state");
        }

        game.addPlayer(username, 0);
        game = gameRepository.save(game);
		gameRepository.flush();

        sendGameUpdate(game);
        return game;
    }

	/**
	 * This is a helper method that will send a update to all the subscribed players
	 *
	 * @param game
	 * @throws nothing
	 * @see Game
	 */

	public Game getGameState(String code) {
		Game game = gameRepository.findByCode(code);
		if (game == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
		return game;
	}

	public Game startGame(String code, int maxRounds) {
		Game game = gameRepository.findByCode(code);
		if (game == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
		game.setStatus(GameStatus.ANSWERING);
		game.setMaxRounds(maxRounds);
		game.setCurrentRound(1);
		game = gameRepository.save(game);
		gameRepository.flush();
		sendGameUpdate(game);
		return game;
	}

	private void sendGameUpdate(Game game) {
        messagingTemplate.convertAndSend("/topic/game/" + game.getCode(), DTOMapper.INSTANCE.convertEntityToGameGetDTO(game));
    }

}

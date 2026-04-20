package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
public class GameController {

	private final GameService gameService;
	private final UserService userService;

	GameController(GameService gameService, UserService userService) {
		this.gameService = gameService;
		this.userService = userService;
	}

	@PostMapping("/games")
	@ResponseStatus(HttpStatus.OK)
    public GameGetDTO create(@RequestHeader("Authorization") String token) {
		String username = userService.checkTokenAuthenticity(token).getUsername();
        return DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.createGame(username));
    }

    @PostMapping("/games/join")
	@ResponseStatus(HttpStatus.OK)
    public GameGetDTO join(@RequestBody Map<String, String> responseBody, @RequestHeader("Authorization") String token) {
		String code = responseBody.get("code").trim();
		String username = userService.checkTokenAuthenticity(token).getUsername();
        return DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.joinGame(code, username));
    }

	@GetMapping("/games/{code}/state")
	@ResponseStatus(HttpStatus.OK)
	public GameGetDTO getState(@PathVariable String code, @RequestHeader("Authorization") String token) {
		userService.checkTokenAuthenticity(token);
		return DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.getGameState(code));
	}

	@PostMapping("/games/{code}/start")
	@ResponseStatus(HttpStatus.OK)
	public GameGetDTO start(@PathVariable String code, @RequestBody Map<String, Object> body, @RequestHeader("Authorization") String token) {
		userService.checkTokenAuthenticity(token);
		int maxRounds = body.containsKey("maxRounds") ? (int) body.get("maxRounds") : 5;
		return DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.startGame(code, maxRounds));
	}
}

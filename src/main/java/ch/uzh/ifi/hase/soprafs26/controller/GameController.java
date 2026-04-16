package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStartPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AnswerPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStateGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import ch.uzh.ifi.hase.soprafs26.service.GameFlowService;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
public class GameController {

	private final GameService gameService;
	private final UserService userService;
	private final GameFlowService gameFlowService;

	GameController(GameService gameService, UserService userService, GameFlowService gameFlowService) {
		this.gameService = gameService;
		this.userService = userService;
		this.gameFlowService = gameFlowService;
	}

	@PostMapping("/games")
	@ResponseStatus(HttpStatus.OK)
    public GameGetDTO create(@RequestHeader("Authorization") String token) {
		String username = userService.checkTokenAuthenticity(token).getUsername(); //check auth get username
        return DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.createGame(username));
    }

    @PostMapping("/games/join")
	@ResponseStatus(HttpStatus.OK)
    public GameGetDTO join(@RequestBody Map<String, String> responseBody, @RequestHeader("Authorization") String token) {
		String code = responseBody.get("code").trim();
		String username = userService.checkTokenAuthenticity(token).getUsername();
        return DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.joinGame(code, username));
    }

	/* 
	@PostMapping("/delete")
	public void delete()
	*/

	@PostMapping("/games/{code}/start")
	@ResponseStatus(HttpStatus.OK)
	public GameStateGetDTO start(@PathVariable String code, @RequestBody GameStartPostDTO gameStartPostDTO, @RequestHeader("Authorization") String token){
		User hostUser = userService.checkTokenAuthenticity(token); //check auth get User
		return gameFlowService.startGame(code, hostUser, gameStartPostDTO);
	}

	@PostMapping("/games/{code}/answers")
	@ResponseStatus(HttpStatus.OK)
	public GameStateGetDTO submitAnswer(@PathVariable String code, @RequestBody AnswerPostDTO answerPostDTO, @RequestHeader("Authorization") String token){
		User user = userService.checkTokenAuthenticity(token); //check auth get User
		return gameFlowService.submitAnswer(code, user, answerPostDTO);
	}

	@PostMapping("/games/{code}/votes")
	@ResponseStatus(HttpStatus.OK)
	public GameStateGetDTO submitVote(@PathVariable String code, @RequestBody VotePostDTO votePostDTO, @RequestHeader("Authorization") String token){
		User user = userService.checkTokenAuthenticity(token); //check auth get User
		return gameFlowService.submitVote(code, user, votePostDTO);
	}

	@GetMapping("/games/{code}/state")
	@ResponseStatus(HttpStatus.OK)
	public GameStateGetDTO getCurrentGameState(@PathVariable String code, @RequestHeader("Authorization") String token){
		User user = userService.checkTokenAuthenticity(token); //check auth get User
		return gameFlowService.getCurrentGameState(code, user);
	}
}

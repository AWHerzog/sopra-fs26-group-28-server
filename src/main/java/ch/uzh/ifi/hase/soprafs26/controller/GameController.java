package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
public class GameController {

	private final GameService gameService;

	GameController(GameService gameService) {
		this.gameService = gameService;
	}

	@PostMapping("/games")
    public GameGetDTO create(@RequestBody Map<String, String> responseBody) {
		String username = responseBody.get("token").trim();
        return DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.createGame(username));
    }

    @PostMapping("/join")
    public GameGetDTO join(@RequestBody Map<String, String> responseBody) {
		String code = responseBody.get("code").trim();
		String username = responseBody.get("token").trim();
        return DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.joinGame(code, username));
    }

	/* 
	@PostMapping("/delete")
	public void delete()
	*/
}

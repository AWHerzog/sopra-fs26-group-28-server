package ch.uzh.ifi.hase.soprafs26.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.GameFlowService;


import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private GameFlowService gameFlowService;


    @Test
    public void createGame_validInput_returnsGame() throws Exception {
        // given
        User user = new User();
        user.setUsername("hostUser");

        Game game = new Game();
        game.setHostname("hostUser");
        game.setCode("abc123");
        game.setStatus(GameStatus.WAITING);

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameService.createGame(Mockito.any())).willReturn(game);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hostname", is("hostUser")))
            .andExpect(jsonPath("$.code", is("abc123")))
            .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    public void joinGame_validCode_returnsGame() throws Exception {
        // given
        User user = new User();
        user.setUsername("newPlayer");

        Game game = new Game();
        game.setHostname("hostUser");
        game.setCode("abc123");
        game.setStatus(GameStatus.WAITING);
        game.addPlayer("newPlayer", 0);

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameService.joinGame(Mockito.any(), Mockito.any())).willReturn(game);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/join")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content("{\"code\": \"abc123\"}");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code", is("abc123")));
    }

    @Test
    public void joinGame_invalidCode_returns404() throws Exception {
        // given
        User user = new User();
        user.setUsername("newPlayer");

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameService.joinGame(Mockito.any(), Mockito.any()))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/join")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content("{\"code\": \"invalid\"}");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isNotFound());
    }

    @Test
    public void joinGame_gameAlreadyStarted_returns409() throws Exception {
        // given
        User user = new User();
        user.setUsername("newPlayer");

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameService.joinGame(Mockito.any(), Mockito.any()))
            .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Game is not in waiting state"));

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/join")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content("{\"code\": \"abc123\"}");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isConflict());
    }
}

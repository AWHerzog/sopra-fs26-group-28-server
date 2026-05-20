package ch.uzh.ifi.hase.soprafs26.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStartPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStateGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.GameFlowService;


import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    public void joinGame_missingCode_returns400() throws Exception {
        // given
        User user = new User();
        user.setUsername("newPlayer");
        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/join")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content("{}");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void leaveGame_validToken_returnsOk() throws Exception {
        // given
        User user = new User();
        user.setUsername("hostUser");
        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/abc123/leave")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk());
        then(gameFlowService).should().leaveGame("abc123", "hostUser");
    }

    @Test
    public void dirtyLeave_validToken_returnsOk() throws Exception {
        // given
        User user = new User();
        user.setUsername("hostUser");
        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/abc123/leave/dirty")
            .contentType(MediaType.APPLICATION_JSON)
            .param("token", "valid-token");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk());
        then(gameFlowService).should().leaveGame("abc123", "hostUser");
    }

    @Test
    public void startGame_validInput_returnsState() throws Exception {
        // given
        User user = new User();
        user.setUsername("hostUser");
        GameStateGetDTO state = createState("abc123", GameStatus.ANSWERING);

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameFlowService.startGame(Mockito.any(), Mockito.any(), Mockito.any())).willReturn(state);

        GameStartPostDTO startDTO = new GameStartPostDTO();
        startDTO.setMaxRounds(3);
        startDTO.setStageDurationSeconds(30);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/abc123/start")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content(asJsonString(startDTO));

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code", is("abc123")))
            .andExpect(jsonPath("$.status", is("ANSWERING")));
    }

    @Test
    public void startGame_notHost_returns403() throws Exception {
        // given
        User user = new User();
        user.setUsername("nonHost");
        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameFlowService.startGame(Mockito.any(), Mockito.any(), Mockito.any()))
            .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only host can start"));

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/abc123/start")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content("{\"maxRounds\":3,\"stageDurationSeconds\":30}");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isForbidden());
    }

    @Test
    public void ready_validToken_returnsState() throws Exception {
        // given
        User user = new User();
        user.setUsername("player1");
        GameStateGetDTO state = createState("abc123", GameStatus.ROUND_RESULT);

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameFlowService.markPlayerReady(Mockito.eq("abc123"), Mockito.eq(user))).willReturn(state);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/abc123/ready")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("ROUND_RESULT")));
    }

    @Test
    public void submitAnswer_usesTextFallback_returnsState() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("player1");
        GameStateGetDTO state = createState("abc123", GameStatus.ANSWERING);

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameFlowService.submitAnswer(Mockito.eq("abc123"), Mockito.eq(user), any())).willReturn(state);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/abc123/answers")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content("{\"text\":\"bluff answer\",\"questionId\":7}");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code", is("abc123")));

        then(gameFlowService).should().submitAnswer(
            Mockito.eq("abc123"),
            Mockito.eq(user),
            argThat(dto -> "bluff answer".equals(dto.getAnswerText()) && Long.valueOf(7L).equals(dto.getQuestionId()))
        );
    }

    @Test
    public void submitVote_validAnswerId_returnsState() throws Exception {
        // given
        User user = new User();
        user.setId(2L);
        user.setUsername("player2");
        GameStateGetDTO state = createState("abc123", GameStatus.VOTING);

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameFlowService.submitVote(Mockito.eq("abc123"), Mockito.eq(user), any())).willReturn(state);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/abc123/votes")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content("{\"answerId\":42}");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("VOTING")));
    }

    @Test
    public void getCurrentGameState_validToken_returnsState() throws Exception {
        // given
        User user = new User();
        user.setUsername("player1");
        GameStateGetDTO state = createState("abc123", GameStatus.ROUND_RESULT);

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameFlowService.getCurrentGameState(Mockito.eq("abc123"), Mockito.eq(user))).willReturn(state);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/abc123/state")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code", is("abc123")))
            .andExpect(jsonPath("$.status", is("ROUND_RESULT")));
    }

    @Test
    public void translateQuestion_validToken_returnsTranslation() throws Exception {
        // given
        User user = new User();
        user.setUsername("player1");

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameFlowService.translateCurrentQuestion("abc123", "de")).willReturn("Frage auf Deutsch");

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/abc123/question/translate")
            .contentType(MediaType.APPLICATION_JSON)
            .param("lang", "de")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.translatedText", is("Frage auf Deutsch")));
    }

    @Test
    public void advanceStage_validToken_returnsState() throws Exception {
        // given
        User user = new User();
        user.setUsername("hostUser");
        GameStateGetDTO state = createState("abc123", GameStatus.VOTING);

        given(userService.checkTokenAuthenticity(Mockito.any())).willReturn(user);
        given(gameFlowService.advanceStage("abc123")).willReturn(state);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/abc123/advance")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("VOTING")));
    }

    private GameStateGetDTO createState(String code, GameStatus status) {
        GameStateGetDTO state = new GameStateGetDTO();
        state.setCode(code);
        state.setStatus(status);
        return state;
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("The request body could not be created.%s", e));
        }
    }
}

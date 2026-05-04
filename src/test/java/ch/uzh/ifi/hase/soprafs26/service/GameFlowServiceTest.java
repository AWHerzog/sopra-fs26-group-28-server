package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Answer;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.Round;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Vote;
import ch.uzh.ifi.hase.soprafs26.repository.AnswerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoundRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.VoteRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AnswerPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStartPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStateGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotePostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GameFlowServiceTest {

    @Mock private GameRepository gameRepository;
    @Mock private RoundRepository roundRepository;
    @Mock private AnswerRepository answerRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private QuestionService questionService;

    @InjectMocks
    private GameFlowService gameFlowService;

    private Game testGame;
    private User hostUser;
    private User player2;
    private Round testRound;
    private Map<String, Object> questionMap;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        hostUser = new User();
        hostUser.setId(1L);
        hostUser.setUsername("hostUser");
        hostUser.setPoints(0);

        player2 = new User();
        player2.setId(2L);
        player2.setUsername("player2");
        player2.setPoints(0);

        testGame = new Game();
        testGame.setId(10L);
        testGame.setCode("abc123");
        testGame.setHostname("hostUser");
        testGame.setStatus(GameStatus.WAITING);
        testGame.setCurrentRound(1);
        testGame.setMaxRounds(3);
        testGame.addPlayer("hostUser", 0);
        testGame.addPlayer("player2", 0);

        testRound = new Round();
        testRound.setId(100L);
        testRound.setGameId(10L);
        testRound.setRoundNumber(1);
        testRound.setCorrectAnswer("Paris");
        testRound.setScored(false);

        questionMap = new HashMap<>();
        questionMap.put("id", 42L);
        questionMap.put("question", "What is the capital of France?");
        questionMap.put("answer", "Paris");
        questionMap.put("category", "Geography");

        // Default repository stubs
        Mockito.when(gameRepository.findByCode("abc123")).thenReturn(testGame);
        Mockito.when(gameRepository.findByCodeForUpdate("abc123")).thenReturn(testGame);
        Mockito.when(gameRepository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(roundRepository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(roundRepository.findByGameIdAndRoundNumber(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(testRound));

        Mockito.when(answerRepository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(answerRepository.existsByRoundIdAndUserId(Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(answerRepository.countByRoundId(Mockito.any())).thenReturn(1L);
        Mockito.when(answerRepository.findByRoundId(Mockito.any())).thenReturn(new ArrayList<>());

        Mockito.when(voteRepository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(voteRepository.existsByRoundIdAndVoterId(Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(voteRepository.countByRoundId(Mockito.any())).thenReturn(1L);
        Mockito.when(voteRepository.findByRoundId(Mockito.any())).thenReturn(new ArrayList<>());
        Mockito.when(voteRepository.findByAnswerId(Mockito.any())).thenReturn(new ArrayList<>());

        Mockito.when(userRepository.findByUsername("hostUser")).thenReturn(hostUser);
        Mockito.when(userRepository.findByUsername("player2")).thenReturn(player2);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(hostUser));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(player2));

        Mockito.when(questionService.getRandomQuestion(Mockito.any())).thenReturn(questionMap);
        Mockito.when(questionService.getQuestionById(Mockito.any())).thenReturn(questionMap);
    }

    // --- startGame ---

    @Test
    public void startGame_validHost_transitionsToAnswering() {
        testGame.setStatus(GameStatus.WAITING);
        testGame.setCurrentRound(0);
        // round lookup after start needs to find the new round
        Mockito.when(roundRepository.findByGameIdAndRoundNumber(Mockito.any(), Mockito.eq(1)))
                .thenReturn(Optional.of(testRound));

        GameStartPostDTO payload = new GameStartPostDTO();
        payload.setMaxRounds(3);

        GameStateGetDTO result = gameFlowService.startGame("abc123", hostUser, payload);

        assertEquals(GameStatus.ANSWERING, result.getStatus());
        Mockito.verify(roundRepository, Mockito.atLeastOnce()).save(Mockito.any(Round.class));
    }

    @Test
    public void startGame_notHost_throwsForbidden() {
        testGame.setStatus(GameStatus.WAITING);

        assertThrows(ResponseStatusException.class,
                () -> gameFlowService.startGame("abc123", player2, new GameStartPostDTO()));
    }

    @Test
    public void startGame_alreadyStarted_throwsConflict() {
        testGame.setStatus(GameStatus.ANSWERING);

        assertThrows(ResponseStatusException.class,
                () -> gameFlowService.startGame("abc123", hostUser, new GameStartPostDTO()));
    }

    // --- submitAnswer ---

    @Test
    public void submitAnswer_valid_savesAnswer() {
        testGame.setStatus(GameStatus.ANSWERING);
        AnswerPostDTO dto = new AnswerPostDTO();
        dto.setAnswerText("Berlin");

        GameStateGetDTO result = gameFlowService.submitAnswer("abc123", hostUser, dto);

        Mockito.verify(answerRepository).save(Mockito.any(Answer.class));
        assertTrue(result.getAnswerSubmitted());
    }

    @Test
    public void submitAnswer_duplicate_returnsStateWithoutSaving() {
        testGame.setStatus(GameStatus.ANSWERING);
        Mockito.when(answerRepository.existsByRoundIdAndUserId(Mockito.any(), Mockito.any())).thenReturn(true);

        AnswerPostDTO dto = new AnswerPostDTO();
        dto.setAnswerText("Berlin");

        GameStateGetDTO result = gameFlowService.submitAnswer("abc123", hostUser, dto);

        Mockito.verify(answerRepository, Mockito.never()).save(Mockito.any());
        assertTrue(result.getAnswerSubmitted());
    }

    @Test
    public void submitAnswer_matchesCorrectAnswer_throwsBadRequest() {
        testGame.setStatus(GameStatus.ANSWERING);
        AnswerPostDTO dto = new AnswerPostDTO();
        dto.setAnswerText("Paris"); // matches testRound.correctAnswer

        assertThrows(ResponseStatusException.class,
                () -> gameFlowService.submitAnswer("abc123", hostUser, dto));
    }

    @Test
    public void submitAnswer_allPlayersAnswered_transitionsToVoting() {
        testGame.setStatus(GameStatus.ANSWERING);
        // After saving, count equals number of players (2)
        Mockito.when(answerRepository.countByRoundId(Mockito.any())).thenReturn(2L);

        AnswerPostDTO dto = new AnswerPostDTO();
        dto.setAnswerText("Berlin");

        gameFlowService.submitAnswer("abc123", hostUser, dto);

        assertEquals(GameStatus.VOTING, testGame.getStatus());
    }

    // --- submitVote ---

    @Test
    public void submitVote_valid_savesVote() {
        testGame.setStatus(GameStatus.VOTING);
        Answer answer = new Answer();
        answer.setId(200L);
        answer.setRoundId(100L);
        answer.setUserId(2L); // player2's answer
        answer.setContent("London");
        Mockito.when(answerRepository.findById(200L)).thenReturn(Optional.of(answer));

        VotePostDTO dto = new VotePostDTO();
        dto.setAnswerId(200L);

        GameStateGetDTO result = gameFlowService.submitVote("abc123", hostUser, dto);

        Mockito.verify(voteRepository).save(Mockito.any(Vote.class));
        assertTrue(result.getVoteSubmitted());
    }

    @Test
    public void submitVote_selfVote_throwsBadRequest() {
        testGame.setStatus(GameStatus.VOTING);
        Answer ownAnswer = new Answer();
        ownAnswer.setId(201L);
        ownAnswer.setRoundId(100L);
        ownAnswer.setUserId(1L); // hostUser's own answer
        ownAnswer.setContent("Madrid");
        Mockito.when(answerRepository.findById(201L)).thenReturn(Optional.of(ownAnswer));

        VotePostDTO dto = new VotePostDTO();
        dto.setAnswerId(201L);

        assertThrows(ResponseStatusException.class,
                () -> gameFlowService.submitVote("abc123", hostUser, dto));
    }

    @Test
    public void submitVote_duplicate_returnsStateWithoutSaving() {
        testGame.setStatus(GameStatus.VOTING);
        Mockito.when(voteRepository.existsByRoundIdAndVoterId(Mockito.any(), Mockito.any())).thenReturn(true);

        VotePostDTO dto = new VotePostDTO();
        dto.setAnswerId(200L);

        GameStateGetDTO result = gameFlowService.submitVote("abc123", hostUser, dto);

        Mockito.verify(voteRepository, Mockito.never()).save(Mockito.any());
        assertTrue(result.getVoteSubmitted());
    }

    @Test
    public void submitVote_allPlayersVoted_transitionsToRoundResult() {
        testGame.setStatus(GameStatus.VOTING);
        Answer answer = new Answer();
        answer.setId(200L);
        answer.setRoundId(100L);
        answer.setUserId(2L);
        answer.setContent("London");
        Mockito.when(answerRepository.findById(200L)).thenReturn(Optional.of(answer));
        // All 2 players have voted
        Mockito.when(voteRepository.countByRoundId(Mockito.any())).thenReturn(2L);

        VotePostDTO dto = new VotePostDTO();
        dto.setAnswerId(200L);

        gameFlowService.submitVote("abc123", hostUser, dto);

        assertEquals(GameStatus.ROUND_RESULT, testGame.getStatus());
        assertTrue(testRound.isScored());
    }

    // --- computeRoundScores ---

    @Test
    public void computeRoundScores_alreadyScored_isIdempotent() {
        testRound.setScored(true);

        gameFlowService.computeRoundScores("abc123", testRound);

        // Scoring logic must not run a second time
        Mockito.verify(answerRepository, Mockito.never()).findByRoundId(Mockito.any());
    }

    @Test
    public void computeRoundScores_correctAnswerVote_awardsPointToVoter() {
        Answer correctAnswer = new Answer();
        correctAnswer.setId(300L);
        correctAnswer.setRoundId(100L);
        correctAnswer.setUserId(null);
        correctAnswer.setContent("Paris");
        correctAnswer.setIsCorrect(true);

        Vote vote = new Vote();
        vote.setVoterId(2L); // player2 voted for the correct answer
        vote.setAnswerId(300L);

        Mockito.when(answerRepository.findByRoundId(100L)).thenReturn(Collections.singletonList(correctAnswer));
        Mockito.when(voteRepository.findByAnswerId(300L)).thenReturn(Collections.singletonList(vote));

        gameFlowService.computeRoundScores("abc123", testRound);

        assertEquals(1, testGame.getPlayers().get("player2"));
    }

    @Test
    public void computeRoundScores_bluffVote_awardsPointToBluffer() {
        Answer bluffAnswer = new Answer();
        bluffAnswer.setId(301L);
        bluffAnswer.setRoundId(100L);
        bluffAnswer.setUserId(1L); // hostUser's bluff answer
        bluffAnswer.setContent("Madrid");
        bluffAnswer.setIsCorrect(false);

        Vote vote = new Vote();
        vote.setVoterId(2L); // player2 was fooled by hostUser's bluff
        vote.setAnswerId(301L);

        Mockito.when(answerRepository.findByRoundId(100L)).thenReturn(Collections.singletonList(bluffAnswer));
        Mockito.when(voteRepository.findByAnswerId(301L)).thenReturn(Collections.singletonList(vote));

        gameFlowService.computeRoundScores("abc123", testRound);

        assertEquals(1, testGame.getPlayers().get("hostUser"));
    }

    // --- finishGame ---

    @Test
    public void finishGame_nullUser_skippedWithoutCrash() {
        testGame.setStatus(GameStatus.ROUND_RESULT);
        testGame.addPlayer("ghostPlayer", 5);
        Mockito.when(userRepository.findByUsername("ghostPlayer")).thenReturn(null);

        // Must not throw NPE
        assertDoesNotThrow(() -> gameFlowService.finishGame("abc123"));

        assertEquals(GameStatus.FINISHED, testGame.getStatus());
        // Known players still get their points updated
        assertEquals(0 + testGame.getPlayers().get("hostUser"), hostUser.getPoints());
    }

    @Test
    public void finishGame_alreadyFinished_isIdempotent() {
        testGame.setStatus(GameStatus.FINISHED);

        gameFlowService.finishGame("abc123");

        // No save should happen on an already-finished game
        Mockito.verify(gameRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void finishGame_valid_updatesUserPoints() {
        testGame.setStatus(GameStatus.ROUND_RESULT);
        testGame.getPlayers().put("hostUser", 3);
        testGame.getPlayers().put("player2", 5);

        gameFlowService.finishGame("abc123");

        assertEquals(3, hostUser.getPoints());
        assertEquals(5, player2.getPoints());
        assertEquals(GameStatus.FINISHED, testGame.getStatus());
    }

    // --- advanceStage ---

    @Test
    public void advanceStage_fromAnswering_transitionsToVoting() {
        testGame.setStatus(GameStatus.ANSWERING);

        GameStateGetDTO result = gameFlowService.advanceStage("abc123");

        assertEquals(GameStatus.VOTING, result.getStatus());
    }

    @Test
    public void advanceStage_fromVoting_transitionsToRoundResult() {
        testGame.setStatus(GameStatus.VOTING);

        GameStateGetDTO result = gameFlowService.advanceStage("abc123");

        assertEquals(GameStatus.ROUND_RESULT, result.getStatus());
        assertTrue(testRound.isScored());
    }

    @Test
    public void advanceStage_fromRoundResult_startsNextRound() {
        testGame.setStatus(GameStatus.ROUND_RESULT);
        testGame.setCurrentRound(1);
        testGame.setMaxRounds(3);

        GameStateGetDTO result = gameFlowService.advanceStage("abc123");

        assertEquals(GameStatus.ANSWERING, result.getStatus());
        assertEquals(2, testGame.getCurrentRound());
    }

    @Test
    public void advanceStage_fromRoundResult_lastRound_finishesGame() {
        testGame.setStatus(GameStatus.ROUND_RESULT);
        testGame.setCurrentRound(3);
        testGame.setMaxRounds(3);

        GameStateGetDTO result = gameFlowService.advanceStage("abc123");

        assertEquals(GameStatus.FINISHED, result.getStatus());
    }

    @Test
    public void advanceStage_invalidStatus_throwsConflict() {
        testGame.setStatus(GameStatus.WAITING);

        assertThrows(ResponseStatusException.class,
                () -> gameFlowService.advanceStage("abc123"));
    }

    // --- leaveGame ---

    @Test
    public void leaveGame_lastPlayer_deletesGame() {
        testGame.getPlayers().clear();
        testGame.addPlayer("hostUser", 0);
        testGame.setStatus(GameStatus.WAITING);

        gameFlowService.leaveGame("abc123", "hostUser");

        Mockito.verify(gameRepository).delete(testGame);
    }

    @Test
    public void leaveGame_nonHost_removesPlayer() {
        testGame.setStatus(GameStatus.WAITING);

        gameFlowService.leaveGame("abc123", "player2");

        assertFalse(testGame.getPlayers().containsKey("player2"));
        assertEquals("hostUser", testGame.getHostname());
    }

    @Test
    public void leaveGame_host_promotesNewHost() {
        testGame.setStatus(GameStatus.WAITING);

        gameFlowService.leaveGame("abc123", "hostUser");

        assertEquals("player2", testGame.getHostname());
        assertFalse(testGame.getPlayers().containsKey("hostUser"));
    }

    @Test
    public void leaveGame_duringAnswering_autoAdvancesToVotingIfAllAnswered() {
        testGame.setStatus(GameStatus.ANSWERING);
        // player2 leaves; hostUser has already answered → answerCount(1) >= playerCount(1)
        Mockito.when(answerRepository.countByRoundId(Mockito.any())).thenReturn(1L);

        gameFlowService.leaveGame("abc123", "player2");

        assertEquals(GameStatus.VOTING, testGame.getStatus());
    }

    @Test
    public void leaveGame_duringVoting_autoAdvancesToRoundResultIfAllVoted() {
        testGame.setStatus(GameStatus.VOTING);
        // player2 leaves; hostUser has already voted → voteCount(1) >= playerCount(1)
        Mockito.when(voteRepository.countByRoundId(Mockito.any())).thenReturn(1L);

        gameFlowService.leaveGame("abc123", "player2");

        assertEquals(GameStatus.ROUND_RESULT, testGame.getStatus());
    }
}

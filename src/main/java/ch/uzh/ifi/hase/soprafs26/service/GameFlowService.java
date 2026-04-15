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
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing game flow and state transitions.
 * Handles core game logic: starting games, submitting answers/votes, advancing stages, computing scores.
 * Enforces validation, transactional boundaries, and idempotency for resilience.
 */

@Transactional
@Service
public class GameFlowService {

    private final GameRepository gameRepository;
    private final RoundRepository roundRepository;
    private final AnswerRepository answerRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GameFlowService(GameRepository gameRepository, RoundRepository roundRepository,
                           AnswerRepository answerRepository, VoteRepository voteRepository,
                           UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.roundRepository = roundRepository;
        this.answerRepository = answerRepository;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Host starts the game, setting state and deadline
    public GameStateGetDTO startGame(String gameCode, User hostUser, GameStartPostDTO payload) {
        Game game = getGameByCode(gameCode);

        // Only host can start the game
        if (!game.getHostname().equals(hostUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can start the game");
        }

        // Game must be in Waiting state
        if (game.getStatus() != GameStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not in WAITING state");
        }

        // Default max Rounds can be set by host
        if (payload.getMaxRounds() != null) {
            game.setMaxRounds(payload.getMaxRounds());
        }
        
        game.setCurrentRound(1);
        game.setStatus(GameStatus.ANSWERING);

        // Set deadline
        if (payload.getStageDurationSeconds() != null) {
            game.setStageDeadline(LocalDateTime.now().plusSeconds(payload.getStageDurationSeconds()));
        }

        // Save
        Round round = new Round();
        round.setGameId(game.getId());
        round.setRoundNumber(1);
        roundRepository.save(round);
        roundRepository.flush();

        game = gameRepository.save(game);
        gameRepository.flush();

        sendGameUpdate(game);
        return buildGameState(game, hostUser);
    }

    // Players submit answer
    public GameStateGetDTO submitAnswer(String gameCode, User user, AnswerPostDTO payload) {
        Game game = getGameByCode(gameCode);

        // Game must be in ANSWERING stage
        if (game.getStatus() != GameStatus.ANSWERING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not in ANSWERING stage");
        }

        // User must be part of the game
        if (!game.getPlayers().containsKey(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of the game");
        }

        Round round = getCurrentRound(game);

        // Reject if answer matches correct answer
        String correctAnswer = round.getCorrectAnswer();
        if (correctAnswer != null && correctAnswer.equalsIgnoreCase(payload.getAnswerText().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer too similar to correct answer");
        }

        // If the answer already exists for this user and round, return current state. Cannot answer twice
        if (answerRepository.existsByRoundIdAndUserId(round.getId(), user.getId())) {
            GameStateGetDTO state  = buildGameState(game, user);
            state.setAnswerSubmitted(true);
            return state;
        }

        

        //Save answer
        Answer answer = new Answer();
        answer.setRoundId(round.getId());
        answer.setUserId(user.getId());
        answer.setContent(payload.getAnswerText());
        answerRepository.save(answer);
        answerRepository.flush();

        // Auto-advance to VOTING if all players have answered
        long answerCount = answerRepository.countByRoundId(round.getId());
        if (answerCount >= game.getPlayers().size()) {
            game.setStatus(GameStatus.VOTING);
            game.setStageDeadline(LocalDateTime.now().plusSeconds(30));
            game = gameRepository.save(game);
            gameRepository.flush();
        }

        
        sendGameUpdate(game);

        GameStateGetDTO state = buildGameState(game, user);
        state.setAnswerSubmitted(true);
        return state;
    }

    // Players submit vote
    public GameStateGetDTO submitVote(String gameCode, User user, VotePostDTO payload) {
        Game game = getGameByCode(gameCode);

        // Game must be in Voting stage
        if (game.getStatus() != GameStatus.VOTING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not in VOTING stage");
        }

        // User must be part of the game
        if(!game.getPlayers().containsKey(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of the game");
        }

        Round round = getCurrentRound(game);

        // User cannot vote twice
        if (voteRepository.existsByRoundIdAndVoterId(round.getId(), user.getId())) {
            GameStateGetDTO state = buildGameState(game, user);
            state.setVoteSubmitted(true);
            return state;
        }
        
        // Validate answer exists. Not really necessary as the foreign key constraint will ensure this, but we want to throw a 400 if the answer does not exist rather than a 500 from the database.
        Answer answer = answerRepository.findById(payload.getAnswerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer not found"));

        // User cannot vote for own answer
        if (answer.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot vote for own answer");
        }

        Vote vote = new Vote();
        vote.setRoundId(round.getId());
        vote.setVoterId(user.getId());
        vote.setAnswerId(payload.getAnswerId());
        voteRepository.save(vote);
        voteRepository.flush();

        // Auto-advance to ROUND_RESULT if all players have voted
        long voteCount = voteRepository.countByRoundId(round.getId());
        if (voteCount >= game.getPlayers().size()) {
            Round currentRound = getCurrentRound(game);
            computeRoundScores(gameCode, currentRound);
            currentRound.setCompletedAt(LocalDateTime.now());
            roundRepository.save(currentRound);
            game.setStatus(GameStatus.ROUND_RESULT);
            game.setStageDeadline(null);
            game = gameRepository.save(game);
            gameRepository.flush();
        }

        sendGameUpdate(game);

        GameStateGetDTO state = buildGameState(game, user);
        state.setVoteSubmitted(true);
        return state;
    }

    public GameStateGetDTO advanceStage(String gameCode) {
       Game game = getGameByCode(gameCode);

       // Right now if someone does not answer the field is empty in the next stage. His Unsaved answer is not taken into account. 
       // If someone feels like changing this go ahead.

        switch (game.getStatus()) {

            // 30 second timer for everyone why answering. Also auto advance if all answers are in before deadline (can be implemented later)
            case ANSWERING:
                game.setStatus(GameStatus.VOTING);
                // Set deadline for voting stage, 30 seconds 
                game.setStageDeadline(LocalDateTime.now().plusSeconds(30));
                break;

            case VOTING:
                Round round = getCurrentRound(game);
                computeRoundScores(gameCode, round);
                round.setCompletedAt(LocalDateTime.now());
                roundRepository.save(round);
                game.setStatus(GameStatus.ROUND_RESULT);
                // Set deadline for round result stage, e.g. 15 seconds from now
                game.setStageDeadline(LocalDateTime.now().plusSeconds(15));
                break;

            case ROUND_RESULT:
                if (game.getCurrentRound() >= game.getMaxRounds()) {
                    return finishGame(gameCode);
                }

                int nextRound = game.getCurrentRound() + 1;
                game.setCurrentRound(nextRound);
                game.setStatus(GameStatus.ANSWERING);

                Round newRound = new Round();
                newRound.setGameId(game.getId());
                newRound.setRoundNumber(nextRound);
                roundRepository.save(newRound);
                roundRepository.flush();
                break;

            default:
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not in a stage that can be advanced");
        }

        game = gameRepository.save(game);
        gameRepository.flush();

        sendGameUpdate(game);
        return buildGameState(game, null);

    }

    // 1 Point if voted for correct answer, 1 point if people voted for your answer.
    public void computeRoundScores(String gameCode, Round round) {
        Game game = getGameByCode(gameCode);

        List<Answer> answers = answerRepository.findByRoundId(round.getId());
        for (Answer answer : answers) {
            List<Vote> votes = voteRepository.findByAnswerId(answer.getId());

            if (answer.getIsCorrect()) {
                
                for (Vote vote : votes) {
                    Optional<User> voterOpt = userRepository.findById(vote.getVoterId());
                    if (voterOpt.isEmpty()) continue;
                    String username = voterOpt.get().getUsername();
                    int current = game.getPlayers().getOrDefault(username, 0);
                    game.getPlayers().put(username, current + 1);
                }
            } else {
              
                if (votes.isEmpty()) continue;
                Optional<User> authorOpt = userRepository.findById(answer.getUserId());
                if (authorOpt.isEmpty()) continue;
                String username = authorOpt.get().getUsername();
                int current = game.getPlayers().getOrDefault(username, 0);
                game.getPlayers().put(username, current + votes.size());
            }
        }

        gameRepository.save(game);
        gameRepository.flush();
    }

    // Game ends after set number of rounds
    public GameStateGetDTO finishGame(String gameCode) {
        Game game = getGameByCode(gameCode);

        if (game.getStatus() == GameStatus.FINISHED) {
            return buildGameState(game, null);
        }

        game.setStatus(GameStatus.FINISHED);
        game.setStageDeadline(null);
        game = gameRepository.save(game);
        gameRepository.flush();

        sendGameUpdate(game);
        return buildGameState(game, null);
    }

    // Returns current game state
    public GameStateGetDTO getCurrentGameState(String gameCode, User user) {
        Game game = getGameByCode(gameCode);
        return buildGameState(game, user);
    }







    // Helper methods


    // fetches game by code, throws 404 if not found
    private Game getGameByCode(String gameCode) {
        Game game = gameRepository.findByCode(gameCode);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return game;
    }

    // Fetches current round for game using its id and current round number, if it does not exist throw 500 as it is likely a server error if the game references a round that does not exist
    private Round getCurrentRound(Game game) {
        return roundRepository.findByGameIdAndRoundNumber(game.getId(), game.getCurrentRound()) 
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Current round not found"));
        
    }

    // Converts Game to GameStateGetDTO and adds info about whether a user has submitted answer/vote for current round. 
    private GameStateGetDTO buildGameState(Game game, User user) {
       GameStateGetDTO state = DTOMapper.INSTANCE.convertEntityToGameStateGetDTO(game);

       if (user != null && game.getCurrentRound() != null && game.getCurrentRound() > 0) {
            Optional<Round> roundOpt = roundRepository.findByGameIdAndRoundNumber(game.getId(), game.getCurrentRound());
        
        if (roundOpt.isPresent()) {
            Round round = roundOpt.get();
            state.setAnswerSubmitted(answerRepository.existsByRoundIdAndUserId(round.getId(), user.getId()));
            state.setVoteSubmitted(voteRepository.existsByRoundIdAndVoterId(round.getId(), user.getId()));
            }
       }
        return state;
    }

    // Sends Current Game State to all players. -> /topic/game/{gameCode} websocket makes sure update is instantly.
    private void sendGameUpdate(Game game) {
        messagingTemplate.convertAndSend("/topic/game/" + game.getCode(), DTOMapper.INSTANCE.convertEntityToGameStateGetDTO(game));
    }

}

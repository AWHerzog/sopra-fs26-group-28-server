package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
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
import java.util.ArrayList;
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

    private static final Logger log = LoggerFactory.getLogger(GameFlowService.class);

    private final GameRepository gameRepository;
    private final RoundRepository roundRepository;
    private final AnswerRepository answerRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final QuestionService questionService;

    public GameFlowService(GameRepository gameRepository, RoundRepository roundRepository,
                        AnswerRepository answerRepository, VoteRepository voteRepository,
                        UserRepository userRepository, SimpMessagingTemplate messagingTemplate,
                        QuestionService questionService) {
        this.gameRepository = gameRepository;
        this.roundRepository = roundRepository;
        this.answerRepository = answerRepository;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.questionService = questionService;
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
        assignQuestionToRound(game, round);
        roundRepository.save(round);
        roundRepository.flush();

        game = gameRepository.save(game);
        gameRepository.flush();

        sendGameUpdate(game);
        return buildGameState(game, hostUser);
    }

    // Players submit answer
    public GameStateGetDTO submitAnswer(String gameCode, User user, AnswerPostDTO payload) {
        log.info("submitAnswer ENTRY: user={}, gameCode={}, answerText={}", user.getUsername(), gameCode, payload.getAnswerText());
        Game game = getGameByCodeForUpdate(gameCode);
        log.info("submitAnswer: game status={}, players={}", game.getStatus(), game.getPlayers().keySet());

        // Game must be in ANSWERING stage
        if (game.getStatus() != GameStatus.ANSWERING) {
            log.warn("submitAnswer REJECTED: not in ANSWERING stage, current={}", game.getStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not in ANSWERING stage");
        }

        // User must be part of the game
        if (!game.getPlayers().containsKey(user.getUsername())) {
            log.warn("submitAnswer REJECTED: user '{}' not in players {}", user.getUsername(), game.getPlayers().keySet());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of the game");
        }

        Round round = getCurrentRound(game);

        // Reject if answer is missing
        if (payload.getAnswerText() == null || payload.getAnswerText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer text is required");
        }

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
        log.info("submitAnswer: user={}, answerCount={}, playerCount={}, gameStatus={}",
                user.getUsername(), answerCount, game.getPlayers().size(), game.getStatus());
        if (answerCount >= game.getPlayers().size()) {
            addCorrectAnswerOption(round);
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
        Game game = getGameByCodeForUpdate(gameCode);

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

        // User cannot vote for own answer (null userId = correct answer option, always allowed)
        if (answer.getUserId() != null && answer.getUserId().equals(user.getId())) {
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
                addCorrectAnswerOption(getCurrentRound(game));
                game.setStatus(GameStatus.VOTING);
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
                assignQuestionToRound(game, newRound);
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
        if (round.isScored()) return;
        round.setScored(true);
        roundRepository.save(round);
        roundRepository.flush();

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

        //update points for users
        for (Map.Entry<String, Integer> entry : game.getPlayers().entrySet()){
            User user = userRepository.findByUsername(entry.getKey());
            if (user == null) continue;
            user.setPoints(user.getPoints() + entry.getValue());
            userRepository.save(user);
        }
        userRepository.flush();

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

    // fetches game with pessimistic write lock to prevent race conditions
    private Game getGameByCodeForUpdate(String gameCode) {
        Game game = gameRepository.findByCodeForUpdate(gameCode);
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

    // Converts Game to GameStateGetDTO and populates question, answers, and user-specific flags.
    private GameStateGetDTO buildGameState(Game game, User user) {
        GameStateGetDTO state = DTOMapper.INSTANCE.convertEntityToGameStateGetDTO(game);

        if (game.getCurrentRound() != null && game.getCurrentRound() > 0) {
            Optional<Round> roundOpt = roundRepository.findByGameIdAndRoundNumber(game.getId(), game.getCurrentRound());

            if (roundOpt.isPresent()) {
                Round round = roundOpt.get();

                // User-specific flags
                if (user != null) {
                    state.setAnswerSubmitted(answerRepository.existsByRoundIdAndUserId(round.getId(), user.getId()));
                    state.setVoteSubmitted(voteRepository.existsByRoundIdAndVoterId(round.getId(), user.getId()));
                }

                // Populate question
                if (round.getQuestionId() != null) {
                    try {
                        Map<String, Object> q = questionService.getQuestionById(round.getQuestionId());
                        GameStateGetDTO.QuestionDTO qDto = new GameStateGetDTO.QuestionDTO();
                        qDto.setId(Long.valueOf(q.get("id").toString()));
                        qDto.setText(q.get("question").toString());
                        qDto.setCategory(q.containsKey("category") ? q.get("category").toString() : "General Knowledge");
                        state.setQuestion(qDto);
                    } catch (Exception ignored) {}
                }

                // Populate submittedUsernames (only real players, not correct answer entry)
                List<Answer> allAnswers = answerRepository.findByRoundId(round.getId());
                List<String> submittedUsernames = new ArrayList<>();

                if (game.getStatus() == GameStatus.ANSWERING || game.getStatus() == GameStatus.WAITING){
                    for (Answer a : allAnswers) {
                    if (a.getUserId() != null) {
                        userRepository.findById(a.getUserId()).ifPresent(u -> submittedUsernames.add(u.getUsername()));
                        }
                    }
                } else if (game.getStatus() == GameStatus.VOTING) {
                    voteRepository.findByRoundId(round.getId()).forEach(v ->
                        userRepository.findById(v.getVoterId()).ifPresent(u -> submittedUsernames.add(u.getUsername())));
                }
                
                state.setSubmittedUsernames(submittedUsernames);

                // Populate answers
                List<Answer> answers = allAnswers;
                String correctAnswer = round.getCorrectAnswer();
                List<GameStateGetDTO.AnswerDTO> answerDTOs = new ArrayList<>();
                for (Answer answer : answers) {
                    GameStateGetDTO.AnswerDTO aDto = new GameStateGetDTO.AnswerDTO();
                    aDto.setId(answer.getId());
                    aDto.setText(answer.getContent());
                    aDto.setIsCorrect(correctAnswer != null && correctAnswer.equalsIgnoreCase(answer.getContent().trim()));
                    if (answer.getUserId() != null) {
                        userRepository.findById(answer.getUserId()).ifPresent(u -> aDto.setAuthorUsername(u.getUsername()));
                    }
                    List<String> voters = new ArrayList<>();
                    voteRepository.findByAnswerId(answer.getId()).forEach(v ->
                        userRepository.findById(v.getVoterId()).ifPresent(u -> voters.add(u.getUsername()))
                    );
                    aDto.setVoters(voters);
                    answerDTOs.add(aDto);
                }
                state.setAnswers(answerDTOs);
            }
        }
        return state;
    }

    // Sends enriched game state to all players via WebSocket.
    private void sendGameUpdate(Game game) {
        messagingTemplate.convertAndSend("/topic/game/" + game.getCode(), buildGameState(game, null));
    }

    // Adds the correct answer as a voting option (userId=null marks it as the real answer)
    private void addCorrectAnswerOption(Round round) {
        String correct = round.getCorrectAnswer();
        if (correct == null || correct.isBlank()) return;
        // Only add if not already present
        boolean alreadyAdded = answerRepository.findByRoundId(round.getId()).stream()
                .anyMatch(a -> a.getUserId() == null);
        if (alreadyAdded) return;
        Answer correctAnswer = new Answer();
        correctAnswer.setRoundId(round.getId());
        correctAnswer.setUserId(null);
        correctAnswer.setContent(correct);
        correctAnswer.setIsCorrect(true);
        answerRepository.save(correctAnswer);
        answerRepository.flush();
    }

    private void assignQuestionToRound(Game game, Round round) {
        Map<String, Object> question = questionService.getRandomQuestion(game);
        round.setQuestionId(Long.valueOf(question.get("id").toString()));
        round.setCorrectAnswer(question.get("answer").toString());
        game.setCurrentQuestionId(Long.valueOf(question.get("id").toString()));
    }

}

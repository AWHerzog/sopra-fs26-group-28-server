package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Round;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AnswerPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStartPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStateGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotePostDTO;
import org.springframework.stereotype.Service;

/**
 * Service for managing game flow and state transitions.
 * Handles core game logic: starting games, submitting answers/votes, advancing stages, computing scores.
 * Enforces validation, transactional boundaries, and idempotency for resilience.
 */
@Service
public class GameFlowService {
    public GameStateGetDTO startGame(String gameCode, User hostUser, GameStartPostDTO payload) {
        throw new UnsupportedOperationException("TODO: implement startGame");
    }

    public GameStateGetDTO submitAnswer(String gameCode, User user, AnswerPostDTO payload) {
        throw new UnsupportedOperationException("TODO: implement submitAnswer");
    }

    public GameStateGetDTO submitVote(String gameCode, User user, VotePostDTO payload) {
        throw new UnsupportedOperationException("TODO: implement submitVote");
    }

    public GameStateGetDTO advanceStage(String gameCode) {
        throw new UnsupportedOperationException("TODO: implement advanceStage");
    }

    public void computeRoundScores(String gameCode, Round round) {
        throw new UnsupportedOperationException("TODO: implement computeRoundScores");
    }

    public GameStateGetDTO finishGame(String gameCode) {
        throw new UnsupportedOperationException("TODO: implement finishGame");
    }

    public GameStateGetDTO getCurrentGameState(String gameCode, User user) {
        throw new UnsupportedOperationException("TODO: implement getCurrentGameState");
    }
}

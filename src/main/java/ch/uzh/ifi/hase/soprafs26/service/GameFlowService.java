package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.stereotype.Service;

/**
 * Service for managing game flow and state transitions.
 * Handles core game logic: starting games, submitting answers/votes, advancing stages, computing scores.
 * Enforces validation, transactional boundaries, and idempotency for resilience.
 */
@Service
public class GameFlowService {
    // TODO: Add core game flow methods:
    // - startGame(gameCode, hostUser)
    // - submitAnswer(gameCode, user, payload)
    // - submitVote(gameCode, user, payload)
    // - advanceStage(gameCode)
    // - computeRoundScores(gameCode, round)
    // - finishGame(gameCode)
    // - getCurrentGameState(gameCode, user)
}

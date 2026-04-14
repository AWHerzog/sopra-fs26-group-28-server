package ch.uzh.ifi.hase.soprafs26.constant;

/**
 * Game Status enum defines the lifecycle states of a game.
 * 
 * WAITING       - Game created, waiting for host to start (players can join)
 * ANSWERING     - Players are submitting answers to the current question
 * VOTING        - Players are voting on answers from the answering phase
 * ROUND_RESULT  - Scores are calculated, results are shown
 * FINISHED      - Game has ended, no more actions allowed
 */
public enum GameStatus {
	WAITING, ANSWERING, VOTING, ROUND_RESULT, FINISHED
}

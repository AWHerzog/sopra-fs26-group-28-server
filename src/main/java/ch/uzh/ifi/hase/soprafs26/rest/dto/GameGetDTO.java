package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;


public class GameGetDTO {

	private Long id;
	private String hostname;
	private String code;
	private GameStatus status;
	private Integer currentRound;
	private Integer maxRounds;
	private Long currentQuestionId;
	private LocalDateTime stageDeadline;
	private Boolean answerSubmitted;
	private Boolean voteSubmitted;
	private Map<String, Integer> players = new HashMap<>();
	private Map<String, Integer> scores = new HashMap<>();

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getCode(){
		return code;
	}

	public void setCode(String code){
		this.code = code;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public Integer getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(Integer currentRound) {
		this.currentRound = currentRound;
	}

	public Integer getMaxRounds() {
		return maxRounds;
	}

	public void setMaxRounds(Integer maxRounds) {
		this.maxRounds = maxRounds;
	}

	public Long getCurrentQuestionId() {
		return currentQuestionId;
	}

	public void setCurrentQuestionId(Long currentQuestionId) {
		this.currentQuestionId = currentQuestionId;
	}

	public LocalDateTime getStageDeadline() {
		return stageDeadline;
	}

	public void setStageDeadline(LocalDateTime stageDeadline) {
		this.stageDeadline = stageDeadline;
	}

	public Boolean getAnswerSubmitted() {
		return answerSubmitted;
	}

	public void setAnswerSubmitted(Boolean answerSubmitted) {
		this.answerSubmitted = answerSubmitted;
	}

	public Boolean getVoteSubmitted() {
		return voteSubmitted;
	}

	public void setVoteSubmitted(Boolean voteSubmitted) {
		this.voteSubmitted = voteSubmitted;
	}

	public Map <String, Integer> getPlayers(){
		return players;
	}

	public void setPlayers(Map <String, Integer> players){
		this.players = players;
	}

	public void addPlayer(String username, Integer points){
		players.put(username, points);
	}

	public void removePlayer(String username){
		players.remove(username);
	}

	public Map<String, Integer> getScores() {
		return scores;
	}

	public void setScores(Map<String, Integer> scores) {
		this.scores = scores;
	}
}

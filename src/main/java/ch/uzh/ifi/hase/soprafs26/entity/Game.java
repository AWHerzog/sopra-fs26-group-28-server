package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;



@Entity
@Table(name = "game")
public class Game implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = true)
	private String hostname;

	@Column(nullable = false, unique = false)
	private String code;

	@Column(nullable = false)
	private GameStatus status;

	@Column(nullable = false)
	private Integer currentRound = 0;

	@Column(nullable = false)
	private Integer maxRounds = 5;

	@Column(nullable = true)
	private Long currentQuestionId;

	@Column(nullable = true)
	private LocalDateTime stageDeadline;

	@ElementCollection
	@CollectionTable(
    	name = "game_players",
    	joinColumns = @JoinColumn(name = "game_id")
	)
	@MapKeyColumn(name = "username")
	@Column(name = "points")
	private Map<String, Integer> players = new HashMap<>();

	@ElementCollection
	@CollectionTable(name = "game_used_questions", joinColumns = @JoinColumn(name = "game_id"))
	@Column(name = "question_id")
	private List<Long> usedQuestionIds = new ArrayList<>();
	
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

	public List<Long> getUsedQuestionIds() {
		return usedQuestionIds;
	}
}

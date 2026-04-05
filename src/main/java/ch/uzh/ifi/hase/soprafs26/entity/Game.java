package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;

import java.io.Serializable;

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

	@ElementCollection
	private Map<String, Integer> players = new HashMap<>();
	
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
}

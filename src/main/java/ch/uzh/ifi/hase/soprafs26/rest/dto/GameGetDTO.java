package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import java.util.Map;
import java.util.HashMap;


public class GameGetDTO {

	private Long id;
	private String hostname;
	private String code;
	private GameStatus status;
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

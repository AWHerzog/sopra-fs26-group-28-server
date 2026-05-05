package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import java.time.LocalDateTime;

public class UserGetDTO {

	private Long id;
	private String username;
	private UserStatus status;
	private LocalDateTime creationDate;
	private int points;
	private boolean onboardingCompleted;
	private String token;

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime status) {
		this.creationDate = creationDate;
	}

	public int getPoints(){
		return points;
	}

	public void setPoints(int points){
		this.points = points;
	}

	public boolean isOnboardingCompleted() {
		return onboardingCompleted;
	}

	public void setOnboardingCompleted(boolean onboardingCompleted) {
		this.onboardingCompleted = onboardingCompleted;
	}

	public String getToken(){
		return token;
	}

	public void setToken(String token){
		this.token = token;
	}
}

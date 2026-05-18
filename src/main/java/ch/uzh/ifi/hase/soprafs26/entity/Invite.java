package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;



@Entity
@Table(name = "invite")
public class Invite implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = false) 
	private String senderUsername;

	@Column(nullable = false, unique = false) 
	private String receiverUsername;

	@Column(nullable = false, unique = false)
	private InviteStatus status;

	@Column(nullable = false, unique = false)
	private String gameCode;



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSenderUsername(){
		return senderUsername;
	}

	public void setSenderUsername(String senderUsername){
		this.senderUsername = senderUsername;
	}

	public String getReceiverUsername(){
		return receiverUsername;
	}

	public void setReceiverUsername(String receiverUsername){
		this.receiverUsername = receiverUsername;
	}

	public InviteStatus getStatus(){
		return status;
	}

	public void setStatus(InviteStatus status){
		this.status = status;
	}

	public String getGameCode(){
		return gameCode;
	}

	public void setGameCode(String gameCode){
		this.gameCode = gameCode;
	}

}

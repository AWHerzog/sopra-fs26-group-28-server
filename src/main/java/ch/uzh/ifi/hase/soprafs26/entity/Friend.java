package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;

import java.time.LocalDateTime;



@Entity
@Table(name = "friends")
public class Friend implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = false) 
	private String senderUsername;

	@Column(nullable = false, unique = false) 
	private String receiverUsername;

	@Column(nullable = false, unique = false)
	private String status;




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

	public String getStatus(){
		return status;
	}

	public void setStatus(String status){
		this.status = status;
	}

}

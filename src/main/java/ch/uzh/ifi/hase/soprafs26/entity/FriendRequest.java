package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;



@Entity
@Table(name = "friends_Requests")
public class FriendRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = false)
	private long senderId;

	@Column(nullable = false, unique = false)
	private String senderUsername;

	@Column(nullable = false, unique = false)
	private long receiverId;

	@Column(nullable = false, unique = false)
	private LocalDateTime createdAt;

	@Column(nullable = false, unique = false)
	private FriendRequestStatus status;




	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	public String getSenderUsername(){
		return senderUsername;
	}

	public void setSenderUsername(String senderUsername){
		this.senderUsername = senderUsername;
	}

	public Long getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(Long receiverId) {
		this.receiverId = receiverId;
	}

	public LocalDateTime getCreatedAt(){
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt){
		this.createdAt = createdAt;
	}

	public FriendRequestStatus getStatus(){
		return status;
	}

	public void setStatus(FriendRequestStatus status){
		this.status = status;
	}

}

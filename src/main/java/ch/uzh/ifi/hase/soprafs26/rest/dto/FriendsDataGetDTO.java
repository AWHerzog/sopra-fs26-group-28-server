package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.entity.Friend;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;

import java.time.LocalDateTime;

import java.util.List;
import java.util.ArrayList;

public class FriendsDataGetDTO {

	private List<Friend> friends = new ArrayList<>();
	private List<FriendRequest> incomingRequests = new ArrayList<>();
	private List<FriendRequest> outgoingRequests = new ArrayList<>();


	public List<Friend> getFriends(){
		return friends;
	}

	public void setFriends(List<Friend> friends){
		this.friends = friends;
	}

	//maybe more list manipulation stuff here if needed

	public List<FriendRequest> getIncomingRequests(){
		return incomingRequests;
	}

	public void setIncomingFriends(List<FriendRequest> incomingRequests){
		this.incomingRequests = incomingRequests;
	}

	//maybe more list manipulation stuff here if needed

	public List<FriendRequest> getOutgoingRequests(){
		return outgoingRequests;
	}

	public void setOutgoingFriends(List<FriendRequest> outgoingRequests){
		this.outgoingRequests = outgoingRequests;
	}

	//maybe more list manipulation stuff here if needed
}

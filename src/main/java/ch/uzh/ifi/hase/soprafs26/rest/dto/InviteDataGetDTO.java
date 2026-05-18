package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.entity.Invite;

import java.time.LocalDateTime;

import java.util.List;
import java.util.ArrayList;

public class InviteDataGetDTO {

	private List<Invite> invites = new ArrayList<>();
	

	public List<Invite> getInvites(){
		return invites;
	}

	public void setInvites(List<Invite> invites){
		this.invites = invites;
	}

}

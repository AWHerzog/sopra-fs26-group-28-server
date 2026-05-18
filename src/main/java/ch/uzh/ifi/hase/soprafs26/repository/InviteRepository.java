package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Invite;

import java.util.List;

@Repository("inviteRepository")
public interface InviteRepository extends JpaRepository<Invite, Long> {
	Invite findInviteById(Long id);
	Invite findBySenderUsernameAndReceiverUsername(String senderUsername, String receiverUsername);
	List<Invite> findByReceiverUsername(String receiverUsername);
}

package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Invite;
import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;

import java.util.List;

@Repository("inviteRepository")
public interface InviteRepository extends JpaRepository<Invite, Long> {
	Invite findInviteById(Long id);
	Invite findBySenderUsernameAndReceiverUsername(String senderUsername, String receiverUsername);
	Invite findBySenderUsernameAndReceiverUsernameAndGameCodeAndStatus(String senderUsername, String receiverUsername, String gameCode, InviteStatus status);
	List<Invite> findByGameCodeAndStatus(String gameCode, InviteStatus status);
	List<Invite> findByReceiverUsername(String receiverUsername);
}

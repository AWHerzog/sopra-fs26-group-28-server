package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Friend;

import java.util.List;

@Repository("friendRepository")
public interface FriendRepository extends JpaRepository<Friend, Long> {
	List<Friend> findBySenderUsernameOrReceiverUsername(String senderUsername, String receiverUsername);
	Friend findFriendById(Long id);
}

package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;

import java.util.List;

@Repository("friendRequestRepository")
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
	List<FriendRequest> findByReceiverId(Long Id);
	List<FriendRequest> findBySenderId(Long Id);
	FriendRequest findFriendRequestById(Long id); // use this instead of findById
	FriendRequest findBySenderIdAndReceiverId(Long senderId, Long receiverId); 
}

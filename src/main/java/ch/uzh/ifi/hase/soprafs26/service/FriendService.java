package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendsDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Friend;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendService {

	private final Logger log = LoggerFactory.getLogger(FriendService.class);

	private final FriendRepository friendRepository;

	private final FriendRequestRepository friendRequestRepository;

	private final UserRepository userRepository;

	public FriendService(@Qualifier("friendRepository") FriendRepository friendRepository, @Qualifier("friendRequestRepository") FriendRequestRepository friendRequestRepository, @Qualifier("userRepository") UserRepository userRepository) {
		this.friendRepository = friendRepository;
		this.friendRequestRepository = friendRequestRepository;
		this.userRepository = userRepository;
	}

	public FriendsDataGetDTO getFriendsData(User user){
		FriendsDataGetDTO friendsDataGetDTO = new FriendsDataGetDTO();
		List<Friend> rawFriends = friendRepository.findBySenderUsernameOrReceiverUsername(user.getUsername(), user.getUsername());

		friendsDataGetDTO.setFriends(normalizeFriends(rawFriends, user.getUsername()));
		friendsDataGetDTO.setIncomingFriends(friendRequestRepository.findByReceiverId(user.getId()));
		friendsDataGetDTO.setOutgoingFriends(friendRequestRepository.findBySenderId(user.getId()));
		return friendsDataGetDTO;
	}

	public void declineFriend(Long id){
		FriendRequest friendRequest = friendRequestRepository.findFriendRequestById(id);
		friendRequest.setStatus(FriendRequestStatus.DECLINED); //still keep request, can change later
	}

	public void acceptFriend(Long id){
		FriendRequest friendRequest = friendRequestRepository.findFriendRequestById(id);
		friendRequest.setStatus(FriendRequestStatus.ACCEPTED); 

		User receiver = userRepository.findById(friendRequest.getReceiverId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		
		Friend friend = new Friend();
		friend.setSenderUsername(friendRequest.getSenderUsername());
		friend.setReceiverUsername(receiver.getUsername());
		friend.setStatus("1"); //temp value

		//here could check if friendship already exists but better do this for sending requests 

		friend = friendRepository.save(friend);
		friendRepository.flush();
	}

	public void removeFriend(Long friendId){
		//delete Friend
		Friend friendship = friendRepository.findFriendById(friendId);
		User sender = userRepository.findByUsername(friendship.getSenderUsername());
		User receiver = userRepository.findByUsername(friendship.getReceiverUsername());
		friendRepository.delete(friendship);

		//delete FriendRequest
		FriendRequest request = friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), receiver.getId());
		friendRequestRepository.delete(request);

		friendRepository.flush();
		friendRequestRepository.flush();
	}

	public void sendFriendRequest(User sender, String receiverUsername){
		User receiver = userRepository.findByUsername(receiverUsername);

		if (sender == receiver){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot add yourself as friend");
		}

		//send request
		FriendRequest friendRequest = new FriendRequest();
		friendRequest.setSenderId(sender.getId());
		friendRequest.setSenderUsername(sender.getUsername());
		friendRequest.setReceiverId(receiver.getId());
		friendRequest.setCreatedAt(LocalDateTime.now());
		friendRequest.setStatus(FriendRequestStatus.PENDING);
	}



	//helper to always have sender as user wanting the friends list and receiver being his friends
	private List<Friend> normalizeFriends(List<Friend> friends, String username) {
    return friends.stream()
        .map(friend -> {
            if (friend.getReceiverUsername().equals(username)) {
                Friend normalized = new Friend();
                normalized.setId(friend.getId());
                normalized.setSenderUsername(friend.getReceiverUsername());
                normalized.setReceiverUsername(friend.getSenderUsername());
                normalized.setStatus(friend.getStatus());
                return normalized;
            }
            return friend;
        })
        .collect(Collectors.toList());
	}
}

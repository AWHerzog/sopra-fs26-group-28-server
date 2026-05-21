package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendsDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InviteDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Friend;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Invite;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.InviteRepository;

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

	private final InviteRepository inviteRepository;

	private final GameRepository gameRepository;

	public FriendService(@Qualifier("inviteRepository") InviteRepository inviteRepository, @Qualifier("friendRepository") FriendRepository friendRepository, @Qualifier("friendRequestRepository") FriendRequestRepository friendRequestRepository, @Qualifier("userRepository") UserRepository userRepository, @Qualifier("gameRepository") GameRepository gameRepository) {
		this.friendRepository = friendRepository;
		this.friendRequestRepository = friendRequestRepository;
		this.userRepository = userRepository;
		this.inviteRepository = inviteRepository;
		this.gameRepository = gameRepository;
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
		if (friendRequest.getStatus() != FriendRequestStatus.PENDING){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You already declined this request");
		}
		friendRequest.setStatus(FriendRequestStatus.DECLINED); //still keep request, can change later
	}

	public void acceptFriend(Long id){
		FriendRequest friendRequest = friendRequestRepository.findFriendRequestById(id);

		if (friendRequest.getStatus() != FriendRequestStatus.PENDING){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You already accepted this request");
		}

		friendRequest.setStatus(FriendRequestStatus.ACCEPTED); 

		User receiver = userRepository.findById(friendRequest.getReceiverId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		Friend friend = new Friend();
		friend.setSenderUsername(friendRequest.getSenderUsername());
		friend.setReceiverUsername(receiver.getUsername());
		friend.setStatus(receiver.getStatus().toString());

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

		//check if a request was already sent 
		if (friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), receiver.getId()) != null){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Already sent a request to this user");
		}

		//check if users are already friends (both directions)
		if (friendRepository.findBySenderUsernameAndReceiverUsername(sender.getUsername(), receiver.getUsername()) != null
				|| friendRepository.findBySenderUsernameAndReceiverUsername(receiver.getUsername(), sender.getUsername()) != null){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have this user as a friend");
		}

		//send request
		FriendRequest friendRequest = new FriendRequest();
		friendRequest.setSenderId(sender.getId());
		friendRequest.setSenderUsername(sender.getUsername());
		friendRequest.setReceiverId(receiver.getId());
		friendRequest.setCreatedAt(LocalDateTime.now());
		friendRequest.setStatus(FriendRequestStatus.PENDING);
		friendRequestRepository.save(friendRequest);
	}

	public void inviteFriend(User sender, String receiverUsername, String gameCode){
		User receiver = userRepository.findByUsername(receiverUsername);
		if (receiver == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found");
		}

		if (sender == receiver){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot invite yourself");
		}

		if (isUserInAnyGame(receiver.getUsername())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Receiver is already in a game");
		}

		if (inviteRepository.findBySenderUsernameAndReceiverUsernameAndGameCodeAndStatus(
				sender.getUsername(), receiver.getUsername(), gameCode, InviteStatus.PENDING) != null){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Already sent a request to this user");
		}

		Invite invite = new Invite();
		invite.setSenderUsername(sender.getUsername());
		invite.setReceiverUsername(receiver.getUsername());
		invite.setStatus(InviteStatus.PENDING); 
		invite.setGameCode(gameCode);
		inviteRepository.save(invite);
	}

	private boolean isUserInAnyGame(String username) {
		return gameRepository.findAll().stream()
			.anyMatch(game -> game.getPlayers() != null && game.getPlayers().containsKey(username));
	}

	public String acceptInvite(Long id){
		Invite invite = inviteRepository.findInviteById(id);
		
		if (invite.getStatus() != InviteStatus.PENDING){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You already accepted this invite");
		}

		invite.setStatus(InviteStatus.ACCEPTED);
		return invite.getGameCode();
	}

	public void declineInvite(Long id){
		Invite invite = inviteRepository.findInviteById(id);
		
		if (invite.getStatus() != InviteStatus.PENDING){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You already declined this invite");
		}

		invite.setStatus(InviteStatus.DECLINED);

		//delete invite 
		inviteRepository.delete(invite);
		inviteRepository.flush();
	}

	public InviteDataGetDTO getInvites(User receiver){
		InviteDataGetDTO inviteDataGetDTO = new InviteDataGetDTO();
		List<Invite> invites = inviteRepository.findByReceiverUsername(receiver.getUsername());

		inviteDataGetDTO.setInvites(invites);
		return inviteDataGetDTO;
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

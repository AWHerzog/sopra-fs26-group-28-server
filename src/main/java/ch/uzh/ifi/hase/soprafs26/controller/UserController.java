package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Friend;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardEntryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserOnboardingPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.FriendService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendsDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InviteDataGetDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

	private final UserService userService;
	private final FriendService friendService;

	UserController(UserService userService, FriendService friendService) {
		this.userService = userService;
		this.friendService = friendService;
	}

	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers() {
		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO); //frontend still needs to be chanhed here for correct input

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

	@GetMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO getUserById(@PathVariable Long userId) {
		User user = userService.getUserById(userId);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}

	@PutMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO updateUser(
			@PathVariable Long userId,
			@RequestBody UserPutDTO userPutDTO,
			@RequestHeader("Authorization") String token) {
		User requester = userService.checkTokenAuthenticity(token);
		if (!requester.getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own profile");
		}
		User updatedUser = userService.updateUser(userId, userPutDTO.getUsername());
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
	}

	@PutMapping("/users/{userId}/onboarding")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO updateOnboardingCompletion(
			@PathVariable Long userId,
			@RequestBody UserOnboardingPutDTO userOnboardingPutDTO) {
		User updatedUser = userService.updateOnboardingCompletion(
				userId,
				userOnboardingPutDTO.isOnboardingCompleted());
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
	}

	@PostMapping("/users/login")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO); //frontend still needs to be chanhed here for correct input

		// login user
		User loggedInUser = userService.loginUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
	}

	

	@PostMapping("/users/logout")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void logout(@RequestHeader("Authorization") String token) {
		userService.checkTokenAuthenticity(token);
		userService.logoutUser(token);
	}

	@GetMapping("/leaderboard")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<LeaderboardEntryDTO> getLeaderboard(@RequestHeader("Authorization") String token) {
		userService.checkTokenAuthenticity(token);
		return userService.getLeaderboard();
	}

	@GetMapping("/_ah/health")
	@ResponseStatus(HttpStatus.OK)
	public void healthCheck() {}


	@GetMapping("users/friends")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public FriendsDataGetDTO getFriendsData(@RequestHeader("Authorization") String token){
		User user = userService.checkTokenAuthenticity(token);
		return friendService.getFriendsData(user);
	}

	@PostMapping("/users/friends/requests/{requestId}/decline")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void declineFriend(@RequestHeader("Authorization") String token, @PathVariable Long requestId){
		User user = userService.checkTokenAuthenticity(token);
		friendService.declineFriend(requestId);
	}

	@PostMapping("/users/friends/requests/{requestId}/accept")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void acceptFriend(@RequestHeader("Authorization") String token, @PathVariable Long requestId){
		userService.checkTokenAuthenticity(token);
		friendService.acceptFriend(requestId);
	}

	//delete
	@DeleteMapping("/users/friends/{friendId}") //im assuming friendId is the id of the friend entity
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void removeFriend(@RequestHeader("Authorization") String token, @PathVariable Long friendId){
		userService.checkTokenAuthenticity(token);
		friendService.removeFriend(friendId);
	}


	//send friend request
	@PostMapping("/users/friends/requests")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void sendFriendRequest(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String token){	//assume sends username of user wanting to be added in body (can be changed)
		String rawUsername = body.get("username");
		if (rawUsername == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing game username");
		String username = rawUsername.trim();
		
		User user = userService.checkTokenAuthenticity(token);
		friendService.sendFriendRequest(user, username);
	}


	//Invite friends
	@PostMapping("/friends/invite")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void invite(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String token){
		User sender = userService.checkTokenAuthenticity(token);
		
		String rawUsername = body.get("username");
		if (rawUsername == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing receiver username");
		String receiverUsername = rawUsername.trim();

		String rawGameCode = body.get("gameCode");
		if (rawGameCode == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing game code");
		String gameCode = rawGameCode.trim();

		friendService.inviteFriend(sender, receiverUsername, gameCode);
	}

	//get invites
	@PostMapping("/friends/invite/get")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public InviteDataGetDTO inviteGet(@RequestHeader("Authorization") String token){
		User receiver = userService.checkTokenAuthenticity(token);
		InviteDataGetDTO inviteDataGetDTO = friendService.getInvites(receiver);
		return inviteDataGetDTO;
	}

	@PostMapping("/friends/invite/accept")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, String> inviteAccept(@RequestBody Map<String, Long> body, @RequestHeader("Authorization") String token){
		userService.checkTokenAuthenticity(token);
		Long id = body.get("inviteId");
		if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing inviteId");
		String gameCode = friendService.acceptInvite(id);
		return Map.of("gameCode", gameCode);
	}

	@DeleteMapping("/friends/invite/decline")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void inviteDecline(@RequestBody Map<String, Long> body, @RequestHeader("Authorization") String token){
		userService.checkTokenAuthenticity(token);
		Long id = body.get("inviteId");
		if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing inviteId");
		friendService.declineInvite(id);
	}
}

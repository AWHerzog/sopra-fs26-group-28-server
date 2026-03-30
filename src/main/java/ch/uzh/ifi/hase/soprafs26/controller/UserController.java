package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

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

	UserController(UserService userService) {
		this.userService = userService;
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
	public void logout(@RequestBody Map<String, String> responseBody) {
		//get token from frontend
		String token = responseBody.get("token").trim();

		//logout
		userService.logoutUser(token);

	}

}

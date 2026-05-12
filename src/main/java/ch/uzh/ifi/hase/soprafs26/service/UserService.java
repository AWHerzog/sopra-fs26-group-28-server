package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardEntryDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public List<LeaderboardEntryDTO> getLeaderboard() {
		List<User> users = userRepository.findAllByOrderByPointsDesc();
		List<LeaderboardEntryDTO> leaderboard = new ArrayList<>();
		int rank = 1;
		for (int i = 0; i < users.size(); i++) {
			if (i > 0 && users.get(i).getPoints() < users.get(i - 1).getPoints()) {
				rank = i + 1;
			}
			LeaderboardEntryDTO entry = new LeaderboardEntryDTO();
			entry.setRank(rank);
			entry.setUsername(users.get(i).getUsername());
			entry.setPoints(users.get(i).getPoints());
			leaderboard.add(entry);
		}
		return leaderboard;
	}

	public User createUser(User newUser) {
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.ONLINE);
		newUser.setCreationDate(LocalDateTime.now());
		newUser.setPoints(0);
		newUser.setOnboardingCompleted(false);

		//hashing
		String hashedPassword = passwordEncoder.encode(newUser.getPassword());
		newUser.setPassword(hashedPassword);

		checkIfUserExists(newUser);
		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	public User loginUser(User loginUser){
		validateLoginCredentials(loginUser); //check if credentials are valid
		User user = userRepository.findByUsername(loginUser.getUsername()); //get User
		user.setToken(UUID.randomUUID().toString());
		user.setStatus(UserStatus.ONLINE);
		return user;
	}

	public User updateUser(Long userId, String newUsername) {
		User user = getUserById(userId);
		if (newUsername == null || newUsername.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be empty");
		}
		if (!newUsername.equals(user.getUsername())) {
			if (userRepository.findByUsername(newUsername) != null) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
			}
			user.setUsername(newUsername);
		}
		return user;
	}

	public User updateOnboardingCompletion(Long userId, boolean onboardingCompleted) {
		User user = getUserById(userId);
		user.setOnboardingCompleted(onboardingCompleted);
		return user;
	}

	public void logoutUser(String token){
		User user = userRepository.findByToken(token);
		user.setToken(null);
		user.setStatus(UserStatus.OFFLINE);
	}

	public User getUserById(Long id) {
    return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public User checkTokenAuthenticity(String token){
		User user = userRepository.findByToken(token);
		if (user == null){
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		return user;
	}

	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

		String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
		if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format(baseErrorMessage, "username", "is"));
		}
	}

	/**
	 * This helper method checks if the password and the db hash match
	 * Method will do nothing if they match and will throw an error otherwise.
	 * @param userToBeLoggedIn
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */

	private void validateLoginCredentials(User userToBeLoggedIn){
		User userByUsername = userRepository.findByUsername(userToBeLoggedIn.getUsername());

		if (userByUsername == null || !passwordEncoder.matches(userToBeLoggedIn.getPassword(), userByUsername.getPassword())){
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Username or Password is wrong"));
		}
	}
}

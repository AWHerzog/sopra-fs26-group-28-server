package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setPassword("password");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertFalse(createdUser.isOnboardingCompleted());
	}

	@Test
	public void updateOnboardingCompletion_validUser_success() {
		Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(java.util.Optional.of(testUser));

		User updatedUser = userService.updateOnboardingCompletion(1L, true);

		assertTrue(updatedUser.isOnboardingCompleted());
	}

	/* can later re enable this 
	@Test
	public void createUser_duplicateName_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	*/

	@Test
	public void createUser_duplicateInputs_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void loginUser_validCredentials_setsNewTokenAndOnline() {
		// given
		User storedUser = new User();
		storedUser.setUsername("testUsername");
		storedUser.setPassword(new BCryptPasswordEncoder().encode("password"));
		storedUser.setStatus(UserStatus.OFFLINE);
		storedUser.setToken("old-token");

		User loginRequest = new User();
		loginRequest.setUsername("testUsername");
		loginRequest.setPassword("password");

		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(storedUser);

		// when
		User loggedIn = userService.loginUser(loginRequest);

		// then
		assertEquals(UserStatus.ONLINE, loggedIn.getStatus());
		assertNotNull(loggedIn.getToken());
		assertNotEquals("old-token", loggedIn.getToken());
	}

	@Test
	public void loginUser_invalidPassword_throwsBadRequest() {
		// given
		User storedUser = new User();
		storedUser.setUsername("testUsername");
		storedUser.setPassword(new BCryptPasswordEncoder().encode("password"));

		User loginRequest = new User();
		loginRequest.setUsername("testUsername");
		loginRequest.setPassword("wrong");

		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(storedUser);

		// when/then
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.loginUser(loginRequest));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void loginUser_unknownUsername_throwsBadRequest() {
		// given
		User loginRequest = new User();
		loginRequest.setUsername("unknown");
		loginRequest.setPassword("password");

		Mockito.when(userRepository.findByUsername("unknown")).thenReturn(null);

		// when/then
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.loginUser(loginRequest));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void logoutUser_validToken_clearsTokenAndSetsOffline() {
		// given
		User storedUser = new User();
		storedUser.setToken("active-token");
		storedUser.setStatus(UserStatus.ONLINE);
		Mockito.when(userRepository.findByToken("active-token")).thenReturn(storedUser);

		// when
		userService.logoutUser("active-token");

		// then
		assertNull(storedUser.getToken());
		assertEquals(UserStatus.OFFLINE, storedUser.getStatus());
	}

	@Test
	public void checkTokenAuthenticity_validToken_returnsUser() {
		// given
		User storedUser = new User();
		storedUser.setUsername("testUsername");
		storedUser.setToken("valid-token");
		Mockito.when(userRepository.findByToken("valid-token")).thenReturn(storedUser);

		// when
		User result = userService.checkTokenAuthenticity("valid-token");

		// then
		assertEquals("testUsername", result.getUsername());
	}

	@Test
	public void checkTokenAuthenticity_invalidToken_throwsNotFound() {
		// given
		Mockito.when(userRepository.findByToken("invalid-token")).thenReturn(null);

		// when/then
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.checkTokenAuthenticity("invalid-token"));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	public void getUserById_existingId_returnsUser() {
		// given
		User storedUser = new User();
		storedUser.setId(1L);
		storedUser.setUsername("testUsername");
		Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(storedUser));

		// when
		User result = userService.getUserById(1L);

		// then
		assertEquals(1L, result.getId());
		assertEquals("testUsername", result.getUsername());
	}

	@Test
	public void getUserById_missingId_throwsNotFound() {
		// given
		Mockito.when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

		// when/then
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.getUserById(99L));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

}

package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendsDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InviteDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardEntryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserOnboardingPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.FriendService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private FriendService friendService;

	@Test
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);

		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService -> we define above what the userService should
		// return when getUsers() is called
		given(userService.getUsers()).willReturn(allUsers);

		// when
		MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.onboardingCompleted", is(false)));
	}

	@Test
	public void givenUser_whenGetUserById_thenReturnJson() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setStatus(UserStatus.ONLINE);
		user.setOnboardingCompleted(true);

		given(userService.getUserById(Mockito.anyLong())).willReturn(user);

		// when/then
		mockMvc.perform(get("/users/1").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.onboardingCompleted", is(true)));
	}

	@Test
	public void loginUser_validInput_userLoggedIn() throws Exception {
		// given
		User user = new User();
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);
		user.setOnboardingCompleted(true);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("password");

		given(userService.loginUser(Mockito.any())).willReturn(user);

		// when/then
		MockHttpServletRequestBuilder postRequest = post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.onboardingCompleted", is(true)));
	}

	@Test
	public void updateOnboardingCompletion_validInput_userUpdated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setStatus(UserStatus.ONLINE);
		user.setOnboardingCompleted(true);

		UserOnboardingPutDTO onboardingPutDTO = new UserOnboardingPutDTO();
		onboardingPutDTO.setOnboardingCompleted(true);

		given(userService.updateOnboardingCompletion(Mockito.anyLong(), Mockito.anyBoolean())).willReturn(user);

		// when/then
		MockHttpServletRequestBuilder putRequest = put("/users/1/onboarding")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(onboardingPutDTO));

		mockMvc.perform(putRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.onboardingCompleted", is(true)));

		verify(userService).updateOnboardingCompletion(Mockito.anyLong(), Mockito.anyBoolean());
	}

	@Test
	public void updateUser_validToken_userUpdated() throws Exception {
		User requester = new User();
		requester.setId(1L);
		requester.setUsername("testUsername");

		User updated = new User();
		updated.setId(1L);
		updated.setUsername("updatedUsername");
		updated.setStatus(UserStatus.ONLINE);

		UserPutDTO userPutDTO = new UserPutDTO();
		userPutDTO.setUsername("updatedUsername");

		given(userService.checkTokenAuthenticity("valid-token")).willReturn(requester);
		given(userService.updateUser(Mockito.anyLong(), Mockito.anyString())).willReturn(updated);

		MockHttpServletRequestBuilder putRequest = put("/users/1")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token")
				.content(asJsonString(userPutDTO));

		mockMvc.perform(putRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is("updatedUsername")));

		verify(userService).updateUser(1L, "updatedUsername");
	}

	@Test
	public void getLeaderboard_validToken_returnsLeaderboard() throws Exception {
		LeaderboardEntryDTO entry = new LeaderboardEntryDTO();
		entry.setRank(1);
		entry.setUsername("player1");
		entry.setPoints(42);

		given(userService.checkTokenAuthenticity("valid-token")).willReturn(new User());
		given(userService.getLeaderboard()).willReturn(Collections.singletonList(entry));

		MockHttpServletRequestBuilder getRequest = get("/leaderboard")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token");

		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].rank", is(1)))
				.andExpect(jsonPath("$[0].username", is("player1")))
				.andExpect(jsonPath("$[0].points", is(42)));
	}

	@Test
	public void getFriendsData_validToken_returnsFriendsData() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");

		FriendsDataGetDTO dto = new FriendsDataGetDTO();
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(user);
		given(friendService.getFriendsData(user)).willReturn(dto);

		MockHttpServletRequestBuilder getRequest = get("/users/friends")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token");

		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.friends").isArray())
				.andExpect(jsonPath("$.incomingRequests").isArray())
				.andExpect(jsonPath("$.outgoingRequests").isArray());
	}

	@Test
	public void declineFriend_validToken_callsService() throws Exception {
		User user = new User();
		user.setUsername("testUsername");
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(user);

		MockHttpServletRequestBuilder postRequest = post("/users/friends/requests/7/decline")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token");

		mockMvc.perform(postRequest)
				.andExpect(status().isOk());
		verify(friendService).declineFriend(7L);
	}

	@Test
	public void acceptFriend_validToken_callsService() throws Exception {
		User user = new User();
		user.setUsername("testUsername");
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(user);

		MockHttpServletRequestBuilder postRequest = post("/users/friends/requests/8/accept")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token");

		mockMvc.perform(postRequest)
				.andExpect(status().isOk());
		verify(friendService).acceptFriend(8L);
	}

	@Test
	public void removeFriend_validToken_callsService() throws Exception {
		User user = new User();
		user.setUsername("testUsername");
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(user);

		MockHttpServletRequestBuilder deleteRequest = delete("/users/friends/9")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token");

		mockMvc.perform(deleteRequest)
				.andExpect(status().isOk());
		verify(friendService).removeFriend(9L);
	}

	@Test
	public void sendFriendRequest_validToken_trimsUsername() throws Exception {
		User user = new User();
		user.setUsername("testUsername");
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(user);

		MockHttpServletRequestBuilder postRequest = post("/users/friends/requests")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token")
				.content("{\"username\":\" receiver \"}");

		mockMvc.perform(postRequest)
				.andExpect(status().isOk());
		verify(friendService).sendFriendRequest(user, "receiver");
	}

	@Test
	public void invite_validToken_trimsBodyValues() throws Exception {
		User user = new User();
		user.setUsername("testUsername");
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(user);

		MockHttpServletRequestBuilder postRequest = post("/friends/invite")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token")
				.content("{\"username\":\" receiver \" , \"gameCode\":\" abc123 \"}");

		mockMvc.perform(postRequest)
				.andExpect(status().isOk());
		verify(friendService).inviteFriend(user, "receiver", "abc123");
	}

	@Test
	public void inviteGet_validToken_returnsInvites() throws Exception {
		User user = new User();
		user.setUsername("testUsername");

		InviteDataGetDTO dto = new InviteDataGetDTO();
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(user);
		given(friendService.getInvites(user)).willReturn(dto);

		MockHttpServletRequestBuilder postRequest = post("/friends/invite/get")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token");

		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invites").isArray());
	}

	@Test
	public void inviteAccept_validToken_returnsGameCode() throws Exception {
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(new User());
		given(friendService.acceptInvite(11L)).willReturn("abc123");

		MockHttpServletRequestBuilder postRequest = post("/friends/invite/accept")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token")
				.content(asJsonString(Map.of("inviteId", 11L)));

		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.gameCode", is("abc123")));
	}

	@Test
	public void inviteDecline_validToken_callsService() throws Exception {
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(new User());

		MockHttpServletRequestBuilder deleteRequest = delete("/friends/invite/decline")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token")
				.content(asJsonString(Map.of("inviteId", 12L)));

		mockMvc.perform(deleteRequest)
				.andExpect(status().isOk());
		verify(friendService).declineInvite(12L);
	}


	@Test
	public void loginUser_invalidCredentials_returns400() throws Exception {
		// given
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("wrong-password");

		given(userService.loginUser(Mockito.any()))
				.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or Password is wrong"));

		// when
		MockHttpServletRequestBuilder postRequest = post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest());
	}

	@Test
	public void givenUnknownUser_whenGetUserById_thenReturn404() throws Exception {
		// given
		given(userService.getUserById(Mockito.anyLong()))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		// when/then
		mockMvc.perform(get("/users/99").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	public void logout_validToken_returns200() throws Exception {
		// given
		User user = new User();
		user.setUsername("testUsername");
		given(userService.checkTokenAuthenticity("valid-token")).willReturn(user);

		// when
		MockHttpServletRequestBuilder postRequest = post("/users/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "valid-token");

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isOk());
		verify(userService).logoutUser("valid-token");
	}

	@Test
	public void healthCheck_returns200() throws Exception {
		// when
		MockHttpServletRequestBuilder getRequest = get("/_ah/health")
				.contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest)
				.andExpect(status().isOk());
	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}
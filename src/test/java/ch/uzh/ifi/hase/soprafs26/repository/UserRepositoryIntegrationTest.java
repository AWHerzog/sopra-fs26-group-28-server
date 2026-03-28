package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

@DataJpaTest
public class UserRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void findByUsername_success() {
		// given
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");
		user.setPassword("password");
		user.setPoints(0);
		user.setCreationDate(LocalDateTime.now());

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByUsername(user.getUsername());

		// then
		assertEquals(found.getUsername(), user.getUsername());
		assertEquals(found.getToken(), user.getToken());
		assertEquals(found.getStatus(), user.getStatus());
	}
}

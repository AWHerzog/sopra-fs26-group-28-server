package ch.uzh.ifi.hase.soprafs26;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Application.class)
class ApplicationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rootEndpoint_returnsHealthMessage() throws Exception {
		mockMvc.perform(get("/").contentType(MediaType.TEXT_PLAIN))
				.andExpect(status().isOk())
				.andExpect(content().string("The application is running."));
	}
}
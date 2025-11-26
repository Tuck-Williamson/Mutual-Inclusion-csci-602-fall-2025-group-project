package edu.citadel.app.digitalocean;

import edu.citadel.main.RestApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RestApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles({"int-test","do-app"})
public class CorsDoAppProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Test
    public void contextLoads() {
    }

    @Test
    public void hasOrigin() {
        String origin = environment.getProperty("app.domain.origin");
        assertEquals(origin, "https://powersofeight.com");
    }

    @Test
    void shouldAllowConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/server/status")
                .header("Origin","https://powersofeight.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://powersofeight.com"));
    }

    @Test
    void shouldRejectUnknownOrigin() throws Exception {
        mockMvc.perform(options("/")
                .header("Origin", "https://evil.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}

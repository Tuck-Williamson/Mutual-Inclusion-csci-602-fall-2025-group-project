package edu.citadel.main;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class RestApiApplicationTests {

    @Value("${info.app.name}")
    private String applicationName;

    @Value("${info.app.description}")
    private String applicationDescription;

    @Value("${info.app.version}")
    private String applicationVersion;

    @Autowired
    private MockMvc mockMvc;

	@Test
	public void contextLoads() {}

    @Test
    public void testStatusEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/status"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.content().string("Hello World"));
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/health"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ok"));
    }

    @Test
    public void testInfoEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/info"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(applicationName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.version").value(applicationVersion))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(applicationDescription));
    }

}


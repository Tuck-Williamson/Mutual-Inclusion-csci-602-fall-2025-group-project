package edu.citadel.main;

import com.jayway.jsonpath.JsonPath;
import edu.citadel.dal.ListItemEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Autowired
    private ListItemEntityRepository listItemEntityRepository;

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

    @Test
    public void testListDeleteNonExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/lists/2112"))
               .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testListDeleteExist() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/list"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the list ID from the response. No need to parse JSON, just grab from the JSON string directly.
        int startIndex = response.indexOf("\"id\":") + 5;
        int endIndex = response.indexOf(",", startIndex);
        String listId = response.substring(startIndex, endIndex);

        // TODO: Once we can set list properties in the post endpoint, test properties of the returned object.
        mockMvc.perform(MockMvcRequestBuilders.delete("/list/" + listId))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testAddListItemToExistingList() throws Exception {
        ResultActions postListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // extract the list id from the response
        Integer listId = JsonPath.read(postListResult.andReturn().getResponse().getContentAsString(), "$.id");


        String listItemJson = """
                {
                    "listItemName": "Test Item",
                    "listItemDescription": "This is a test item."
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/item")
                        .contentType("application/json")
                        .content(listItemJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.listItemName").value("Test Item"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.listItemDesc").value("This is a test item."));

    }

    @Test
    public void testUpdateListItem() throws Exception {
        ResultActions postListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Integer listId = JsonPath.read(postListResult.andReturn().getResponse().getContentAsString(), "$.id");

        ResultActions addListItemResult = mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/item")
                        .contentType("application/json")
                        .content("""
                                {
                                    "listItemName": "Associated Item",
                                    "listItemDescription": "This item should be associated with the list."
                                }
                                """))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Integer listItemId = JsonPath.read(addListItemResult.andReturn().getResponse().getContentAsString(), "$.id");

        // Now let's update the listItem and see if it verifies the association
        String updateListItemJson = """
                {
                    "listItemName": "Updated Item Name"
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.patch("/list/" + listId + "/item/" + listItemId)
                        .contentType("application/json")
                        .content(updateListItemJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.listItemName").value("Updated Item Name"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.listItemDesc").value("This item should be associated with the list."));
    }


    @Test
    public void testDeleteListItem() throws Exception {
        ResultActions postListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Integer listId = JsonPath.read(postListResult.andReturn().getResponse().getContentAsString(), "$.id");

        ResultActions addListItemResult = mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/item")
                        .contentType("application/json")
                        .content("""
                                {
                                    "listItemName": "Item to be deleted",
                                    "listItemDescription": "This item will be deleted in the test."
                                }
                                """))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Integer listItemId = JsonPath.read(addListItemResult.andReturn().getResponse().getContentAsString(), "$.id");

        // Now let's delete the listItem
        mockMvc.perform(MockMvcRequestBuilders.delete("/list/" + listId + "/item/" + listItemId))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Verify that the list item has been deleted from the repository
        assertTrue(listItemEntityRepository.findById(Long.valueOf(listItemId)).isEmpty());
    }

    @Test
    public void testMarkListItemAsCompleted() throws Exception {
        // Create a new list
        ResultActions postListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Integer listId = JsonPath.read(postListResult.andReturn().getResponse().getContentAsString(), "$.id");

        // Add an item to that list
        ResultActions addListItemResult = mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/item")
                        .contentType("application/json")
                        .content("""
                            {
                                "listItemName": "Complete me",
                                "listItemDescription": "This item will be marked complete."
                            }
                            """))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Integer listItemId = JsonPath.read(addListItemResult.andReturn().getResponse().getContentAsString(), "$.id");

        // Mark the item as completed
        mockMvc.perform(MockMvcRequestBuilders.patch("/list/" + listId + "/item/" + listItemId + "/complete")
                        .param("completed", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.completed").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.completedOn").exists());

        // Optionally test marking it incomplete again
        mockMvc.perform(MockMvcRequestBuilders.patch("/list/" + listId + "/item/" + listItemId + "/complete")
                        .param("completed", "false"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.completed").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.completedOn").doesNotExist());
    }



}


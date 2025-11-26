package edu.citadel.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import edu.citadel.dal.AccountRepository;
import edu.citadel.dal.ListItemEntityRepository;
import edu.citadel.dal.model.Account;
import edu.citadel.dal.model.Login;
import edu.citadel.dal.model.LoginProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the REST API application.
 * Uses MockMvc to simulate HTTP requests and validate responses.
 * Tests cover status endpoints and list/list item operations.
 * TODO: Add OAuth2 security tests once implemented.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("int-test")
@WithMockUser(username = "testUser", roles = {"USER"})// mocking a user for security context
public class RestApiApplicationTests {

    private static final Logger logger =
            LoggerFactory.getLogger(RestApiApplicationTests.class);

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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

	@Test
	public void contextLoads() {}

    @Test
    public void testPullsInDefault() {

        Optional<Account> guestAccount = accountRepository
                .findByLoginLoginIdAndLoginLoginProvider(0L, LoginProvider.ROOT);
        assertTrue(guestAccount.isPresent());
        guestAccount.map(guest -> {
            try {
                logger.info("Guest account found: \n{}", objectMapper
                        .writer()
                        .withDefaultPrettyPrinter().
                        writeValueAsString(guest));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return guest;
        }).orElseThrow(AssertionError::new);
    }

    @Test
    public void testSavesNewAccount() {
        long n = accountRepository.count();
        // Generate a new account
        Account newAccount = new Account();
        Arrays.stream(newAccount.getClass().getDeclaredFields())
                .forEach(field ->{
                    // not trying to generate ids
                    if (field.getName().equals("user_id") ||
                    Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isFinal(field.getModifiers())) {
                        return;
                    }
                    field.setAccessible(true);
                    try{
                        if (field.getType().equals(String.class)) {
                            field.set(newAccount, "test-string");
                        } else if (field.getType().equals(Long.class)) {
                            field.set(newAccount, 123456L);
                        } else if (field.getType().equals(Timestamp.class)) {
                            field.set(newAccount, Timestamp.valueOf(
                                    LocalDateTime.now()));
                        } else if (field.getType().equals(Login.class)) {
                            Login login = new Login();
                            login.setLoginProvider(LoginProvider.ROOT);
                            login.setLoginId(Long.MAX_VALUE);
                            field.set(newAccount, login);
                        }
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                });
        // now try to save it
        Account result = accountRepository.save(newAccount);
        assertTrue(result != null);
        assertEquals(result.getUsername(), newAccount.getUsername());
        // Now we must delete it
        accountRepository.delete(result);
        assertEquals(accountRepository.count(), n);

    }

    @Test
    public void testStatusEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/server/status"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.content().string("Hello World"));
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/server/health"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ok"));
    }

    @Test
    public void testInfoEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/server/info"))
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
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New List\"}"))
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
        ResultActions postListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New List\"}"))
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
        ResultActions postListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New List\"}"))
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
        ResultActions postListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New List\"}"))
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
        ResultActions postListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New List\"}"))
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

    @Test
    public void testInvalidListShare() throws Exception {
        // Create a new list to then delete so that we know the list id is not valid.
        ResultActions postListResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/list")
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "one")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Shared List\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Integer listId = JsonPath.read(postListResult.andReturn().getResponse().getContentAsString(), "$.id");

        // Try and share the list without a user.
        mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/share"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        // Try and share the list with a bad user.
        mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/share")
                        .with(oauth2Login()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());


        // Try and share the list with a non-owner user.
        // Todo: Uncomment the following when the DB stuff for users has been added.
//        mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/share")
//                .with(oauth2Login().attributes(attrs -> attrs.put("login", "two")))
//        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());

        ResultActions deleteListResult = mockMvc.perform(MockMvcRequestBuilders.delete("/list/" + listId)
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "one"))))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Now try and share the deleted list
        mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/share")
                .with(oauth2Login().attributes(attrs -> attrs.put("login", "one")))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());

        // Test bad accept link
        mockMvc.perform(MockMvcRequestBuilders.get("/list/accept/badtoken")
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "one"))))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Test delete with no user
        mockMvc.perform(MockMvcRequestBuilders.delete("/list/share/badtoken"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    public void testShareListAndDelete() throws Exception {
        // Create a new list first
        ResultActions postListResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/list")
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "one")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Shared List\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Integer listId = JsonPath.read(postListResult.andReturn().getResponse().getContentAsString(), "$.id");

        ResultActions shareListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/share")
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "one"))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.expiryTime").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.link").isNotEmpty());

        String shareLink = "/" + JsonPath.read(shareListResult.andReturn().getResponse().getContentAsString(), "$.link");
        String token = JsonPath.read(shareListResult.andReturn().getResponse().getContentAsString(), "$.token");

        // Test link redirects

        // With no user has an error message.
        mockMvc.perform(MockMvcRequestBuilders.get(shareLink))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/?error=**"));

        // With the owner user has redirected to the list share page.
        mockMvc.perform(MockMvcRequestBuilders.get(shareLink)
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "one"))))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/?v=" + listId + "&share=true"));

        // With a new user has redirection to the list page.
        mockMvc.perform(MockMvcRequestBuilders.get(shareLink)
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "two"))))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/?v=" + listId + "&note=**"));

        // Now ensure that the list cannot be re-shared from the same token.
        // Todo: Uncomment the following when the DB stuff for users has been added.
//        mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/share")
//                .with(userOne))
//                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.delete("/list/share/" + token)
                .with(oauth2Login().attributes(attrs -> attrs.put("login", "one"))))
                .andExpect(MockMvcResultMatchers.status().isFound());

        // Now check and make sure that the share can be rejected.
        shareListResult = mockMvc.perform(MockMvcRequestBuilders.post("/list/" + listId + "/share")
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "one"))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.expiryTime").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.link").isNotEmpty());

        shareLink = "/" + JsonPath.read(shareListResult.andReturn().getResponse().getContentAsString(), "$.link");
        token = JsonPath.read(shareListResult.andReturn().getResponse().getContentAsString(), "$.token");

        mockMvc.perform(MockMvcRequestBuilders.get(shareLink)
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "two"))))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/?v=" + listId + "&note=**"));

        // test deleting the share with the non-owner.
        mockMvc.perform(MockMvcRequestBuilders.delete("/list/share/" + token)
                        .with(oauth2Login().attributes(attrs -> attrs.put("login", "two"))))
                .andExpect(MockMvcResultMatchers.status().isFound());
    }

}


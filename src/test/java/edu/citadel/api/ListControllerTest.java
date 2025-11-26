package edu.citadel.api;

import edu.citadel.api.ListController.UpdateListRequest;
import edu.citadel.api.request.ListItemRequestBody;
import edu.citadel.api.websocket.ListUpdatePublisher;
import edu.citadel.dal.AccountRepository;
import edu.citadel.dal.ListEntityRepository;
import edu.citadel.dal.ListItemEntityRepository;
import edu.citadel.dal.model.ListEntity;
import edu.citadel.dal.model.ListItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.sql.Timestamp;
import java.util.Optional;
import edu.citadel.api.request.CreateListRequest;


import static org.junit.jupiter.api.Assertions.*;

class ListControllerTest {

    @Mock
    private ListEntityRepository mockListEntityRepository; // Mocked dependency

    @Mock
    private ListItemEntityRepository mockListItemEntityRepository;

    @Mock
    private ListUpdatePublisher mockUpdateListRequest;

    @Mock
    private AccountRepository mockAccountRepository;

    private ListController instance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        instance = new ListController(mockListEntityRepository, mockListItemEntityRepository, mockUpdateListRequest, mockAccountRepository);
    }

    private OAuth2User mockPrincipal(String username) {
        OAuth2User principal = Mockito.mock(OAuth2User.class);
        Mockito.when(principal.getAttribute("login")).thenReturn(username);
        return principal;
    }

    @Test
    void createList_HappyPath() {
        Timestamp mockedTimestamp = Timestamp.valueOf("1234-01-01 00:00:00");
        Mockito.when(mockListEntityRepository.save(Mockito.any(ListEntity.class)))
                .thenAnswer(invocation -> {
                    ListEntity arg = invocation.getArgument(0);
                    arg.setId(1L); // Simulate setting an ID upon saving
                    arg.setCreatedOn(mockedTimestamp); // Set the mocked timestamp
                    return arg;
                });
        OAuth2User principal = mockPrincipal("testUser");
        ResponseEntity<ListEntity> response = instance.createList(null, principal);
        ListEntity result = response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, result.getId());
        assertEquals("New List", result.getTitle());
        assertEquals(mockedTimestamp, result.getCreatedOn());
        assertNull(result.getCompletedOn());
        assertNotNull(result.getListItems());
        assertTrue(result.getListItems().isEmpty());
        assertEquals("testUser", result.getOwnerUsername());
    }

    @Test
    void createList_WithCustomTitle() {
        Timestamp mockedTimestamp = Timestamp.valueOf("2024-01-01 00:00:00");

        Mockito.when(mockListEntityRepository.save(Mockito.any(ListEntity.class)))
                .thenAnswer(invocation -> {
                    ListEntity arg = invocation.getArgument(0);
                    arg.setId(2L);
                    arg.setCreatedOn(mockedTimestamp);
                    return arg;
                });

        CreateListRequest body = new CreateListRequest();
        body.setTitle("Weekend Tasks");
        OAuth2User principal = mockPrincipal("testUser2");
        ResponseEntity<ListEntity> response = instance.createList(body, principal);
        ListEntity result = response.getBody();

        assertNotNull(result);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2L, result.getId());
        assertEquals("Weekend Tasks", result.getTitle());
        assertEquals(mockedTimestamp, result.getCreatedOn());
        assertNull(result.getCompletedOn());
        assertNotNull(result.getListItems());
        assertTrue(result.getListItems().isEmpty());
        assertEquals("testUser2", result.getOwnerUsername());
    }


    @Test
    void createList_BodyWithoutTitle_UsesDefault() {
        Timestamp mockedTimestamp = Timestamp.valueOf("2024-01-02 00:00:00");

        Mockito.when(mockListEntityRepository.save(Mockito.any(ListEntity.class)))
                .thenAnswer(invocation -> {
                    ListEntity arg = invocation.getArgument(0);
                    arg.setId(3L);
                    arg.setCreatedOn(mockedTimestamp);
                    return arg;
                });

        CreateListRequest body = new CreateListRequest();
        OAuth2User principal = mockPrincipal("testUser3");
        ResponseEntity<ListEntity> response = instance.createList(body, principal);
        ListEntity result = response.getBody();

        assertNotNull(result);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3L, result.getId());
        assertEquals("New List", result.getTitle()); // Default
        assertEquals(mockedTimestamp, result.getCreatedOn());
        assertNull(result.getCompletedOn());
        assertNotNull(result.getListItems());
        assertTrue(result.getListItems().isEmpty());
        assertEquals("testUser3", result.getOwnerUsername());
    }


    @Test
    void createList_BlankTitle_UsesDefault() {
        Timestamp mockedTimestamp = Timestamp.valueOf("2024-01-04 00:00:00");

        Mockito.when(mockListEntityRepository.save(Mockito.any(ListEntity.class)))
                .thenAnswer(invocation -> {
                    ListEntity arg = invocation.getArgument(0);
                    arg.setId(4L);
                    arg.setCreatedOn(mockedTimestamp);
                    return arg;
                });

        CreateListRequest body = new CreateListRequest();
        body.setTitle("   ");
        OAuth2User principal = mockPrincipal("testUser4");
        ResponseEntity<ListEntity> response = instance.createList(body, principal);
        ListEntity result = response.getBody();

        assertNotNull(result);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4L, result.getId());
        assertEquals("New List", result.getTitle()); // Default still applies
        assertEquals(mockedTimestamp, result.getCreatedOn());
        assertNull(result.getCompletedOn());
        assertNotNull(result.getListItems());
        assertTrue(result.getListItems().isEmpty());
        assertEquals("testUser4", result.getOwnerUsername());
    }





    @Test
    void createListItem_HappyPath() {
        Timestamp mockedTimestamp = Timestamp.valueOf("1234-01-01 00:00:00");
        Mockito.when(mockListEntityRepository.findById(Mockito.any(Long.class)))
                .thenAnswer(invocation -> {
                  Long arg = invocation.getArgument(0);
                    ListEntity listEntity = new ListEntity();
                    listEntity.setId(arg); // Simulate setting an ID upon saving
                    listEntity.setCreatedOn(mockedTimestamp); // Set the mocked timestamp
                    return Optional.of(listEntity);
                });
        Mockito.when(mockListItemEntityRepository.save(Mockito.any(ListItemEntity.class)))
                .thenAnswer(invocation -> {
                    ListItemEntity arg = invocation.getArgument(0);
                    arg.setId(1L); // Simulate setting an ID upon saving
                    arg.setListItemName("New List Item");
                    return arg;
                });
        ResponseEntity<ListItemEntity> result = instance.addListItem(1L, null);
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals("New List Item", result.getBody().getListItemName());
        assertNull(result.getBody().getListItemDesc());
    }
}
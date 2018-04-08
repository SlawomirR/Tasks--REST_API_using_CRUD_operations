package com.crud.tasks.trello.client;

import com.crud.tasks.domain.*;
import com.crud.tasks.trello.config.TrelloConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TrelloClientTest {
    @InjectMocks
    private TrelloClient trelloClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TrelloConfig trelloConfig;

    @Before
    public void init() {
        Mockito.when(trelloConfig.getTrelloAppEndpoint()).thenReturn("http://test.com");
        Mockito.when(trelloConfig.getTrelloAppKey()).thenReturn("test");
        Mockito.when(trelloConfig.getTrelloToken()).thenReturn("test");
        Mockito.when(trelloConfig.getUsername()).thenReturn("USERNAME");
    }

    @Test
    public void shouldFetchTrelloBoards() throws URISyntaxException {
        // Given
        TrelloBoardDto[] trelloBoards = new TrelloBoardDto[1];
        trelloBoards[0] = new TrelloBoardDto("test_board", "test_id", false, new ArrayList<>());

        URI uri = new URI("http://test.com/members/USERNAME/boards?key=test&token=test&fields=name,id,closed&lists=all");

        Mockito.when(restTemplate.getForObject(uri, TrelloBoardDto[].class)).thenReturn(trelloBoards);
        // When
        List<TrelloBoardDto> fetchedTrelloBoards = trelloClient.getTrelloBoards();
        // Then
        assertEquals(1, fetchedTrelloBoards.size());
        assertEquals("test_id", fetchedTrelloBoards.get(0).getId());
        assertEquals("test_board", fetchedTrelloBoards.get(0).getName());
        assertEquals(new ArrayList<>(), fetchedTrelloBoards.get(0).getLists());
    }

    @Test
    public void shouldCreateCard() throws URISyntaxException {
        // Given
        TrelloDto trelloDto = new TrelloDto(0, 0);
        TrelloCardAttachmentByTypeDto attachments = new TrelloCardAttachmentByTypeDto(trelloDto);
        TrelloCardBadgesDto badges = new TrelloCardBadgesDto(0, attachments);
        TrelloCardDto trelloCardDto = new TrelloCardDto(
                "Test task name",
                "Test Description",
                "top",
                "test_id",
                badges
        );

        URI uri = new URI("http://test.com/cards?key=test&token=test&name=Test%20task%20name&desc=Test%20Description&pos=top&idList=test_id");

        CreatedTrelloCard createdTrelloCard = new CreatedTrelloCard(
                "1",
                "Test task name",
                "http://test.com",
                badges
        );

        Mockito.when(restTemplate.postForObject(uri, null, CreatedTrelloCard.class)).thenReturn(createdTrelloCard);
        // When
        CreatedTrelloCard newCard = trelloClient.createNewCard(trelloCardDto);
        // Then
        assertEquals("1", newCard.getId());
        assertEquals("Test task name", newCard.getName());
        assertEquals("http://test.com", newCard.getShortUrl());
    }
}

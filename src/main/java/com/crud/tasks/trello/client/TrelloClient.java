package com.crud.tasks.trello.client;

import com.crud.tasks.domain.CreatedTrelloCard;
import com.crud.tasks.domain.TrelloBoardDto;
import com.crud.tasks.domain.TrelloCardDto;
import com.crud.tasks.trello.config.TrelloConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class TrelloClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrelloClient.class);

    @Autowired
    private TrelloConfig trelloConfig;

    @Autowired
    private RestTemplate restTemplate;

    public CreatedTrelloCard createNewCard(TrelloCardDto trelloCardDto) {
        // without TRY to let front-end to do error verification
        return restTemplate.postForObject(urlTrelloCard(trelloCardDto), null, CreatedTrelloCard.class);
    }

    public List<TrelloBoardDto> getTrelloBoards() {
        try {
            TrelloBoardDto[] boardsResponse = restTemplate.getForObject(urlTrelloBoards(), TrelloBoardDto[].class);
            return Arrays.asList(Optional.ofNullable(boardsResponse).orElse(new TrelloBoardDto[0]));
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private URI urlTrelloBoards() {
        String pathOnServer = "/members/" + trelloConfig.getUsername() + "/boards";
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
        autenticationQueryParam(uriComponentsBuilder);
        uriComponentsBuilder
                .queryParam("fields", "name,id,closed")
                .queryParam("lists", "all");
        return uriComponentsBuilder.buildAndExpand(pathOnServer).encode().toUri();
    }

    private URI urlTrelloCard(TrelloCardDto trelloCardDto) {
        String pathOnServer = "/cards";
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
        autenticationQueryParam(uriComponentsBuilder);
        uriComponentsBuilder
                .queryParam("desc", trelloCardDto.getDescription())
                .queryParam("pos", trelloCardDto.getPos())
                .queryParam("idList", trelloCardDto.getListId());
        return uriComponentsBuilder.buildAndExpand(pathOnServer).encode().toUri();
    }

    private UriComponentsBuilder autenticationQueryParam(UriComponentsBuilder uriComponentsBuilder) {
        String[] trelloAppEndpoint = trelloConfig.getTrelloAppEndpoint().split("/");
        return uriComponentsBuilder
                .scheme(trelloAppEndpoint[0].substring(0, trelloAppEndpoint[0].length() - 1)) // htttps part without ":" at the end
                .host(trelloAppEndpoint[2])
                .path(trelloAppEndpoint[3])
                .path("{path}")
                .queryParam("key", trelloConfig.getTrelloAppKey())
                .queryParam("token", trelloConfig.getTrelloToken());
    }
}

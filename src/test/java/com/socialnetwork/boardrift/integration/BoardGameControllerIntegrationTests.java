package com.socialnetwork.boardrift.integration;

import com.socialnetwork.boardrift.feign.BGGV1ApiConnection;
import com.socialnetwork.boardrift.feign.BGGV2ApiConnection;
import com.socialnetwork.boardrift.rest.model.BGGSearchResponse;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class BoardGameControllerIntegrationTests {
    @MockBean
    BGGV1ApiConnection bggv1ApiConnection;

    @MockBean
    BGGV2ApiConnection bggv2ApiConnection;

    @Autowired
    MockMvc mockMvc;

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void searchBoardGamesShouldSucceed() throws Exception {
        Mockito.when(bggv2ApiConnection.searchForBoardGames(any(), any())).thenReturn(new BGGSearchResponse());

        mockMvc.perform(get("/board-games/search")
                        .param("query", "test"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void getBoardGameByIdShouldSucceed() throws Exception {
        BGGThingResponse bggThingResponse = new BGGThingResponse();
        bggThingResponse.setItems(List.of(new BGGThingResponse.BoardGame()));
        Mockito.when(bggv1ApiConnection.getBoardGameById(any())).thenReturn(bggThingResponse);

        mockMvc.perform(get("/board-games/1"))
                .andExpect(status().isOk());
    }
}

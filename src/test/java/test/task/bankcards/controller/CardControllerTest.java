package test.task.bankcards.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import test.task.bankcards.config.SecurityConfig;
import test.task.bankcards.dto.response.CardResponse;
import test.task.bankcards.security.JwtAuthFilter;
import test.task.bankcards.security.JwtProvider;
import test.task.bankcards.service.CardService;
import test.task.bankcards.util.enums.CardStatus;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
@ContextConfiguration(classes = {CardController.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private CardResponse buildResponse() {
        CardResponse response = new CardResponse();
        response.setMaskedCardNumber("400000******1234");
        response.setHolder("JOHN DOE");
        response.setExpirationDate(LocalDate.now().plusYears(3));
        response.setStatus(CardStatus.ACTIVE);
        response.setBalance(BigDecimal.valueOf(1000));
        return response;
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should create card")
    void createCard_success() throws Exception {

        CardResponse response = buildResponse();
        when(cardService.createCard()).thenReturn(response);

        mockMvc.perform(post("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedCardNumber").value("400000******1234"))
                .andExpect(jsonPath("$.holder").value("JOHN DOE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardService).createCard();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return paged cards")
    void getAll_success() throws Exception {

        CardResponse response = buildResponse();
        Page<CardResponse> page =
                new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(cardService.getAll(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].holder").value("JOHN DOE"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(cardService).getAll(any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return card by id")
    void getOneById_success() throws Exception {

        CardResponse response = buildResponse();
        when(cardService.getOneById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("JOHN DOE"));

        verify(cardService).getOneById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTotalBalance_success() throws Exception {

        when(cardService.getTotalBalance()).thenReturn(BigDecimal.valueOf(5000));

        mockMvc.perform(get("/api/cards/balance/get/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"));

        verify(cardService).getTotalBalance();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_success() throws Exception {

        CardResponse response = buildResponse();
        when(cardService.activateCard(1L)).thenReturn(response);

        mockMvc.perform(put("/api/cards/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardService).activateCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_success() throws Exception {

        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(1L);
    }

}

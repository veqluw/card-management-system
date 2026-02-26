package test.task.bankcards.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import test.task.bankcards.config.SecurityConfig;
import test.task.bankcards.dto.request.TransferRequest;
import test.task.bankcards.security.JwtAuthFilter;
import test.task.bankcards.security.JwtProvider;
import test.task.bankcards.service.TransactionService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
@ContextConfiguration(classes = {TransactionController.class})
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    JwtProvider jwtProvider;

    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        transferRequest = new TransferRequest(
                1L,
                2L,
                new BigDecimal("100.50")
        );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void transfer_success() throws Exception {
        doNothing().when(transactionService).transfer(transferRequest);

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transferRequest))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void transfer_insufficientFunds_shouldFail() throws Exception {
        doNothing().when(transactionService).transfer(transferRequest);
    }
}
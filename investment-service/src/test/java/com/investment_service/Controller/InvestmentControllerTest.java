package com.investment_service.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.investment_service.DTO.CreateInvestmentRequest;
import com.investment_service.DTO.StartupDto;
import com.investment_service.DTO.UpdateInvestmentStatusRequest;
import com.investment_service.Entity.Investment;
import com.investment_service.Feign.StartupClient;
import com.investment_service.Service.InvestmentService;

@ExtendWith(MockitoExtension.class)
class InvestmentControllerTest {

    @Mock
    private InvestmentService service;

    @Mock
    private StartupClient startupClient;

    private InvestmentController controller;
    private Investment investment;

    @BeforeEach
    void setUp() {
        controller = new InvestmentController(service, startupClient);
        investment = investment();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("investor@test.com", null));
    }

    @Test
    void investCreatesInvestmentForAuthenticatedUser() {
        CreateInvestmentRequest request = new CreateInvestmentRequest();
        request.setStartupId(10L);
        request.setAmount(50000.0);
        when(service.invest(request, "investor@test.com")).thenReturn(investment);
        when(startupClient.getStartup(10L)).thenReturn(startup("NewCo"));

        var response = controller.invest(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/investments/1", response.getHeaders().getLocation().toString());
        assertEquals("NewCo", response.getBody().getStartupName());
    }

    @Test
    void listEndpointsMapInvestmentsAndHandleMissingStartupName() {
        when(service.getUserInvestments("investor@test.com")).thenReturn(List.of(investment));
        when(service.getAllInvestments()).thenReturn(List.of(investment));
        when(service.getStartupInvestments(10L)).thenReturn(List.of(investment));
        when(startupClient.getStartup(10L)).thenThrow(new RuntimeException("down"));

        assertEquals(1, controller.myInvestments().getBody().size());
        assertEquals(1, controller.allInvestments().getBody().size());
        assertEquals(null, controller.startupInvestments(10L).getBody().get(0).getStartupName());
    }

    @Test
    void updateStatusMapsUpdatedInvestment() {
        UpdateInvestmentStatusRequest request = new UpdateInvestmentStatusRequest();
        request.setStatus("APPROVED");
        investment.setStatus("APPROVED");
        when(service.updateStatus(1L, "APPROVED")).thenReturn(investment);
        when(startupClient.getStartup(10L)).thenReturn(startup("NewCo"));

        assertEquals("APPROVED", controller.updateStatus(1L, request).getBody().getStatus());
    }

    private Investment investment() {
        Investment value = new Investment();
        value.setId(1L);
        value.setStartupId(10L);
        value.setInvestorEmail("investor@test.com");
        value.setInvestorFirm("Seed Fund");
        value.setFounderEmail("founder@test.com");
        value.setAmount(50000.0);
        value.setStatus("PENDING");
        value.setCreatedAt(LocalDateTime.now());
        return value;
    }

    private StartupDto startup(String name) {
        StartupDto dto = new StartupDto();
        dto.setId(10L);
        dto.setName(name);
        return dto;
    }
}

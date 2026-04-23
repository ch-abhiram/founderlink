package com.investment_service.Service;

import com.investment_service.Config.RabbitConfig;
import com.investment_service.DTO.CreateInvestmentRequest;
import com.investment_service.DTO.StartupDto;
import com.investment_service.Entity.Investment;
import com.investment_service.Feign.StartupClient;
import com.investment_service.Repository.InvestmentRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository repository;

    @Mock
    private StartupClient startupClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private InvestmentService investmentService;

    private CreateInvestmentRequest createRequest;
    private StartupDto startupDto;
    private Investment investment;

    @BeforeEach
    void setUp() {
        createRequest = new CreateInvestmentRequest();
        createRequest.setStartupId(10L);
        createRequest.setAmount(50000.0);
        createRequest.setInvestorFirm("Sequoia Capital");

        startupDto = new StartupDto();
        startupDto.setId(10L);
        startupDto.setName("NewCo");
        startupDto.setFounderEmail("founder@test.com");

        investment = new Investment();
        investment.setId(1L);
        investment.setStartupId(10L);
        investment.setInvestorEmail("investor@test.com");
        investment.setAmount(50000.0);
        investment.setStatus("PENDING");
    }

    private void setupSecurityContext(String email, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testInvest_Success() {
        when(startupClient.getStartup(10L)).thenReturn(startupDto);
        when(repository.save(any(Investment.class))).thenAnswer(i -> {
            Investment inv = i.getArgument(0);
            inv.setId(1L);
            return inv;
        });

        Investment result = investmentService.invest(createRequest, "investor@test.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("investor@test.com", result.getInvestorEmail());
        assertEquals("Sequoia Capital", result.getInvestorFirm());
        assertEquals("founder@test.com", result.getFounderEmail());
        assertEquals("PENDING", result.getStatus());

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.ROUTING_KEY), payloadCaptor.capture());
        assertEquals(50000.0, payloadCaptor.getValue().get("amount"));
    }

    @Test
    void testInvest_FounderCannotInvestInOwnStartup() {
        startupDto.setFounderEmail("investor@test.com");
        when(startupClient.getStartup(10L)).thenReturn(startupDto);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                investmentService.invest(createRequest, "investor@test.com"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Founders cannot invest in their own startup", exception.getReason());
        verify(repository, never()).save(any());
    }

    @Test
    void testInvest_DuplicateInvestmentThrowsConflict() {
        when(startupClient.getStartup(10L)).thenReturn(startupDto);
        when(repository.findByInvestorEmailAndStartupId("investor@test.com", 10L)).thenReturn(Optional.of(investment));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                investmentService.invest(createRequest, "investor@test.com"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("You have already submitted an investment request for this startup", exception.getReason());
        verify(repository, never()).save(any());
    }

    @Test
    void testInvest_StartupNotFound() {
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(startupClient.getStartup(10L)).thenThrow(notFound);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            investmentService.invest(createRequest, "investor@test.com");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void testInvest_StartupServiceUnavailable() {
        FeignException upstreamError = mock(FeignException.class);
        when(startupClient.getStartup(10L)).thenThrow(upstreamError);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            investmentService.invest(createRequest, "investor@test.com");
        });

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("Unable to load startup details", exception.getReason());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatus_Success_Founder() {
        setupSecurityContext("founder@test.com", "FOUNDER");
        
        when(repository.findById(1L)).thenReturn(Optional.of(investment));
        when(startupClient.getStartup(10L)).thenReturn(startupDto);
        when(repository.save(any(Investment.class))).thenAnswer(i -> i.getArgument(0));

        Investment result = investmentService.updateStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", result.getStatus());
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitConfig.EXCHANGE), eq("investment.status"), any(Map.class));
    }

    @Test
    void testUpdateStatus_Forbidden_Investor() {
        setupSecurityContext("investor@test.com", "INVESTOR");

        when(repository.findById(1L)).thenReturn(Optional.of(investment));
        when(startupClient.getStartup(10L)).thenReturn(startupDto);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            investmentService.updateStatus(1L, "COMPLETED");
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatus_StartupServiceUnavailable() {
        setupSecurityContext("founder@test.com", "FOUNDER");

        FeignException upstreamError = mock(FeignException.class);
        when(repository.findById(1L)).thenReturn(Optional.of(investment));
        when(startupClient.getStartup(10L)).thenThrow(upstreamError);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            investmentService.updateStatus(1L, "COMPLETED");
        });

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("Unable to load startup details", exception.getReason());
        verify(repository, never()).save(any());
    }
}

package com.investment_service.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.investment_service.Config.RabbitConfig;
import com.investment_service.DTO.CreateInvestmentRequest;
import com.investment_service.DTO.StartupDto;
import com.investment_service.Entity.Investment;
import com.investment_service.Feign.StartupClient;
import com.investment_service.Repository.InvestmentRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private static final List<String> ALLOWED_STATUSES = List.of("PENDING", "SUCCESS", "FAILED", "COMPLETED", "APPROVED", "REJECTED");

    private final InvestmentRepository repository;
    private final StartupClient startupClient;
    private final RabbitTemplate rabbitTemplate;

    public Investment invest(CreateInvestmentRequest request, String email) {

        StartupDto startup;
        try {
            startup = startupClient.getStartup(request.getStartupId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }

        Investment investment = new Investment();
        investment.setStartupId(request.getStartupId());
        investment.setInvestorEmail(email);
        investment.setAmount(request.getAmount());

        Investment saved = repository.save(investment);

        Map<String, Object> event = new HashMap<>();
        event.put("investmentId", saved.getId());
        event.put("startupId", saved.getStartupId());
        event.put("investorEmail", saved.getInvestorEmail());
        event.put("amount", saved.getAmount());
        event.put("status", saved.getStatus());
        event.put("founderEmail", startup.getFounderEmail());

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                event
        );

        return saved;
    }

    public List<Investment> getUserInvestments(String email) {
        return repository.findByInvestorEmail(email);
    }

    public List<Investment> getStartupInvestments(Long startupId) {
        StartupDto startup;
        try {
            startup = startupClient.getStartup(startupId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }
        
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!startup.getFounderEmail().equals(currentUser) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the founder can view startup investors");
        }

        return repository.findByStartupId(startupId);
    }

    public Investment updateStatus(Long id, String status) {
        Investment investment = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investment not found"));

        StartupDto startup;
        try {
            startup = startupClient.getStartup(investment.getStartupId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!startup.getFounderEmail().equals(currentUser) && !isAdmin) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only founder or admin can update investment status");
        }

        investment.setStatus(normalizeStatus(status));
        Investment saved = repository.save(investment);

        Map<String, Object> event = new HashMap<>();
        event.put("investmentId", saved.getId());
        event.put("startupId", saved.getStartupId());
        event.put("investorEmail", saved.getInvestorEmail());
        event.put("status", saved.getStatus());

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                "investment.status",
                event
        );

        return saved;
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid investment status");
        }
        return normalized;
    }
}

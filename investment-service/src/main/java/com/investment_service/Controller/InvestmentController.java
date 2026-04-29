package com.investment_service.Controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.investment_service.Entity.Investment;
import com.investment_service.DTO.CreateInvestmentRequest;
import com.investment_service.DTO.InvestmentResponseDTO;
import com.investment_service.DTO.StartupDto;
import com.investment_service.DTO.UpdateInvestmentStatusRequest;
import com.investment_service.Feign.StartupClient;
import com.investment_service.Service.InvestmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService service;
    private final StartupClient startupClient;

    @PostMapping
    public ResponseEntity<InvestmentResponseDTO> invest(@RequestBody @Valid CreateInvestmentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Investment investment = service.invest(request, email);
        return ResponseEntity
                .created(URI.create("/investments/" + investment.getId()))
                .body(toDto(investment));
    }

    @GetMapping("/me")
    public ResponseEntity<List<InvestmentResponseDTO>> myInvestments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(service.getUserInvestments(email).stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvestmentResponseDTO>> allInvestments() {
        return ResponseEntity.ok(service.getAllInvestments().stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/startup/{startupId}")
    public ResponseEntity<List<InvestmentResponseDTO>> startupInvestments(@PathVariable Long startupId) {
        return ResponseEntity.ok(service.getStartupInvestments(startupId).stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<InvestmentResponseDTO> updateStatus(
            @PathVariable Long id, 
            @RequestBody @Valid UpdateInvestmentStatusRequest request) {
        return ResponseEntity.ok(toDto(service.updateStatus(id, request.getStatus())));
    }

    private InvestmentResponseDTO toDto(Investment investment) {
        InvestmentResponseDTO dto = new InvestmentResponseDTO();
        dto.setId(investment.getId());
        dto.setStartupId(investment.getStartupId());
        dto.setStartupName(resolveStartupName(investment.getStartupId()));
        dto.setInvestorEmail(investment.getInvestorEmail());
        dto.setInvestorFirm(investment.getInvestorFirm());
        dto.setFounderEmail(investment.getFounderEmail());
        dto.setAmount(investment.getAmount());
        dto.setStatus(investment.getStatus());
        dto.setCreatedAt(investment.getCreatedAt());
        return dto;
    }

    private String resolveStartupName(Long startupId) {
        try {
            StartupDto startup = startupClient.getStartup(startupId);
            return startup.getName();
        } catch (Exception ex) {
            return null;
        }
    }
}

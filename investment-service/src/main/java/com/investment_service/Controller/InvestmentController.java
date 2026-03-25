package com.investment_service.Controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.investment_service.Entity.Investment;
import com.investment_service.DTO.CreateInvestmentRequest;
import com.investment_service.DTO.InvestmentResponseDTO;
import com.investment_service.DTO.UpdateInvestmentStatusRequest;
import com.investment_service.Service.InvestmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService service;

    @PostMapping
    public ResponseEntity<InvestmentResponseDTO> invest(@RequestBody @Valid CreateInvestmentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(toDto(service.invest(request, email)));
    }

    @GetMapping("/me")
    public ResponseEntity<List<InvestmentResponseDTO>> myInvestments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(service.getUserInvestments(email).stream().map(this::toDto).collect(Collectors.toList()));
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
        dto.setInvestorEmail(investment.getInvestorEmail());
        dto.setAmount(investment.getAmount());
        dto.setStatus(investment.getStatus());
        dto.setCreatedAt(investment.getCreatedAt());
        return dto;
    }
}

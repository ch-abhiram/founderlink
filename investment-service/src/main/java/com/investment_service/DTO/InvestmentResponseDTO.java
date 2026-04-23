package com.investment_service.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InvestmentResponseDTO {
    private Long id;
    private Long startupId;
    private String investorEmail;
    private String investorFirm;
    private String founderEmail;
    private Double amount;
    private String status;
    private LocalDateTime createdAt;
}

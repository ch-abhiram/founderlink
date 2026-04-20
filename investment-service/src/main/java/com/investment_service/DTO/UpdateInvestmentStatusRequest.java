package com.investment_service.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateInvestmentStatusRequest {
    
    @NotBlank(message = "Status cannot be blank")
    @Pattern(
            regexp = "(?i)PENDING|SUCCESS|FAILED|COMPLETED|APPROVED|REJECTED",
            message = "Status must be one of PENDING, SUCCESS, FAILED, COMPLETED, APPROVED, or REJECTED"
    )
    private String status;
}

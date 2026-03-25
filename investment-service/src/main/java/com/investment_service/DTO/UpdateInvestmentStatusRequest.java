package com.investment_service.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateInvestmentStatusRequest {
    
    @NotBlank(message = "Status cannot be blank")
    private String status;
}

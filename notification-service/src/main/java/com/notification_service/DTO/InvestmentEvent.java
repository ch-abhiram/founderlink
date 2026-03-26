package com.notification_service.DTO;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class InvestmentEvent {
    private Long id;
    private Long startupId;
    private String investorEmail;
    private String founderEmail;
    private Double amount;
    private String status;
}

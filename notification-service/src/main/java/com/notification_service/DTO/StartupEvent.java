package com.notification_service.DTO;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StartupEvent {
    private Long startupId;
    private String name;
    private String founderEmail;
    private String status;
}

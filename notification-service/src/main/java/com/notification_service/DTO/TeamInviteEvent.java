package com.notification_service.DTO;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TeamInviteEvent {
    private Long inviteId;
    private Long startupId;
    private String startupName;
    private String founderEmail;
    private String userEmail;
    private String role;
    private String status;
}

package com.messaging_service.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {
    
    @NotNull(message = "Startup ID is required")
    private Long startupId;

    // Optional: if the sender is a startup founder, they can specify the participant they're replying to.
    // If sender is an investor/user, this can be empty as it implies they are the participant.
    private String participantEmail;

    @NotBlank(message = "Message content is required")
    private String content;
}

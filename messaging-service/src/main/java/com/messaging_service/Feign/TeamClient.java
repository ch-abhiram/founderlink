package com.messaging_service.Feign;

import com.messaging_service.DTO.TeamMemberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "TEAM-SERVICE", path = "/team")
public interface TeamClient {

    @GetMapping("/startup/{startupId}")
    List<TeamMemberDto> getStartupTeam(
            @PathVariable("startupId") Long startupId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String userRole
    );
}

package com.startup_service.Feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.startup_service.DTO.TeamMemberDto;

@FeignClient(name = "TEAM-SERVICE", path = "/team")
public interface TeamClient {

    @GetMapping("/startup/{startupId}")
    List<TeamMemberDto> getStartupTeam(
            @PathVariable("startupId") Long startupId,
            @RequestHeader("X-User-Email") String actingEmail,
            @RequestHeader("X-User-Role") String actingRole);
}

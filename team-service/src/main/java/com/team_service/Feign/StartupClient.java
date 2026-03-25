package com.team_service.Feign;

import com.team_service.DTO.StartupDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "startup-service", url = "http://localhost:8084/startups")
public interface StartupClient {
    
    @GetMapping("/{id}")
    StartupDto getStartup(@PathVariable("id") Long id);
}

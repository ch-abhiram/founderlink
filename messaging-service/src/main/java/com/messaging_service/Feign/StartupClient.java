package com.messaging_service.Feign;

import com.messaging_service.DTO.StartupDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "STARTUP-SERVICE", path = "/startups")
public interface StartupClient {
    
    @GetMapping("/{id}")
    StartupDto getStartup(@PathVariable("id") Long id);
}

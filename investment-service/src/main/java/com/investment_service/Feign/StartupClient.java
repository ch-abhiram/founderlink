package com.investment_service.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.investment_service.DTO.StartupDto;

@FeignClient(name = "STARTUP-SERVICE")
public interface StartupClient {

    @GetMapping("/startups/{id}")
    StartupDto getStartup(@PathVariable("id") Long id);
}

package com.startup_service.Listener;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.startup_service.Config.RabbitConfig;
import com.startup_service.Entity.Startup;
import com.startup_service.Repository.StartupRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupInvestmentListener {

    private final StartupRepository repository;

    @RabbitListener(queues = RabbitConfig.APPROVED_QUEUE)
    public void handleApprovedInvestment(Map<String, Object> event) {
        String status = (String) event.get("status");
        if (!"APPROVED".equals(status)) {
            return;
        }

        Long startupId = ((Number) event.get("startupId")).longValue();
        Double amount = ((Number) event.get("amount")).doubleValue();

        Startup startup = repository.findById(startupId).orElse(null);
        if (startup == null) {
            log.warn("Startup not found for id: {}", startupId);
            return;
        }

        startup.setCurrentFunding(startup.getCurrentFunding() + amount);
        repository.save(startup);
        log.info("Updated currentFunding for startup {} by {}", startupId, amount);
    }
}

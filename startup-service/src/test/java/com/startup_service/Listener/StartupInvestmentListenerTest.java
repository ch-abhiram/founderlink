package com.startup_service.Listener;

import com.startup_service.Entity.Startup;
import com.startup_service.Repository.StartupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartupInvestmentListenerTest {

    @Mock
    private StartupRepository repository;

    @InjectMocks
    private StartupInvestmentListener listener;

    @Test
    void testHandleApprovedInvestmentSkipsWhenAmountMissing() {
        listener.handleApprovedInvestment(Map.of(
                "startupId", 1L,
                "status", "APPROVED"
        ));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testHandleApprovedInvestmentUpdatesFundingWhenApproved() {
        Startup startup = new Startup();
        startup.setId(1L);
        startup.setCurrentFunding(100.0);
        when(repository.findById(1L)).thenReturn(Optional.of(startup));

        listener.handleApprovedInvestment(Map.of(
                "startupId", 1L,
                "status", "APPROVED",
                "amount", 25.0
        ));

        verify(repository).save(startup);
    }
}

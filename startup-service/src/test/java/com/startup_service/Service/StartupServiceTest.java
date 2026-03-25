package com.startup_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.startup_service.DTO.CreateStartupRequest;
import com.startup_service.DTO.UserDto;
import com.startup_service.Entity.Startup;
import com.startup_service.Repository.StartupRepository;

@ExtendWith(MockitoExtension.class)
class StartupServiceTest {

    @Mock
    private StartupRepository repository;

    @Mock
    private Wrapper wrapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private StartupService startupService;

    private CreateStartupRequest createRequest;
    private UserDto founderDto;
    private Startup mockStartup;

    @BeforeEach
    void setUp() {
        createRequest = new CreateStartupRequest();
        createRequest.setName("FounderLink");
        createRequest.setDescription("A great platform");
        createRequest.setFundingGoal(1000000.0);
        createRequest.setCategory("Tech");
        createRequest.setCurrentRound("Seed");
        createRequest.setValuation(5000000.0);

        founderDto = new UserDto();
        founderDto.setEmail("founder@test.com");
        founderDto.setRole("ROLE_FOUNDER");

        mockStartup = new Startup();
        mockStartup.setId(1L);
        mockStartup.setName("FounderLink");
        mockStartup.setFounderEmail("founder@test.com");
    }

    @Test
    void testCreateStartup_Success() {
        when(wrapper.fetchUser("founder@test.com")).thenReturn(founderDto);
        when(repository.save(any(Startup.class))).thenAnswer(i -> {
            Startup s = (Startup) i.getArguments()[0];
            s.setId(1L);
            return s;
        });

        Startup created = startupService.create(createRequest, "founder@test.com");

        assertNotNull(created);
        assertEquals("FounderLink", created.getName());
        assertEquals("founder@test.com", created.getFounderEmail());
        
        verify(rabbitTemplate, times(1)).convertAndSend(eq("startup.exchange"), eq("startup.created"), any(Object.class));
    }

    @Test
    void testCreateStartup_Forbidden_Role() {
        founderDto.setRole("ROLE_INVESTOR");
        when(wrapper.fetchUser("investor@test.com")).thenReturn(founderDto);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            startupService.create(createRequest, "investor@test.com");
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testGetById_Found() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockStartup));

        Startup found = startupService.getById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("FounderLink", found.getName());
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            startupService.getById(2L);
        });
    }
}

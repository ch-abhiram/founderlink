package com.startup_service.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import com.startup_service.DTO.UserDto;
import com.startup_service.DTO.CreateStartupRequest;
import com.startup_service.DTO.UpdateStartupRequest;
import com.startup_service.Entity.Startup;
import com.startup_service.Repository.StartupRepository;
import com.startup_service.Util.StartupSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StartupService {

    private static final List<String> ALLOWED_STATUSES = List.of("PENDING", "OPEN", "CLOSED", "REJECTED");

    private final StartupRepository repository;
    private final Wrapper wrapper;
    private final RabbitTemplate rabbitTemplate;

    public Startup create(CreateStartupRequest request, String email) {
        UserDto user = wrapper.fetchUser(email);

        if (user.getRole() == null ||
           (!user.getRole().equals("ROLE_FOUNDER") && !user.getRole().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User not allowed to create startup"
            );
        }

        Startup startup = new Startup();
        startup.setName(request.getName());
        startup.setDescription(request.getDescription());
        startup.setTagline(request.getTagline());
        startup.setLocation(request.getLocation());
        startup.setFoundedYear(request.getFoundedYear());
        startup.setTeamSize(request.getTeamSize() != null ? request.getTeamSize() : 0);
        startup.setMrr(request.getMrr() != null ? request.getMrr() : 0.0);
        startup.setFundingGoal(request.getFundingGoal());
        startup.setCategory(request.getCategory());
        startup.setStage(request.getStage());
        startup.setCurrentRound(request.getCurrentRound());
        startup.setValuation(request.getValuation());
        startup.setFounderEmail(email);

        Startup saved = repository.save(startup);
        
        // Publish event
        Map<String, Object> event = new HashMap<>();
        event.put("startupId", saved.getId());
        event.put("founderEmail", saved.getFounderEmail());
        event.put("name", saved.getName());
        event.put("status", saved.getStatus());
        rabbitTemplate.convertAndSend("startup.exchange", "startup.created", event);

        return saved;
    }

    public Page<Startup> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Startup getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Startup not found"
                ));
    }

    public Page<Startup> search(String category, String status, String currentRound, String stage, Pageable pageable) {
        return repository.findAll(StartupSpecification.search(category, status, currentRound, stage), pageable);
    }

    public Startup update(Long id, UpdateStartupRequest request) {
        Startup existing = getById(id);
        
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!existing.getFounderEmail().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the founder");
        }

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getTagline() != null) existing.setTagline(request.getTagline());
        if (request.getLocation() != null) existing.setLocation(request.getLocation());
        if (request.getFoundedYear() != null) existing.setFoundedYear(request.getFoundedYear());
        if (request.getTeamSize() != null) existing.setTeamSize(request.getTeamSize());
        if (request.getMrr() != null) existing.setMrr(request.getMrr());
        if (request.getFundingGoal() != null) existing.setFundingGoal(request.getFundingGoal());
        if (request.getCategory() != null) existing.setCategory(request.getCategory());
        if (request.getStage() != null) existing.setStage(request.getStage());
        if (request.getCurrentRound() != null) existing.setCurrentRound(request.getCurrentRound());
        if (request.getValuation() != null) existing.setValuation(request.getValuation());

        return repository.save(existing);
    }

    public void delete(Long id) {
        Startup existing = getById(id);
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if(!existing.getFounderEmail().equals(currentUser) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete");
        }
        repository.deleteById(id);
    }

    public Startup updateStatus(Long id, String status) {
        Startup existing = getById(id);
        existing.setStatus(normalizeStatus(status));
        return repository.save(existing);
    }

    public void follow(Long id, String email) {
        Startup existing = getById(id);
        if(!existing.getFollowers().contains(email)) {
            existing.getFollowers().add(email);
            repository.save(existing);
        }
    }

    public void unfollow(Long id, String email) {
        Startup existing = getById(id);
        if(existing.getFollowers().contains(email)) {
            existing.getFollowers().remove(email);
            repository.save(existing);
        }
    }

    public List<String> getFollowers(Long id) {
        return getById(id).getFollowers();
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid startup status");
        }
        return normalized;
    }
}

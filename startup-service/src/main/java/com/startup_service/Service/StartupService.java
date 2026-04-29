package com.startup_service.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;

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
import com.startup_service.Entity.StartupUpdate;
import com.startup_service.Repository.StartupRepository;
import com.startup_service.Repository.StartupUpdateRepository;
import com.startup_service.Util.StartupSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StartupService {

    private static final List<String> ALLOWED_STATUSES = List.of("PENDING", "OPEN", "CLOSED", "REJECTED");

    private final StartupRepository repository;
    private final Wrapper wrapper;
    private final RabbitTemplate rabbitTemplate;
    private final StartupPermissionService permissionService;
    private final StartupUpdateRepository startupUpdateRepository;

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
        startup.setEquityOffered(request.getEquityOffered());
        startup.setWebsiteUrl(request.getWebsiteUrl());
        startup.setLogoUrl(request.getLogoUrl());
        startup.setLinkedinUrl(request.getLinkedinUrl());
        startup.setTwitterUrl(request.getTwitterUrl());
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

    public Page<Startup> getAll(String founderEmail, Pageable pageable) {
        if (founderEmail != null && !founderEmail.isBlank()) {
            return repository.findAll(StartupSpecification.search(null, null, null, null, founderEmail), pageable);
        }
        return repository.findAll(pageable);
    }

    public Page<Startup> getAll(Pageable pageable) {
        return getAll(null, pageable);
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

        permissionService.requireStartupManager(existing);

        List<String> changedFields = new ArrayList<>();

        if (applyIfChanged(existing.getName(), request.getName())) { existing.setName(request.getName()); changedFields.add("name"); }
        if (applyIfChanged(existing.getDescription(), request.getDescription())) { existing.setDescription(request.getDescription()); changedFields.add("description"); }
        if (applyIfChanged(existing.getTagline(), request.getTagline())) { existing.setTagline(request.getTagline()); changedFields.add("tagline"); }
        if (applyIfChanged(existing.getLocation(), request.getLocation())) { existing.setLocation(request.getLocation()); changedFields.add("location"); }
        if (applyIfChanged(existing.getFoundedYear(), request.getFoundedYear())) { existing.setFoundedYear(request.getFoundedYear()); changedFields.add("founded year"); }
        if (applyIfChanged(existing.getTeamSize(), request.getTeamSize())) { existing.setTeamSize(request.getTeamSize()); changedFields.add("team size"); }
        if (applyIfChanged(existing.getMrr(), request.getMrr())) { existing.setMrr(request.getMrr()); changedFields.add("MRR"); }
        if (applyIfChanged(existing.getFundingGoal(), request.getFundingGoal())) { existing.setFundingGoal(request.getFundingGoal()); changedFields.add("funding goal"); }
        if (applyIfChanged(existing.getCategory(), request.getCategory())) { existing.setCategory(request.getCategory()); changedFields.add("category"); }
        if (applyIfChanged(existing.getStage(), request.getStage())) { existing.setStage(request.getStage()); changedFields.add("stage"); }
        if (applyIfChanged(existing.getCurrentRound(), request.getCurrentRound())) { existing.setCurrentRound(request.getCurrentRound()); changedFields.add("funding round"); }
        if (applyIfChanged(existing.getValuation(), request.getValuation())) { existing.setValuation(request.getValuation()); changedFields.add("valuation"); }
        if (applyIfChanged(existing.getEquityOffered(), request.getEquityOffered())) { existing.setEquityOffered(request.getEquityOffered()); changedFields.add("equity offered"); }
        if (applyIfChanged(existing.getWebsiteUrl(), request.getWebsiteUrl())) { existing.setWebsiteUrl(request.getWebsiteUrl()); changedFields.add("website"); }
        if (applyIfChanged(existing.getLogoUrl(), request.getLogoUrl())) { existing.setLogoUrl(request.getLogoUrl()); changedFields.add("logo"); }
        if (applyIfChanged(existing.getLinkedinUrl(), request.getLinkedinUrl())) { existing.setLinkedinUrl(request.getLinkedinUrl()); changedFields.add("LinkedIn"); }
        if (applyIfChanged(existing.getTwitterUrl(), request.getTwitterUrl())) { existing.setTwitterUrl(request.getTwitterUrl()); changedFields.add("Twitter"); }

        Startup saved = repository.save(existing);
        createProfileUpdate(saved, changedFields);
        return saved;
    }

    public void delete(Long id) {
        Startup existing = getById(id);
        String currentUser = permissionService.currentUserEmail();
        if(!existing.getFounderEmail().equals(currentUser) && !permissionService.isAdmin()) {
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

    private boolean applyIfChanged(Object currentValue, Object requestedValue) {
        return requestedValue != null && !Objects.equals(currentValue, requestedValue);
    }

    private void createProfileUpdate(Startup startup, List<String> changedFields) {
        if (changedFields.isEmpty()) {
            return;
        }

        StartupUpdate update = new StartupUpdate();
        update.setStartupId(startup.getId());
        update.setTitle("Startup profile updated");
        update.setContent("Updated " + String.join(", ", changedFields) + ".");
        startupUpdateRepository.save(update);
    }
}

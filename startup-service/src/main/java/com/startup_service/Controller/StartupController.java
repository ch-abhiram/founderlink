package com.startup_service.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.startup_service.DTO.CreateStartupRequest;
import com.startup_service.DTO.UpdateStartupRequest;
import com.startup_service.DTO.StartupResponseDTO;
import com.startup_service.Entity.Startup;
import com.startup_service.Service.StartupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/startups")
@RequiredArgsConstructor
public class StartupController {

    private final StartupService service;

    @PostMapping
    public ResponseEntity<StartupResponseDTO> create(@RequestBody @Valid CreateStartupRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Startup startup = service.create(request, email);
        return ResponseEntity.ok(toDto(startup));
    }

    @GetMapping
    public ResponseEntity<Page<StartupResponseDTO>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable).map(this::toDto));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StartupResponseDTO>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currentRound,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.search(category, status, currentRound, pageable).map(this::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StartupResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(service.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StartupResponseDTO> update(@PathVariable Long id, @RequestBody @Valid UpdateStartupRequest request) {
        return ResponseEntity.ok(toDto(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StartupResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(service.updateStatus(id, "OPEN")));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StartupResponseDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(service.updateStatus(id, "REJECTED")));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<String> follow(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        service.follow(id, email);
        return ResponseEntity.ok("Followed successfully");
    }

    @DeleteMapping("/{id}/unfollow")
    public ResponseEntity<String> unfollow(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        service.unfollow(id, email);
        return ResponseEntity.ok("Unfollowed successfully");
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<String>> getFollowers(@PathVariable Long id) {
        return ResponseEntity.ok(service.getFollowers(id));
    }

    private StartupResponseDTO toDto(Startup startup) {
        StartupResponseDTO dto = new StartupResponseDTO();
        dto.setId(startup.getId());
        dto.setName(startup.getName());
        dto.setDescription(startup.getDescription());
        dto.setFounderEmail(startup.getFounderEmail());
        dto.setFundingGoal(startup.getFundingGoal());
        dto.setCurrentFunding(startup.getCurrentFunding());
        dto.setCategory(startup.getCategory());
        dto.setCurrentRound(startup.getCurrentRound());
        dto.setValuation(startup.getValuation());
        dto.setStatus(startup.getStatus());
        dto.setFollowersCount(startup.getFollowers() != null ? startup.getFollowers().size() : 0);
        dto.setCreatedAt(startup.getCreatedAt());
        return dto;
    }
}

package com.startup_service.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import com.startup_service.DTO.CreateStartupRequest;
import com.startup_service.DTO.CreateStartupDocumentRequest;
import com.startup_service.DTO.CreateStartupUpdateRequest;
import com.startup_service.DTO.StartupDocumentResponseDTO;
import com.startup_service.DTO.UpdateStartupRequest;
import com.startup_service.DTO.StartupResponseDTO;
import com.startup_service.DTO.StartupUpdateResponseDTO;
import com.startup_service.Entity.StartupDocument;
import com.startup_service.Entity.Startup;
import com.startup_service.Entity.StartupUpdate;
import com.startup_service.Service.StartupContentService;
import com.startup_service.Service.StartupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/startups")
@RequiredArgsConstructor
public class StartupController {

    private final StartupService service;
    private final StartupContentService startupContentService;

    @PostMapping
    public ResponseEntity<StartupResponseDTO> create(@RequestBody @Valid CreateStartupRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Startup startup = service.create(request, email);
        return ResponseEntity
                .created(URI.create("/startups/" + startup.getId()))
                .body(toDto(startup));
    }

    @GetMapping
    public ResponseEntity<Page<StartupResponseDTO>> getAll(
            @RequestParam(required = false) String founderEmail,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(founderEmail, pageable).map(this::toDto));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StartupResponseDTO>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currentRound,
            @RequestParam(required = false) String stage,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.search(category, status, currentRound, stage, pageable).map(this::toDto));
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
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

    @PostMapping("/{id}/updates")
    public ResponseEntity<StartupUpdateResponseDTO> createUpdate(
            @PathVariable Long id,
            @RequestBody @Valid CreateStartupUpdateRequest request) {
        StartupUpdate update = startupContentService.createUpdate(id, request);
        return ResponseEntity
                .created(URI.create("/startups/" + id + "/updates/" + update.getId()))
                .body(toDto(update));
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<List<StartupUpdateResponseDTO>> getUpdates(@PathVariable Long id) {
        return ResponseEntity.ok(startupContentService.getUpdates(id).stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<StartupDocumentResponseDTO> createDocument(
            @PathVariable Long id,
            @RequestBody @Valid CreateStartupDocumentRequest request) {
        StartupDocument document = startupContentService.createDocument(id, request);
        return ResponseEntity
                .created(URI.create("/startups/" + id + "/documents/" + document.getId()))
                .body(toDto(document));
    }

    @PostMapping(value = "/{id}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StartupDocumentResponseDTO> uploadDocument(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String docType,
            @RequestParam("file") MultipartFile file) {
        StartupDocument document = startupContentService.uploadDocument(id, name, docType, file);
        return ResponseEntity
                .created(URI.create("/startups/" + id + "/documents/" + document.getId()))
                .body(toDto(document));
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<StartupDocumentResponseDTO>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(startupContentService.getDocuments(id).stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, @PathVariable Long documentId) {
        StartupDocument document = startupContentService.getDocument(id, documentId);
        Resource resource = startupContentService.loadDocumentFile(document);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(document.getName())
                        .build()
                        .toString())
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, @PathVariable Long documentId) {
        startupContentService.deleteDocument(id, documentId);
        return ResponseEntity.noContent().build();
    }

    private StartupResponseDTO toDto(Startup startup) {
        StartupResponseDTO dto = new StartupResponseDTO();
        dto.setId(startup.getId());
        dto.setName(startup.getName());
        dto.setDescription(startup.getDescription());
        dto.setFounderEmail(startup.getFounderEmail());
        dto.setTagline(startup.getTagline());
        dto.setLocation(startup.getLocation());
        dto.setFoundedYear(startup.getFoundedYear());
        dto.setTeamSize(startup.getTeamSize());
        dto.setMrr(startup.getMrr());
        dto.setFundingGoal(startup.getFundingGoal());
        dto.setCurrentFunding(startup.getCurrentFunding());
        dto.setCategory(startup.getCategory());
        dto.setStage(startup.getStage());
        dto.setCurrentRound(startup.getCurrentRound());
        dto.setValuation(startup.getValuation());
        dto.setStatus(startup.getStatus());
        dto.setEquityOffered(startup.getEquityOffered());
        dto.setWebsiteUrl(startup.getWebsiteUrl());
        dto.setLogoUrl(startup.getLogoUrl());
        dto.setLinkedinUrl(startup.getLinkedinUrl());
        dto.setTwitterUrl(startup.getTwitterUrl());
        dto.setFollowersCount(startup.getFollowers() != null ? startup.getFollowers().size() : 0);
        dto.setCreatedAt(startup.getCreatedAt());
        return dto;
    }

    private StartupUpdateResponseDTO toDto(StartupUpdate update) {
        StartupUpdateResponseDTO dto = new StartupUpdateResponseDTO();
        dto.setId(update.getId());
        dto.setStartupId(update.getStartupId());
        dto.setTitle(update.getTitle());
        dto.setContent(update.getContent());
        dto.setCreatedAt(update.getCreatedAt());
        return dto;
    }

    private StartupDocumentResponseDTO toDto(StartupDocument document) {
        StartupDocumentResponseDTO dto = new StartupDocumentResponseDTO();
        dto.setId(document.getId());
        dto.setStartupId(document.getStartupId());
        dto.setName(document.getName());
        dto.setUrl(document.getUrl());
        dto.setDocType(document.getDocType());
        dto.setCreatedAt(document.getCreatedAt());
        return dto;
    }
}

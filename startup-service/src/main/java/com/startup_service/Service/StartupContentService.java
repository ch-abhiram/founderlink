package com.startup_service.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.startup_service.DTO.CreateStartupDocumentRequest;
import com.startup_service.DTO.CreateStartupUpdateRequest;
import com.startup_service.Entity.Startup;
import com.startup_service.Entity.StartupDocument;
import com.startup_service.Entity.StartupUpdate;
import com.startup_service.Repository.StartupDocumentRepository;
import com.startup_service.Repository.StartupRepository;
import com.startup_service.Repository.StartupUpdateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StartupContentService {

    private final StartupRepository startupRepository;
    private final StartupUpdateRepository startupUpdateRepository;
    private final StartupDocumentRepository startupDocumentRepository;

    public StartupUpdate createUpdate(Long startupId, CreateStartupUpdateRequest request) {
        Startup startup = getStartupForWrite(startupId);

        StartupUpdate update = new StartupUpdate();
        update.setStartupId(startup.getId());
        update.setTitle(request.getTitle());
        update.setContent(request.getContent());
        return startupUpdateRepository.save(update);
    }

    public List<StartupUpdate> getUpdates(Long startupId) {
        ensureStartupExists(startupId);
        return startupUpdateRepository.findByStartupIdOrderByCreatedAtDesc(startupId);
    }

    public StartupDocument createDocument(Long startupId, CreateStartupDocumentRequest request) {
        Startup startup = getStartupForWrite(startupId);

        StartupDocument document = new StartupDocument();
        document.setStartupId(startup.getId());
        document.setName(request.getName());
        document.setUrl(request.getUrl());
        document.setDocType(request.getDocType());
        return startupDocumentRepository.save(document);
    }

    public List<StartupDocument> getDocuments(Long startupId) {
        ensureStartupExists(startupId);
        return startupDocumentRepository.findByStartupIdOrderByCreatedAtDesc(startupId);
    }

    private Startup getStartupForWrite(Long startupId) {
        Startup startup = ensureStartupExists(startupId);
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!startup.getFounderEmail().equals(currentUser) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only founder or admin can manage startup content");
        }
        return startup;
    }

    private Startup ensureStartupExists(Long startupId) {
        return startupRepository.findById(startupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found"));
    }
}

package com.startup_service.Service;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
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
    private final StartupPermissionService permissionService;

    @Value("${startup.documents.upload-dir:uploads/startup-documents}")
    private String uploadDir;

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

    public StartupDocument uploadDocument(Long startupId, String name, String docType, MultipartFile file) {
        Startup startup = getStartupForWrite(startupId);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document file is required");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
        String documentName = StringUtils.hasText(name) ? name.trim() : originalFilename;

        StartupDocument document = new StartupDocument();
        document.setStartupId(startup.getId());
        document.setName(documentName);
        document.setDocType(docType);
        document.setUrl("pending");
        StartupDocument saved = startupDocumentRepository.save(document);

        String storedFilename = saved.getId() + "-" + originalFilename.replaceAll("[^A-Za-z0-9._-]", "_");
        Path startupDirectory = uploadRoot().resolve(String.valueOf(startup.getId())).normalize();
        Path target = startupDirectory.resolve(storedFilename).normalize();

        if (!target.startsWith(startupDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid document filename");
        }

        try {
            Files.createDirectories(startupDirectory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            startupDocumentRepository.deleteById(saved.getId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store document");
        }

        saved.setUrl("/startups/" + startup.getId() + "/documents/" + saved.getId() + "/download");
        return startupDocumentRepository.save(saved);
    }

    public List<StartupDocument> getDocuments(Long startupId) {
        ensureStartupExists(startupId);
        return startupDocumentRepository.findByStartupIdOrderByCreatedAtDesc(startupId);
    }

    public StartupDocument getDocument(Long startupId, Long documentId) {
        ensureStartupExists(startupId);
        StartupDocument document = startupDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (!startupId.equals(document.getStartupId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }
        return document;
    }

    public Resource loadDocumentFile(StartupDocument document) {
        Path startupDirectory = uploadRoot().resolve(String.valueOf(document.getStartupId())).normalize();

        try (Stream<Path> files = Files.list(startupDirectory)) {
            Path file = files
                    .filter(path -> path.getFileName().toString().startsWith(document.getId() + "-"))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found"));
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found");
            }
            return resource;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found");
        }
    }

    public void deleteDocument(Long startupId, Long documentId) {
        Startup startup = getStartupForWrite(startupId);
        StartupDocument document = getDocument(startup.getId(), documentId);

        if (isUploadedDocument(document)) {
            deleteDocumentFile(document);
        }
        startupDocumentRepository.deleteById(document.getId());
    }

    private Startup getStartupForWrite(Long startupId) {
        Startup startup = ensureStartupExists(startupId);
        permissionService.requireStartupManager(startup);
        return startup;
    }

    private Startup ensureStartupExists(Long startupId) {
        return startupRepository.findById(startupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found"));
    }

    private Path uploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private boolean isUploadedDocument(StartupDocument document) {
        return document.getUrl() != null && document.getUrl().contains("/documents/" + document.getId() + "/download");
    }

    private void deleteDocumentFile(StartupDocument document) {
        Path startupDirectory = uploadRoot().resolve(String.valueOf(document.getStartupId())).normalize();
        if (!Files.isDirectory(startupDirectory)) {
            return;
        }

        try (Stream<Path> files = Files.list(startupDirectory)) {
            files.filter(path -> path.getFileName().toString().startsWith(document.getId() + "-"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete document file");
                        }
                    });
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete document file");
        }
    }
}

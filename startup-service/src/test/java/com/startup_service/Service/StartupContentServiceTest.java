package com.startup_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.startup_service.DTO.CreateStartupDocumentRequest;
import com.startup_service.DTO.CreateStartupUpdateRequest;
import com.startup_service.Entity.Startup;
import com.startup_service.Entity.StartupDocument;
import com.startup_service.Entity.StartupUpdate;
import com.startup_service.Repository.StartupDocumentRepository;
import com.startup_service.Repository.StartupRepository;
import com.startup_service.Repository.StartupUpdateRepository;

@ExtendWith(MockitoExtension.class)
class StartupContentServiceTest {

    @Mock
    private StartupRepository startupRepository;

    @Mock
    private StartupUpdateRepository startupUpdateRepository;

    @Mock
    private StartupDocumentRepository startupDocumentRepository;

    @Mock
    private StartupPermissionService permissionService;

    @TempDir
    private Path uploadDir;

    private StartupContentService service;
    private Startup startup;

    @BeforeEach
    void setUp() {
        service = new StartupContentService(
                startupRepository,
                startupUpdateRepository,
                startupDocumentRepository,
                permissionService);
        ReflectionTestUtils.setField(service, "uploadDir", uploadDir.toString());

        startup = new Startup();
        startup.setId(7L);
        startup.setName("SignalForge");
    }

    @Test
    void createUpdateRequiresManagerAndPersistsUpdate() {
        CreateStartupUpdateRequest request = new CreateStartupUpdateRequest();
        request.setTitle("Launched beta");
        request.setContent("First customers are live.");

        when(startupRepository.findById(7L)).thenReturn(Optional.of(startup));
        when(startupUpdateRepository.save(any(StartupUpdate.class))).thenAnswer(invocation -> {
            StartupUpdate update = invocation.getArgument(0);
            update.setId(11L);
            return update;
        });

        StartupUpdate result = service.createUpdate(7L, request);

        assertEquals(11L, result.getId());
        assertEquals(7L, result.getStartupId());
        assertEquals("Launched beta", result.getTitle());
        verify(permissionService).requireStartupManager(startup);
    }

    @Test
    void getUpdatesRequiresExistingStartup() {
        StartupUpdate update = new StartupUpdate();
        update.setId(3L);
        when(startupRepository.findById(7L)).thenReturn(Optional.of(startup));
        when(startupUpdateRepository.findByStartupIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(update));

        List<StartupUpdate> updates = service.getUpdates(7L);

        assertEquals(1, updates.size());
        assertEquals(3L, updates.get(0).getId());
    }

    @Test
    void createDocumentRequiresManagerAndPersistsLinkDocument() {
        CreateStartupDocumentRequest request = new CreateStartupDocumentRequest();
        request.setName("Pitch deck");
        request.setUrl("https://example.com/deck.pdf");
        request.setDocType("PITCH_DECK");

        when(startupRepository.findById(7L)).thenReturn(Optional.of(startup));
        when(startupDocumentRepository.save(any(StartupDocument.class))).thenAnswer(invocation -> {
            StartupDocument document = invocation.getArgument(0);
            document.setId(22L);
            return document;
        });

        StartupDocument document = service.createDocument(7L, request);

        assertEquals(22L, document.getId());
        assertEquals("Pitch deck", document.getName());
        assertEquals("https://example.com/deck.pdf", document.getUrl());
        verify(permissionService).requireStartupManager(startup);
    }

    @Test
    void uploadDocumentStoresFileAndUpdatesDownloadUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pitch deck.pdf",
                "application/pdf",
                "deck-content".getBytes());

        when(startupRepository.findById(7L)).thenReturn(Optional.of(startup));
        when(startupDocumentRepository.save(any(StartupDocument.class))).thenAnswer(invocation -> {
            StartupDocument document = invocation.getArgument(0);
            if (document.getId() == null) {
                document.setId(44L);
            }
            return document;
        });

        StartupDocument saved = service.uploadDocument(7L, "", "PITCH_DECK", file);

        assertEquals("pitch deck.pdf", saved.getName());
        assertEquals("/startups/7/documents/44/download", saved.getUrl());
        assertTrue(Files.exists(uploadDir.resolve("7").resolve("44-pitch_deck.pdf")));
    }

    @Test
    void uploadDocumentRejectsMissingFile() {
        when(startupRepository.findById(7L)).thenReturn(Optional.of(startup));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.uploadDocument(7L, "Pitch", "PITCH_DECK", null));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void getDocumentRejectsDocumentFromAnotherStartup() {
        StartupDocument document = new StartupDocument();
        document.setId(9L);
        document.setStartupId(99L);

        when(startupRepository.findById(7L)).thenReturn(Optional.of(startup));
        when(startupDocumentRepository.findById(9L)).thenReturn(Optional.of(document));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getDocument(7L, 9L));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void loadDocumentFileFindsStoredUpload() throws Exception {
        Path startupDir = Files.createDirectories(uploadDir.resolve("7"));
        Files.writeString(startupDir.resolve("44-pitch.pdf"), "deck");

        StartupDocument document = uploadedDocument();

        Resource resource = service.loadDocumentFile(document);

        assertTrue(resource.exists());
        assertEquals("44-pitch.pdf", resource.getFilename());
    }

    @Test
    void deleteDocumentRemovesUploadedFileAndRepositoryRecord() throws Exception {
        Path startupDir = Files.createDirectories(uploadDir.resolve("7"));
        Path file = startupDir.resolve("44-pitch.pdf");
        Files.writeString(file, "deck");

        when(startupRepository.findById(7L)).thenReturn(Optional.of(startup));
        when(startupDocumentRepository.findById(44L)).thenReturn(Optional.of(uploadedDocument()));

        service.deleteDocument(7L, 44L);

        assertTrue(Files.notExists(file));
        verify(startupDocumentRepository).deleteById(44L);
    }

    @Test
    void deleteDocumentDeletesLinkedDocumentWithoutLookingForFile() {
        StartupDocument document = new StartupDocument();
        document.setId(12L);
        document.setStartupId(7L);
        document.setName("External deck");
        document.setUrl("https://example.com/deck.pdf");

        when(startupRepository.findById(7L)).thenReturn(Optional.of(startup));
        when(startupDocumentRepository.findById(12L)).thenReturn(Optional.of(document));

        service.deleteDocument(7L, 12L);

        verify(startupDocumentRepository).deleteById(12L);
    }

    @Test
    void missingStartupReturnsNotFound() {
        when(startupRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getDocuments(404L));

        assertEquals(404, exception.getStatusCode().value());
    }

    private StartupDocument uploadedDocument() {
        StartupDocument document = new StartupDocument();
        document.setId(44L);
        document.setStartupId(7L);
        document.setName("Pitch");
        document.setUrl("/startups/7/documents/44/download");
        return document;
    }
}

package com.startup_service.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.startup_service.DTO.CreateStartupDocumentRequest;
import com.startup_service.DTO.CreateStartupRequest;
import com.startup_service.DTO.CreateStartupUpdateRequest;
import com.startup_service.DTO.UpdateStartupRequest;
import com.startup_service.Entity.Startup;
import com.startup_service.Entity.StartupDocument;
import com.startup_service.Entity.StartupUpdate;
import com.startup_service.Service.StartupContentService;
import com.startup_service.Service.StartupService;

@ExtendWith(MockitoExtension.class)
class StartupControllerTest {

    @Mock
    private StartupService startupService;

    @Mock
    private StartupContentService contentService;

    @TempDir
    private Path tempDir;

    private StartupController controller;
    private Startup startup;

    @BeforeEach
    void setUp() {
        controller = new StartupController(startupService, contentService);
        startup = startup();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("founder@test.com", null));
    }

    @Test
    void createReturnsCreatedStartupDto() {
        CreateStartupRequest request = new CreateStartupRequest();
        when(startupService.create(request, "founder@test.com")).thenReturn(startup);

        var response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/startups/7", response.getHeaders().getLocation().toString());
        assertEquals("SignalForge", response.getBody().getName());
        assertEquals(2, response.getBody().getFollowersCount());
    }

    @Test
    void listAndSearchMapPagesToDtos() {
        when(startupService.getAll(eq("founder@test.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(startup)));
        when(startupService.search(eq("AI"), eq("OPEN"), eq("Seed"), eq("MVP"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(startup)));

        assertEquals("SignalForge", controller.getAll("founder@test.com", Pageable.unpaged()).getBody().getContent().get(0).getName());
        assertEquals("OPEN", controller.search("AI", "OPEN", "Seed", "MVP", Pageable.unpaged()).getBody().getContent().get(0).getStatus());
    }

    @Test
    void getUpdateApproveRejectAndDeleteDelegateToService() {
        when(startupService.getById(7L)).thenReturn(startup);
        when(startupService.update(eq(7L), any(UpdateStartupRequest.class))).thenReturn(startup);
        when(startupService.updateStatus(7L, "OPEN")).thenReturn(startup);
        when(startupService.updateStatus(7L, "REJECTED")).thenReturn(startup);

        assertEquals(7L, controller.getById(7L).getBody().getId());
        assertEquals("SignalForge", controller.update(7L, new UpdateStartupRequest()).getBody().getName());
        assertEquals("OPEN", controller.approve(7L).getBody().getStatus());
        assertEquals("OPEN", controller.reject(7L).getBody().getStatus());
        assertEquals(HttpStatus.NO_CONTENT, controller.delete(7L).getStatusCode());

        verify(startupService).delete(7L);
    }

    @Test
    void followUnfollowAndFollowersUseAuthenticatedUser() {
        when(startupService.getFollowers(7L)).thenReturn(List.of("one@test.com", "two@test.com"));

        assertEquals("Followed successfully", controller.follow(7L).getBody());
        assertEquals("Unfollowed successfully", controller.unfollow(7L).getBody());
        assertEquals(2, controller.getFollowers(7L).getBody().size());

        verify(startupService).follow(7L, "founder@test.com");
        verify(startupService).unfollow(7L, "founder@test.com");
    }

    @Test
    void updatesAndDocumentsAreMappedToDtos() {
        StartupUpdate update = update();
        StartupDocument document = document();
        when(contentService.createUpdate(eq(7L), any(CreateStartupUpdateRequest.class))).thenReturn(update);
        when(contentService.getUpdates(7L)).thenReturn(List.of(update));
        when(contentService.createDocument(eq(7L), any(CreateStartupDocumentRequest.class))).thenReturn(document);
        when(contentService.getDocuments(7L)).thenReturn(List.of(document));

        assertEquals(HttpStatus.CREATED, controller.createUpdate(7L, new CreateStartupUpdateRequest()).getStatusCode());
        assertEquals("Beta", controller.getUpdates(7L).getBody().get(0).getTitle());
        assertEquals(HttpStatus.CREATED, controller.createDocument(7L, new CreateStartupDocumentRequest()).getStatusCode());
        assertEquals("Pitch", controller.getDocuments(7L).getBody().get(0).getName());
    }

    @Test
    void uploadDownloadAndDeleteDocumentDelegateToContentService() throws Exception {
        Path file = tempDir.resolve("pitch.pdf");
        Files.writeString(file, "deck");
        StartupDocument document = document();
        MockMultipartFile upload = new MockMultipartFile("file", "pitch.pdf", "application/pdf", "deck".getBytes());

        when(contentService.uploadDocument(7L, "Pitch", "PITCH_DECK", upload)).thenReturn(document);
        when(contentService.getDocument(7L, 44L)).thenReturn(document);
        when(contentService.loadDocumentFile(document)).thenReturn(new UrlResource(file.toUri()));

        assertEquals(HttpStatus.CREATED, controller.uploadDocument(7L, "Pitch", "PITCH_DECK", upload).getStatusCode());
        var download = controller.downloadDocument(7L, 44L);
        assertEquals("pitch.pdf", download.getBody().getFilename());
        assertEquals("Pitch", download.getHeaders().getContentDisposition().getFilename());
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteDocument(7L, 44L).getStatusCode());

        verify(contentService).deleteDocument(7L, 44L);
    }

    private Startup startup() {
        Startup value = new Startup();
        value.setId(7L);
        value.setName("SignalForge");
        value.setDescription("AI signals");
        value.setFounderEmail("founder@test.com");
        value.setTagline("Better decisions");
        value.setLocation("Bengaluru");
        value.setFoundedYear(2024);
        value.setTeamSize(6);
        value.setMrr(12000.0);
        value.setFundingGoal(500000.0);
        value.setCurrentFunding(100000.0);
        value.setCategory("AI");
        value.setStage("MVP");
        value.setCurrentRound("Seed");
        value.setValuation(2500000.0);
        value.setStatus("OPEN");
        value.setEquityOffered(8.0);
        value.setWebsiteUrl("https://example.com");
        value.setLogoUrl("https://example.com/logo.png");
        value.setLinkedinUrl("https://linkedin.com/company/signalforge");
        value.setTwitterUrl("https://x.com/signalforge");
        value.setFollowers(new ArrayList<>(List.of("one@test.com", "two@test.com")));
        return value;
    }

    private StartupUpdate update() {
        StartupUpdate value = new StartupUpdate();
        value.setId(21L);
        value.setStartupId(7L);
        value.setTitle("Beta");
        value.setContent("Launched");
        value.setCreatedAt(LocalDateTime.now());
        return value;
    }

    private StartupDocument document() {
        StartupDocument value = new StartupDocument();
        value.setId(44L);
        value.setStartupId(7L);
        value.setName("Pitch");
        value.setDocType("PITCH_DECK");
        value.setUrl("/startups/7/documents/44/download");
        value.setCreatedAt(LocalDateTime.now());
        return value;
    }
}

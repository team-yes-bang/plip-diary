package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.out.persistence.theme.DiaryThemePersistenceAdapter;
import com.plip.diary.adapter.out.persistence.video.DiaryVideoPersistenceAdapter;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoServicePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.time.KstDateTimes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ThemeTimelineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiaryThemePersistenceAdapter diaryThemePersistenceAdapter;

    @Autowired
    private DiaryVideoPersistenceAdapter diaryVideoPersistenceAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private VideoServicePort videoMetadataEnrichmentPort;

    private UUID userUuid;

    @BeforeEach
    void setUp() {
        userUuid = UUID.randomUUID();
    }

    @Test
    void getThemeTimeline_returnsDateSectionsWithEnrichment() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );

        UUID aug1VideoUuid = UUID.randomUUID();
        UUID aug2VideoUuid = UUID.randomUUID();
        DiaryVideo aug1Video = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), aug1VideoUuid)
        );
        DiaryVideo aug2Video = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), aug2VideoUuid)
        );

        LocalDateTime aug1KstUtc = KstDateTimes.startOfDay(
                java.time.LocalDate.of(2026, 8, 1)
        ).plusHours(10);
        LocalDateTime aug2KstUtc = KstDateTimes.startOfDay(
                java.time.LocalDate.of(2026, 8, 2)
        ).plusHours(9);
        updateCreatedAt(aug1Video.getId(), aug1KstUtc);
        updateCreatedAt(aug2Video.getId(), aug2KstUtc);

        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), any()))
                .thenReturn(Map.of(
                        aug1VideoUuid, new VideoMetadata(aug1VideoUuid, "8/1 캡션", "https://cdn/aug1.jpg"),
                        aug2VideoUuid, new VideoMetadata(aug2VideoUuid, "8/2 캡션", "https://cdn/aug2.jpg")
                ));

        mockMvc.perform(get("/api/v1/diaries/themes/{id}/timeline", theme.getId())
                        .header(DateTimelineController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(2))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.sections[0].date").value("2026-08-02"))
                .andExpect(jsonPath("$.sections[0].videos[0].caption").value("8/2 캡션"))
                .andExpect(jsonPath("$.sections[1].date").value("2026-08-01"))
                .andExpect(jsonPath("$.sections[1].videos[0].thumbnailUrl").value("https://cdn/aug1.jpg"));
    }

    @Test
    void getThemeTimeline_excludesSoftDeletedVideosAndOtherThemes() throws Exception {
        DiaryTheme targetTheme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryTheme otherTheme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "여행", UUID.randomUUID())
        );

        DiaryVideo active = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(targetTheme.getId(), UUID.randomUUID())
        );
        DiaryVideo deleted = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(targetTheme.getId(), UUID.randomUUID())
        );
        diaryVideoPersistenceAdapter.softDelete(deleted.getId());
        diaryVideoPersistenceAdapter.save(DiaryVideo.create(otherTheme.getId(), UUID.randomUUID()));

        updateCreatedAt(
                active.getId(),
                KstDateTimes.startOfDay(java.time.LocalDate.of(2026, 8, 1)).plusHours(1)
        );

        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), any())).thenReturn(Map.of());

        mockMvc.perform(get("/api/v1/diaries/themes/{id}/timeline", targetTheme.getId())
                        .header(DateTimelineController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(1))
                .andExpect(jsonPath("$.sections[0].videos.length()").value(1));
    }

    @Test
    void getThemeTimeline_emptyWhenNoVideos() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );

        mockMvc.perform(get("/api/v1/diaries/themes/{id}/timeline", theme.getId())
                        .header(DateTimelineController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(0));
    }

    @Test
    void getThemeTimeline_returnsNotFoundForOtherUserTheme() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );

        UUID otherUser = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/diaries/themes/{id}/timeline", theme.getId())
                        .header(DateTimelineController.USER_UUID_HEADER, otherUser))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_001"));
    }

    @Test
    void getThemeTimeline_returnsNotFoundForDeletedTheme() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        diaryThemePersistenceAdapter.softDelete(theme.getId());

        mockMvc.perform(get("/api/v1/diaries/themes/{id}/timeline", theme.getId())
                        .header(DateTimelineController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_001"));
    }

    private void updateCreatedAt(Long videoId, LocalDateTime createdAt) {
        jdbcTemplate.update("UPDATE diary_videos SET created_at = ? WHERE id = ?", createdAt, videoId);
        entityManager.flush();
        entityManager.clear();
    }
}

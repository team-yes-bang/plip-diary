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
class HomeFeedControllerTest {

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
    void getHomeFeed_returnsThreeSectionsIncludingEmptyToday() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );

        UUID yesterdayVideoUuid = UUID.randomUUID();
        UUID twoDaysAgoVideoUuid = UUID.randomUUID();
        DiaryVideo yesterdayVideo = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), yesterdayVideoUuid)
        );
        DiaryVideo twoDaysAgoVideo = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), twoDaysAgoVideoUuid)
        );

        updateCreatedAt(
                yesterdayVideo.getId(),
                KstDateTimes.startOfDay(KstDateTimes.today().minusDays(1)).plusHours(10)
        );
        updateCreatedAt(
                twoDaysAgoVideo.getId(),
                KstDateTimes.startOfDay(KstDateTimes.today().minusDays(2)).plusHours(9)
        );

        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), any()))
                .thenReturn(Map.of(
                        yesterdayVideoUuid, new VideoMetadata(yesterdayVideoUuid, "어제", "https://cdn/y.jpg"),
                        twoDaysAgoVideoUuid, new VideoMetadata(twoDaysAgoVideoUuid, "그제", "https://cdn/t.jpg")
                ));

        mockMvc.perform(get("/api/diaries/home")
                        .header(HomeFeedController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(3))
                .andExpect(jsonPath("$.sections[0].date").value(KstDateTimes.today().toString()))
                .andExpect(jsonPath("$.sections[0].videos.length()").value(0))
                .andExpect(jsonPath("$.sections[1].date").value(KstDateTimes.today().minusDays(1).toString()))
                .andExpect(jsonPath("$.sections[1].videos[0].caption").value("어제"))
                .andExpect(jsonPath("$.sections[2].date").value(KstDateTimes.today().minusDays(2).toString()))
                .andExpect(jsonPath("$.sections[2].videos[0].thumbnailUrl").value("https://cdn/t.jpg"));
    }

    @Test
    void getHomeFeed_returnsTodayOnlyWhenNoVideos() throws Exception {
        mockMvc.perform(get("/api/diaries/home")
                        .header(HomeFeedController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(1))
                .andExpect(jsonPath("$.sections[0].date").value(KstDateTimes.today().toString()))
                .andExpect(jsonPath("$.sections[0].videos.length()").value(0));
    }

    @Test
    void getHomeFeed_skipsEmptyDaysAndLimitsVideosPerSection() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );

        for (int i = 0; i < 5; i++) {
            DiaryVideo video = diaryVideoPersistenceAdapter.save(
                    DiaryVideo.create(theme.getId(), UUID.randomUUID())
            );
            updateCreatedAt(
                    video.getId(),
                    KstDateTimes.startOfDay(KstDateTimes.today()).plusHours(i + 1)
            );
        }
        DiaryVideo twoDaysAgo = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), UUID.randomUUID())
        );
        updateCreatedAt(
                twoDaysAgo.getId(),
                KstDateTimes.startOfDay(KstDateTimes.today().minusDays(2)).plusHours(1)
        );

        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), any())).thenReturn(Map.of());

        mockMvc.perform(get("/api/diaries/home")
                        .header(HomeFeedController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(2))
                .andExpect(jsonPath("$.sections[0].videos.length()").value(4))
                .andExpect(jsonPath("$.sections[1].date").value(KstDateTimes.today().minusDays(2).toString()));
    }

    @Test
    void getHomeFeed_excludesSoftDeletedVideosAndOtherUsers() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        UUID otherUser = UUID.randomUUID();
        DiaryTheme otherTheme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(otherUser, "타인", UUID.randomUUID())
        );

        DiaryVideo active = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), UUID.randomUUID())
        );
        DiaryVideo deleted = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), UUID.randomUUID())
        );
        diaryVideoPersistenceAdapter.softDelete(deleted.getId());
        diaryVideoPersistenceAdapter.save(DiaryVideo.create(otherTheme.getId(), UUID.randomUUID()));

        updateCreatedAt(
                active.getId(),
                KstDateTimes.startOfDay(KstDateTimes.today()).plusHours(1)
        );

        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), any())).thenReturn(Map.of());

        mockMvc.perform(get("/api/diaries/home")
                        .header(HomeFeedController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(1))
                .andExpect(jsonPath("$.sections[0].videos.length()").value(1));
    }

    private void updateCreatedAt(Long videoId, LocalDateTime createdAt) {
        jdbcTemplate.update("UPDATE diary_videos SET created_at = ? WHERE id = ?", createdAt, videoId);
        entityManager.flush();
        entityManager.clear();
    }
}

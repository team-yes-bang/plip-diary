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
class DateTimelineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiaryThemePersistenceAdapter diaryThemePersistenceAdapter;

    @Autowired
    private DiaryVideoPersistenceAdapter diaryVideoPersistenceAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private VideoServicePort videoServicePort;

    private UUID userUuid;

    @BeforeEach
    void setUp() {
        userUuid = UUID.randomUUID();
    }

    @Test
    void getDateTimeline_returnsThemeSectionsWithEnrichment() throws Exception {
        DiaryTheme daily = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryTheme travel = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "여행", UUID.randomUUID())
        );

        UUID dailyVideoUuid = UUID.randomUUID();
        UUID travelVideoUuid = UUID.randomUUID();
        DiaryVideo dailyVideo = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(daily.getId(), dailyVideoUuid)
        );
        DiaryVideo travelVideo = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(travel.getId(), travelVideoUuid)
        );

        LocalDateTime aug1KstUtc = KstDateTimes.startOfDayKstAsUtcLocalDateTime(
                java.time.LocalDate.of(2026, 8, 1)
        ).plusHours(10);
        updateCreatedAt(dailyVideo.getId(), aug1KstUtc);
        updateCreatedAt(travelVideo.getId(), aug1KstUtc.plusHours(2));

        when(videoServicePort.fetchVideoMetadata(eq(userUuid), any()))
                .thenReturn(Map.of(
                        dailyVideoUuid, new VideoMetadata(dailyVideoUuid, "일상 캡션", "https://cdn/daily.jpg"),
                        travelVideoUuid, new VideoMetadata(travelVideoUuid, "여행 캡션", "https://cdn/travel.jpg")
                ));

        mockMvc.perform(get("/api/diaries/dates/2026-08-01")
                        .header(DateTimelineController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-08-01"))
                .andExpect(jsonPath("$.sections.length()").value(2))
                .andExpect(jsonPath("$.sections[0].themeName").value("일상"))
                .andExpect(jsonPath("$.sections[0].videos[0].caption").value("일상 캡션"))
                .andExpect(jsonPath("$.sections[0].videos[0].thumbnailUrl").value("https://cdn/daily.jpg"))
                .andExpect(jsonPath("$.sections[1].themeName").value("여행"))
                .andExpect(jsonPath("$.sections[1].videos[0].caption").value("여행 캡션"));
    }

    @Test
    void getDateTimeline_excludesSoftDeletedVideosAndThemes() throws Exception {
        DiaryTheme activeTheme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryTheme deletedTheme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "삭제됨", UUID.randomUUID())
        );
        diaryVideoPersistenceAdapter.save(DiaryVideo.create(deletedTheme.getId(), UUID.randomUUID()));
        diaryThemePersistenceAdapter.softDelete(deletedTheme.getId());

        DiaryVideo active = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(activeTheme.getId(), UUID.randomUUID())
        );
        DiaryVideo deleted = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(activeTheme.getId(), UUID.randomUUID())
        );
        diaryVideoPersistenceAdapter.softDelete(deleted.getId());

        LocalDateTime aug1KstUtc = KstDateTimes.startOfDayKstAsUtcLocalDateTime(
                java.time.LocalDate.of(2026, 8, 1)
        ).plusHours(5);
        updateCreatedAt(active.getId(), aug1KstUtc);

        when(videoServicePort.fetchVideoMetadata(eq(userUuid), any())).thenReturn(Map.of());

        mockMvc.perform(get("/api/diaries/dates/2026-08-01")
                        .header(DateTimelineController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(1))
                .andExpect(jsonPath("$.sections[0].videos.length()").value(1));
    }

    @Test
    void getDateTimeline_emptyWhenNoVideosOnDate() throws Exception {
        mockMvc.perform(get("/api/diaries/dates/2026-08-01")
                        .header(DateTimelineController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(0));
    }

    @Test
    void getDateTimeline_doesNotReturnOtherUserVideos() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryVideo video = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));
        updateCreatedAt(
                video.getId(),
                KstDateTimes.startOfDayKstAsUtcLocalDateTime(java.time.LocalDate.of(2026, 8, 1)).plusHours(1)
        );

        UUID otherUser = UUID.randomUUID();
        mockMvc.perform(get("/api/diaries/dates/2026-08-01")
                        .header(DateTimelineController.USER_UUID_HEADER, otherUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(0));
    }

    @Test
    void getDateTimeline_invalidDate_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/diaries/dates/2026-13-40")
                        .header(DateTimelineController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }

    private void updateCreatedAt(Long videoId, LocalDateTime createdAt) {
        jdbcTemplate.update("UPDATE diary_videos SET created_at = ? WHERE id = ?", createdAt, videoId);
    }
}

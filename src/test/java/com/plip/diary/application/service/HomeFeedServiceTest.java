package com.plip.diary.application.service;

import com.plip.diary.application.port.in.dto.HomeFeed;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoServicePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.time.KstDateTimes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeFeedServiceTest {

    @Mock
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Mock
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Mock
    private VideoServicePort videoMetadataEnrichmentPort;

    @InjectMocks
    private HomeFeedService homeFeedService;

    @Test
    void getHomeFeed_returnsTodayOnlyWhenNoVideos() {
        UUID userUuid = UUID.randomUUID();

        when(diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(eq(userUuid), any(), any()))
                .thenReturn(List.of());

        HomeFeed result = homeFeedService.getHomeFeed(userUuid);

        assertThat(result.sections()).hasSize(1);
        assertThat(result.sections().get(0).date()).isEqualTo(KstDateTimes.today());
        assertThat(result.sections().get(0).videos()).isEmpty();
        verifyNoInteractions(videoMetadataEnrichmentPort);
    }

    @Test
    void getHomeFeed_includesTodayEvenWhenEmptyWithRecentPastDays() {
        UUID userUuid = UUID.randomUUID();
        LocalDate today = KstDateTimes.today();
        DiaryTheme theme = theme(userUuid, 1L, "일상");
        DiaryVideo yesterdayVideo = video(
                10L, theme.getId(), UUID.randomUUID(),
                KstDateTimes.startOfDay(today.minusDays(1)).plusHours(3)
        );
        DiaryVideo twoDaysAgoVideo = video(
                11L, theme.getId(), UUID.randomUUID(),
                KstDateTimes.startOfDay(today.minusDays(2)).plusHours(5)
        );

        when(diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(eq(userUuid), any(), any()))
                .thenReturn(List.of(yesterdayVideo, twoDaysAgoVideo));
        when(diaryThemePersistencePort.findAllByUserUuid(userUuid)).thenReturn(List.of(theme));
        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), any())).thenReturn(Map.of());

        HomeFeed result = homeFeedService.getHomeFeed(userUuid);

        assertThat(result.sections()).hasSize(3);
        assertThat(result.sections().get(0).date()).isEqualTo(today);
        assertThat(result.sections().get(0).videos()).isEmpty();
        assertThat(result.sections().get(1).date()).isEqualTo(today.minusDays(1));
        assertThat(result.sections().get(2).date()).isEqualTo(today.minusDays(2));
    }

    @Test
    void getHomeFeed_skipsEmptyDaysBetweenWrittenDates() {
        UUID userUuid = UUID.randomUUID();
        LocalDate today = KstDateTimes.today();
        DiaryTheme theme = theme(userUuid, 1L, "일상");
        DiaryVideo todayVideo = video(
                10L, theme.getId(), UUID.randomUUID(),
                KstDateTimes.startOfDay(today).plusHours(1)
        );
        DiaryVideo twoDaysAgoVideo = video(
                11L, theme.getId(), UUID.randomUUID(),
                KstDateTimes.startOfDay(today.minusDays(2)).plusHours(2)
        );
        DiaryVideo fourDaysAgoVideo = video(
                12L, theme.getId(), UUID.randomUUID(),
                KstDateTimes.startOfDay(today.minusDays(4)).plusHours(3)
        );

        when(diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(eq(userUuid), any(), any()))
                .thenReturn(List.of(todayVideo, twoDaysAgoVideo, fourDaysAgoVideo));
        when(diaryThemePersistencePort.findAllByUserUuid(userUuid)).thenReturn(List.of(theme));
        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), any())).thenReturn(Map.of());

        HomeFeed result = homeFeedService.getHomeFeed(userUuid);

        assertThat(result.sections()).extracting(section -> section.date())
                .containsExactly(today, today.minusDays(2), today.minusDays(4));
    }

    @Test
    void getHomeFeed_limitsVideosPerSectionToFour() {
        UUID userUuid = UUID.randomUUID();
        LocalDate today = KstDateTimes.today();
        DiaryTheme theme = theme(userUuid, 1L, "일상");
        List<DiaryVideo> todayVideos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            todayVideos.add(video(
                    10L + i,
                    theme.getId(),
                    UUID.randomUUID(),
                    KstDateTimes.startOfDay(today).plusHours(i + 1)
            ));
        }

        when(diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(eq(userUuid), any(), any()))
                .thenReturn(todayVideos);
        when(diaryThemePersistencePort.findAllByUserUuid(userUuid)).thenReturn(List.of(theme));
        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), any())).thenReturn(Map.of());

        HomeFeed result = homeFeedService.getHomeFeed(userUuid);

        assertThat(result.sections()).hasSize(1);
        assertThat(result.sections().get(0).videos()).hasSize(4);
    }

    @Test
    void getHomeFeed_enrichesVideosAcrossThemes() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        LocalDate today = KstDateTimes.today();
        DiaryTheme theme = theme(userUuid, 1L, "일상");
        DiaryVideo todayVideo = video(
                10L, theme.getId(), videoUuid,
                KstDateTimes.startOfDay(today).plusHours(1)
        );

        when(diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(eq(userUuid), any(), any()))
                .thenReturn(List.of(todayVideo));
        when(diaryThemePersistencePort.findAllByUserUuid(userUuid)).thenReturn(List.of(theme));
        when(videoMetadataEnrichmentPort.fetchVideoMetadata(userUuid, List.of(videoUuid)))
                .thenReturn(Map.of(videoUuid, new VideoMetadata(videoUuid, "캡션", "https://cdn/thumb.jpg")));

        HomeFeed result = homeFeedService.getHomeFeed(userUuid);

        assertThat(result.sections().get(0).videos().get(0).caption()).isEqualTo("캡션");
        assertThat(result.sections().get(0).videos().get(0).thumbnailUrl()).isEqualTo("https://cdn/thumb.jpg");
        verify(videoMetadataEnrichmentPort).fetchVideoMetadata(userUuid, List.of(videoUuid));
    }

    private static DiaryTheme theme(UUID userUuid, long id, String name) {
        return DiaryTheme.reconstitute(
                id, UUID.randomUUID(), userUuid, name,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null
        );
    }

    private static DiaryVideo video(Long id, Long themeId, UUID videoUuid, LocalDateTime createdAt) {
        return DiaryVideo.reconstitute(id, themeId, videoUuid, createdAt, createdAt, null);
    }
}

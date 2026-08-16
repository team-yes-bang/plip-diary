package com.plip.diary.application.service;

import com.plip.diary.application.port.in.dto.DateTimeline;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoServicePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DateTimelineServiceTest {

    @Mock
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Mock
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Mock
    private VideoServicePort videoServicePort;

    @InjectMocks
    private DateTimelineService dateTimelineService;

    @Test
    void getDateTimeline_returnsEmptySectionsWhenNoVideos() {
        UUID userUuid = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 8, 1);

        when(diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(eq(userUuid), any(), any()))
                .thenReturn(List.of());

        DateTimeline result = dateTimelineService.getDateTimeline(userUuid, date);

        assertThat(result.date()).isEqualTo(date);
        assertThat(result.sections()).isEmpty();
    }

    @Test
    void getDateTimeline_groupsVideosByThemeWithEnrichment() {
        UUID userUuid = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 8, 1);
        UUID videoUuid = UUID.randomUUID();

        DiaryTheme dailyTheme = DiaryTheme.reconstitute(
                1L, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null
        );
        DiaryTheme travelTheme = DiaryTheme.reconstitute(
                2L, UUID.randomUUID(), userUuid, "여행",
                LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 2, 1, 0, 0),
                null
        );
        DiaryVideo video = DiaryVideo.reconstitute(
                10L, dailyTheme.getId(), videoUuid,
                LocalDateTime.of(2026, 8, 1, 3, 0),
                LocalDateTime.of(2026, 8, 1, 3, 0),
                null
        );

        when(diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(eq(userUuid), any(), any()))
                .thenReturn(List.of(video));
        when(diaryThemePersistencePort.findAllByUserUuid(userUuid))
                .thenReturn(List.of(dailyTheme, travelTheme));
        when(videoServicePort.fetchVideoMetadata(userUuid, List.of(videoUuid)))
                .thenReturn(Map.of(videoUuid, new VideoMetadata(videoUuid, "캡션", "https://cdn/thumb.jpg")));

        DateTimeline result = dateTimelineService.getDateTimeline(userUuid, date);

        assertThat(result.sections()).hasSize(1);
        assertThat(result.sections().get(0).themeId()).isEqualTo(dailyTheme.getId());
        assertThat(result.sections().get(0).themeName()).isEqualTo("일상");
        assertThat(result.sections().get(0).videos()).hasSize(1);
        assertThat(result.sections().get(0).videos().get(0).caption()).isEqualTo("캡션");
        assertThat(result.sections().get(0).videos().get(0).thumbnailUrl()).isEqualTo("https://cdn/thumb.jpg");

        verify(videoServicePort).fetchVideoMetadata(userUuid, List.of(videoUuid));
    }
}

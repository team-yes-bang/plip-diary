package com.plip.diary.application.service;

import com.plip.diary.application.port.in.dto.ThemeTimeline;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoServicePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.exception.ThemeNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThemeTimelineServiceTest {

    @Mock
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Mock
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Mock
    private VideoServicePort videoMetadataEnrichmentPort;

    @InjectMocks
    private ThemeTimelineService themeTimelineService;

    @Test
    void getThemeTimeline_throwsWhenThemeNotFound() {
        UUID userUuid = UUID.randomUUID();

        when(diaryThemePersistencePort.findByIdAndUserUuid(1L, userUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeTimelineService.getThemeTimeline(userUuid, 1L))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void getThemeTimeline_returnsEmptySectionsWhenNoVideos() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(
                1L, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null
        );

        when(diaryThemePersistencePort.findByIdAndUserUuid(theme.getId(), userUuid))
                .thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.findByThemeIdAndUserUuid(theme.getId(), userUuid))
                .thenReturn(List.of());

        ThemeTimeline result = themeTimelineService.getThemeTimeline(userUuid, theme.getId());

        assertThat(result.sections()).isEmpty();
    }

    @Test
    void getThemeTimeline_groupsVideosByDateWithEnrichment() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid1 = UUID.randomUUID();
        UUID videoUuid2 = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(
                1L, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null
        );
        DiaryVideo aug1Video = DiaryVideo.reconstitute(
                10L, theme.getId(), videoUuid1,
                LocalDateTime.of(2026, 8, 1, 3, 0),
                LocalDateTime.of(2026, 8, 1, 3, 0),
                null
        );
        DiaryVideo aug2Video = DiaryVideo.reconstitute(
                11L, theme.getId(), videoUuid2,
                LocalDateTime.of(2026, 8, 2, 5, 0),
                LocalDateTime.of(2026, 8, 2, 5, 0),
                null
        );

        when(diaryThemePersistencePort.findByIdAndUserUuid(theme.getId(), userUuid))
                .thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.findByThemeIdAndUserUuid(theme.getId(), userUuid))
                .thenReturn(List.of(aug1Video, aug2Video));
        when(videoMetadataEnrichmentPort.fetchVideoMetadata(userUuid, List.of(videoUuid1, videoUuid2)))
                .thenReturn(Map.of(
                        videoUuid1, new VideoMetadata(videoUuid1, "8/1 캡션", "https://cdn/aug1.jpg"),
                        videoUuid2, new VideoMetadata(videoUuid2, "8/2 캡션", "https://cdn/aug2.jpg")
                ));

        ThemeTimeline result = themeTimelineService.getThemeTimeline(userUuid, theme.getId());

        assertThat(result.sections()).hasSize(2);
        assertThat(result.sections().get(0).date()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(result.sections().get(0).videos()).hasSize(1);
        assertThat(result.sections().get(0).videos().get(0).caption()).isEqualTo("8/2 캡션");
        assertThat(result.sections().get(1).date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.sections().get(1).videos().get(0).thumbnailUrl()).isEqualTo("https://cdn/aug1.jpg");

        verify(videoMetadataEnrichmentPort).fetchVideoMetadata(userUuid, List.of(videoUuid1, videoUuid2));
    }
}

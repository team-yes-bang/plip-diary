package com.plip.diary.application.service;

import com.plip.diary.application.port.in.dto.ThemeTimelinePage;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoServicePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.exception.ThemeNotFoundException;
import com.plip.diary.global.pagination.TimelineCursor;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
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

        assertThatThrownBy(() -> themeTimelineService.getThemeTimeline(userUuid, 1L, null, 50))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void getThemeTimeline_returnsEmptySectionsWhenNoVideos() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = diaryTheme(userUuid);

        when(diaryThemePersistencePort.findByIdAndUserUuid(theme.getId(), userUuid))
                .thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.findByThemeIdAndUserUuidWithCursor(theme.getId(), userUuid, null, 51))
                .thenReturn(List.of());

        ThemeTimelinePage result = themeTimelineService.getThemeTimeline(userUuid, theme.getId(), null, 50);

        assertThat(result.sections()).isEmpty();
        assertThat(result.hasMore()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void getThemeTimeline_groupsVideosByDateWithEnrichment() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid1 = UUID.randomUUID();
        UUID videoUuid2 = UUID.randomUUID();
        DiaryTheme theme = diaryTheme(userUuid);
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
        when(diaryVideoPersistencePort.findByThemeIdAndUserUuidWithCursor(theme.getId(), userUuid, null, 51))
                .thenReturn(List.of(aug2Video, aug1Video));
        when(videoMetadataEnrichmentPort.fetchVideoMetadata(userUuid, List.of(videoUuid2, videoUuid1)))
                .thenReturn(Map.of(
                        videoUuid1, new VideoMetadata(videoUuid1, "8/1 캡션", "https://cdn/aug1.jpg"),
                        videoUuid2, new VideoMetadata(videoUuid2, "8/2 캡션", "https://cdn/aug2.jpg")
                ));

        ThemeTimelinePage result = themeTimelineService.getThemeTimeline(userUuid, theme.getId(), null, 50);

        assertThat(result.sections()).hasSize(2);
        assertThat(result.sections().get(0).date()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(result.sections().get(0).videos()).hasSize(1);
        assertThat(result.sections().get(0).videos().get(0).caption()).isEqualTo("8/2 캡션");
        assertThat(result.sections().get(1).date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.sections().get(1).videos().get(0).thumbnailUrl()).isEqualTo("https://cdn/aug1.jpg");
        assertThat(result.hasMore()).isFalse();
        assertThat(result.nextCursor()).isNull();

        verify(videoMetadataEnrichmentPort).fetchVideoMetadata(userUuid, List.of(videoUuid2, videoUuid1));
    }

    @Test
    void getThemeTimeline_returnsNextCursorWhenMoreVideosExist() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = diaryTheme(userUuid);
        List<DiaryVideo> fetched = new ArrayList<>();
        for (long id = 1; id <= 51; id++) {
            fetched.add(DiaryVideo.reconstitute(
                    id,
                    theme.getId(),
                    UUID.randomUUID(),
                    LocalDateTime.of(2026, 8, 1, 0, 0).plusMinutes(id),
                    LocalDateTime.of(2026, 8, 1, 0, 0).plusMinutes(id),
                    null
            ));
        }

        when(diaryThemePersistencePort.findByIdAndUserUuid(theme.getId(), userUuid))
                .thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.findByThemeIdAndUserUuidWithCursor(theme.getId(), userUuid, null, 51))
                .thenReturn(fetched);
        when(videoMetadataEnrichmentPort.fetchVideoMetadata(eq(userUuid), eq(fetched.subList(0, 50).stream()
                .map(DiaryVideo::getVideoUuid)
                .toList())))
                .thenReturn(Map.of());

        ThemeTimelinePage result = themeTimelineService.getThemeTimeline(userUuid, theme.getId(), null, 50);

        assertThat(result.hasMore()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(TimelineCursor.encode(fetched.get(49)));
    }

    @Test
    void getThemeTimeline_passesDecodedCursorToPersistence() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = diaryTheme(userUuid);
        String cursor = TimelineCursor.encode(LocalDateTime.of(2026, 8, 1, 12, 0), 99L);
        TimelineCursor.Decoded decoded = TimelineCursor.decode(cursor);

        when(diaryThemePersistencePort.findByIdAndUserUuid(theme.getId(), userUuid))
                .thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.findByThemeIdAndUserUuidWithCursor(theme.getId(), userUuid, decoded, 51))
                .thenReturn(List.of());

        themeTimelineService.getThemeTimeline(userUuid, theme.getId(), cursor, 50);

        verify(diaryVideoPersistencePort).findByThemeIdAndUserUuidWithCursor(theme.getId(), userUuid, decoded, 51);
    }

    private DiaryTheme diaryTheme(UUID userUuid) {
        return DiaryTheme.reconstitute(
                1L, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null
        );
    }
}

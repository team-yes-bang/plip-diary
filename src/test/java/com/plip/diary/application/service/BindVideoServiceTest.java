package com.plip.diary.application.service;

import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BindVideoServiceTest {

    @Mock
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Mock
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @InjectMocks
    private BindVideoService bindVideoService;

    @Test
    void bindVideo_savesWhenValid() {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(1L, themeUuid, userUuid, "일상", null, null, null);

        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.countTodayByUserUuid(userUuid)).thenReturn(0L);
        when(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(1L, videoUuid)).thenReturn(false);

        boolean bound = bindVideoService.bindVideo(themeUuid, videoUuid, userUuid);

        assertThat(bound).isTrue();
        ArgumentCaptor<DiaryVideo> captor = ArgumentCaptor.forClass(DiaryVideo.class);
        verify(diaryVideoPersistencePort).save(captor.capture());
        DiaryVideo saved = captor.getValue();
        assertThat(saved.getThemeId()).isEqualTo(1L);
        assertThat(saved.getVideoUuid()).isEqualTo(videoUuid);
    }

    @Test
    void bindVideo_skipsWhenThemeNotFound() {
        UUID themeUuid = UUID.randomUUID();
        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.empty());

        assertThat(bindVideoService.bindVideo(themeUuid, UUID.randomUUID(), UUID.randomUUID())).isFalse();

        verify(diaryVideoPersistencePort, never()).save(any());
    }

    @Test
    void bindVideo_skipsWhenUserMismatch() {
        UUID themeUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(1L, themeUuid, UUID.randomUUID(), "일상", null, null, null);
        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.of(theme));

        assertThat(bindVideoService.bindVideo(themeUuid, UUID.randomUUID(), userUuid)).isFalse();

        verify(diaryVideoPersistencePort, never()).save(any());
    }

    @Test
    void bindVideo_skipsWhenDailyLimitExceeded() {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(1L, themeUuid, userUuid, "일상", null, null, null);

        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.countTodayByUserUuid(userUuid))
                .thenReturn((long) BindVideoService.DAILY_VIDEO_LIMIT);

        assertThat(bindVideoService.bindVideo(themeUuid, UUID.randomUUID(), userUuid)).isFalse();

        verify(diaryVideoPersistencePort, never()).save(any());
    }

    @Test
    void bindVideo_skipsWhenAlreadyBound() {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(1L, themeUuid, userUuid, "일상", null, null, null);

        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.countTodayByUserUuid(userUuid)).thenReturn(0L);
        when(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(1L, videoUuid)).thenReturn(true);

        assertThat(bindVideoService.bindVideo(themeUuid, videoUuid, userUuid)).isFalse();

        verify(diaryVideoPersistencePort, never()).save(any());
        verify(diaryVideoPersistencePort).existsByThemeIdAndVideoUuid(eq(1L), eq(videoUuid));
    }
}

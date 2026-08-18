package com.plip.diary.application.service;

import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoLinkedEventPort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    @Mock
    private VideoLinkedEventPort videoLinkedEventPort;

    @InjectMocks
    private BindVideoService bindVideoService;

    @Test
    void bindVideo_savesAndPublishesLinkedWhenValid() {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(1L, themeUuid, userUuid, "일상", null, null, null);

        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.countTodayByUserUuid(userUuid)).thenReturn(0L);
        when(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(1L, videoUuid)).thenReturn(false);

        TransactionSynchronizationManager.initSynchronization();
        try {
            boolean bound = bindVideoService.bindVideo(themeUuid, videoUuid, userUuid, "캡션", "https://cdn/thumb.jpg");

            assertThat(bound).isTrue();
            ArgumentCaptor<DiaryVideo> captor = ArgumentCaptor.forClass(DiaryVideo.class);
            verify(diaryVideoPersistencePort).save(captor.capture());
            assertThat(captor.getValue().getThemeId()).isEqualTo(1L);
            assertThat(captor.getValue().getVideoUuid()).isEqualTo(videoUuid);

            verify(videoLinkedEventPort, never()).publish(videoUuid);
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(sync -> sync.afterCommit());
            verify(videoLinkedEventPort).publish(videoUuid);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void bindVideo_skipsWhenThemeNotFound() {
        UUID themeUuid = UUID.randomUUID();
        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.empty());

        assertThat(bindVideoService.bindVideo(themeUuid, UUID.randomUUID(), UUID.randomUUID(), null, null)).isFalse();

        verify(diaryVideoPersistencePort, never()).save(any());
    }

    @Test
    void bindVideo_skipsWhenUserMismatch() {
        UUID themeUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(1L, themeUuid, UUID.randomUUID(), "일상", null, null, null);
        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.of(theme));

        assertThat(bindVideoService.bindVideo(themeUuid, UUID.randomUUID(), userUuid, null, null)).isFalse();

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

        assertThat(bindVideoService.bindVideo(themeUuid, UUID.randomUUID(), userUuid, null, null)).isFalse();

        verify(diaryVideoPersistencePort, never()).save(any());
    }

    @Test
    void bindVideo_skipsWhenAlreadyBound() {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.reconstitute(1L, themeUuid, userUuid, "일상", null, null, null);

        when(diaryThemePersistencePort.findByThemeUuid(themeUuid)).thenReturn(Optional.of(theme));
        when(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(1L, videoUuid)).thenReturn(true);

        assertThat(bindVideoService.bindVideo(themeUuid, videoUuid, userUuid, null, null)).isFalse();

        verify(diaryVideoPersistencePort, never()).save(any());
        verify(videoLinkedEventPort, never()).publish(any());
        verify(diaryVideoPersistencePort).existsByThemeIdAndVideoUuid(eq(1L), eq(videoUuid));
    }
}

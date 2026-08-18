package com.plip.diary.application.service;

import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoUnlinkedEventPort;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.exception.DiaryVideoNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnbindDiaryVideoServiceTest {

    @Mock
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Mock
    private VideoUnlinkedEventPort videoUnlinkedEventPort;

    @Mock
    private VideoMetadataSyncService videoMetadataSyncService;

    @InjectMocks
    private UnbindDiaryVideoService unbindDiaryVideoService;

    @Test
    void unbindDiaryVideo_softDeletesAndRegistersEventPublish() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        Long diaryVideoId = 10L;
        DiaryVideo video = DiaryVideo.reconstitute(diaryVideoId, 1L, videoUuid, null, null, null);

        when(diaryVideoPersistencePort.findByIdAndUserUuid(diaryVideoId, userUuid))
                .thenReturn(Optional.of(video));

        TransactionSynchronizationManager.initSynchronization();
        try {
            unbindDiaryVideoService.unbindDiaryVideo(userUuid, diaryVideoId);

            verify(diaryVideoPersistencePort).softDelete(diaryVideoId);
            verify(videoUnlinkedEventPort, never()).publish(videoUuid);
            verify(videoMetadataSyncService, never()).remove(userUuid, videoUuid);

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(sync -> sync.afterCommit());

            verify(videoUnlinkedEventPort).publish(videoUuid);
            verify(videoMetadataSyncService).remove(userUuid, videoUuid);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void unbindDiaryVideo_throwsWhenNotFound() {
        UUID userUuid = UUID.randomUUID();
        Long diaryVideoId = 10L;

        when(diaryVideoPersistencePort.findByIdAndUserUuid(diaryVideoId, userUuid))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> unbindDiaryVideoService.unbindDiaryVideo(userUuid, diaryVideoId))
                .isInstanceOf(DiaryVideoNotFoundException.class);

        verify(diaryVideoPersistencePort, never()).softDelete(diaryVideoId);
        verify(videoUnlinkedEventPort, never()).publish(org.mockito.ArgumentMatchers.any());
        verify(videoMetadataSyncService, never()).remove(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}

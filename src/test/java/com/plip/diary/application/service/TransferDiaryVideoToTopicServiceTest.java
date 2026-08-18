package com.plip.diary.application.service;

import com.plip.diary.application.port.in.VideoTransferMode;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoUnlinkedEventPort;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.exception.DiaryVideoNotFoundException;
import com.plip.diary.global.exception.DiaryVideoTopicCopyNotOnDiaryException;
import com.plip.diary.global.exception.DiaryVideoTopicTransferNotAllowedException;
import com.plip.diary.global.time.KstDateTimes;
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
class TransferDiaryVideoToTopicServiceTest {

    @Mock
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Mock
    private VideoUnlinkedEventPort videoUnlinkedEventPort;

    @Mock
    private VideoMetadataSyncService videoMetadataSyncService;

    @InjectMocks
    private TransferDiaryVideoToTopicService transferDiaryVideoToTopicService;

    @Test
    void transfer_copy_throwsNotOnDiary() {
        assertThatThrownBy(() -> transferDiaryVideoToTopicService.transfer(
                UUID.randomUUID(),
                1L,
                VideoTransferMode.COPY
        )).isInstanceOf(DiaryVideoTopicCopyNotOnDiaryException.class);

        verify(diaryVideoPersistencePort, never()).softDelete(1L);
    }

    @Test
    void transfer_move_softDeletesAndUnlinksDiary() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryVideo video = DiaryVideo.reconstitute(
                1L,
                10L,
                videoUuid,
                KstDateTimes.startOfToday().plusHours(1),
                null,
                null
        );

        when(diaryVideoPersistencePort.findByIdAndUserUuid(1L, userUuid)).thenReturn(Optional.of(video));

        TransactionSynchronizationManager.initSynchronization();
        try {
            transferDiaryVideoToTopicService.transfer(userUuid, 1L, VideoTransferMode.MOVE);

            verify(diaryVideoPersistencePort).softDelete(1L);
            verify(videoUnlinkedEventPort, never()).publish(videoUuid);

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(sync -> sync.afterCommit());

            verify(videoUnlinkedEventPort).publish(videoUuid);
            verify(videoMetadataSyncService).remove(userUuid, videoUuid);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void transfer_move_throwsWhenNotToday() {
        UUID userUuid = UUID.randomUUID();
        DiaryVideo video = DiaryVideo.reconstitute(
                1L,
                10L,
                UUID.randomUUID(),
                KstDateTimes.startOfToday().minusDays(1),
                null,
                null
        );

        when(diaryVideoPersistencePort.findByIdAndUserUuid(1L, userUuid)).thenReturn(Optional.of(video));

        assertThatThrownBy(() -> transferDiaryVideoToTopicService.transfer(
                userUuid,
                1L,
                VideoTransferMode.MOVE
        )).isInstanceOf(DiaryVideoTopicTransferNotAllowedException.class);

        verify(diaryVideoPersistencePort, never()).softDelete(1L);
    }

    @Test
    void transfer_move_throwsWhenNotFound() {
        UUID userUuid = UUID.randomUUID();
        when(diaryVideoPersistencePort.findByIdAndUserUuid(1L, userUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferDiaryVideoToTopicService.transfer(
                userUuid,
                1L,
                VideoTransferMode.MOVE
        )).isInstanceOf(DiaryVideoNotFoundException.class);
    }
}

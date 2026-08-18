package com.plip.diary.application.service;

import com.plip.diary.application.port.in.TransferDiaryVideoToTopicUseCase;
import com.plip.diary.application.port.in.VideoTransferMode;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoUnlinkedEventPort;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.exception.DiaryVideoNotFoundException;
import com.plip.diary.global.exception.DiaryVideoTopicCopyNotOnDiaryException;
import com.plip.diary.global.exception.DiaryVideoTopicTransferNotAllowedException;
import com.plip.diary.global.time.KstDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * 다이어리 → 토픽 <b>이동</b> 시 diary 담당 구간.
 * <p>토픽 바인딩(COPY·topic 등록)은 topic-service REST가 선행 처리한다(FE 오케스트레이션).
 * 본 서비스는 MOVE일 때만 `diary_videos` Soft Delete + `diary.video.unlinked`를 수행한다.</p>
 */
@Service
@RequiredArgsConstructor
public class TransferDiaryVideoToTopicService implements TransferDiaryVideoToTopicUseCase {

    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final VideoUnlinkedEventPort videoUnlinkedEventPort;
    private final VideoMetadataSyncService videoMetadataSyncService;

    @Override
    @Transactional
    public void transfer(UUID userUuid, Long diaryVideoId, VideoTransferMode mode) {
        if (mode == VideoTransferMode.COPY) {
            throw new DiaryVideoTopicCopyNotOnDiaryException();
        }

        DiaryVideo video = diaryVideoPersistencePort.findByIdAndUserUuid(diaryVideoId, userUuid)
                .orElseThrow(DiaryVideoNotFoundException::new);

        validateMoveEligible(video);

        UUID videoUuid = video.getVideoUuid();
        diaryVideoPersistencePort.softDelete(diaryVideoId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                videoUnlinkedEventPort.publish(videoUuid);
                videoMetadataSyncService.remove(userUuid, videoUuid);
            }
        });
    }

    private void validateMoveEligible(DiaryVideo video) {
        if (video.getCreatedAt() == null) {
            throw new DiaryVideoTopicTransferNotAllowedException();
        }
        if (!KstDateTimes.toLocalDate(video.getCreatedAt()).equals(KstDateTimes.today())) {
            throw new DiaryVideoTopicTransferNotAllowedException();
        }
    }
}

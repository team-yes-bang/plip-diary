package com.plip.diary.application.service;

import com.plip.diary.application.port.in.UnbindDiaryVideoUseCase;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoDeletedEventPort;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.exception.DiaryVideoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnbindDiaryVideoService implements UnbindDiaryVideoUseCase {

    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final VideoDeletedEventPort videoDeletedEventPort;

    @Override
    @Transactional
    public void unbindDiaryVideo(UUID userUuid, Long diaryVideoId) {
        DiaryVideo video = diaryVideoPersistencePort.findByIdAndUserUuid(diaryVideoId, userUuid)
                .orElseThrow(DiaryVideoNotFoundException::new);

        diaryVideoPersistencePort.softDelete(diaryVideoId);

        UUID videoUuid = video.getVideoUuid();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                videoDeletedEventPort.publish(videoUuid, userUuid);
            }
        });
    }
}

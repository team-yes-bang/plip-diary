package com.plip.diary.application.service;

import com.plip.diary.application.port.in.BindVideoUseCase;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoLinkedEventPort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BindVideoService implements BindVideoUseCase {

    static final int DAILY_VIDEO_LIMIT = 20;

    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final VideoLinkedEventPort videoLinkedEventPort;

    @Override
    @Transactional
    public boolean bindVideo(UUID themeUuid, UUID videoUuid, UUID userUuid, String caption, String thumbnailUrl) {
        DiaryTheme theme = diaryThemePersistencePort.findByThemeUuid(themeUuid).orElse(null);
        if (theme == null) {
            log.warn("diary.video.uploaded themeUuid 미존재 — skip themeUuid={}", themeUuid);
            return false;
        }

        if (!theme.getUserUuid().equals(userUuid)) {
            log.warn(
                    "diary.video.uploaded userUuid 불일치 — skip themeUuid={} eventUserUuid={} themeUserUuid={}",
                    themeUuid,
                    userUuid,
                    theme.getUserUuid()
            );
            return false;
        }

        if (diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid)) {
            return false;
        }

        if (diaryVideoPersistencePort.countTodayByUserUuid(userUuid) >= DAILY_VIDEO_LIMIT) {
            log.warn("diary.video.uploaded 당일 영상 한도 초과 — skip userUuid={}", userUuid);
            return false;
        }

        diaryVideoPersistencePort.save(DiaryVideo.create(theme.getId(), videoUuid));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                videoLinkedEventPort.publish(videoUuid);
            }
        });
        return true;
    }
}

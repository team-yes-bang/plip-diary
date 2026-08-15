package com.plip.diary.application.service;

import com.plip.diary.application.port.in.BindVideoUseCase;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BindVideoService implements BindVideoUseCase {

    static final int DAILY_VIDEO_LIMIT = 20;

    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Override
    @Transactional
    public void bindVideo(UUID themeUuid, UUID videoUuid, UUID userUuid) {
        DiaryTheme theme = diaryThemePersistencePort.findByThemeUuid(themeUuid).orElse(null);
        if (theme == null) {
            log.warn("video.uploaded themeUuid 미존재 — skip themeUuid={}", themeUuid);
            return;
        }

        if (!theme.getUserUuid().equals(userUuid)) {
            log.warn(
                    "video.uploaded userUuid 불일치 — skip themeUuid={} eventUserUuid={} themeUserUuid={}",
                    themeUuid,
                    userUuid,
                    theme.getUserUuid()
            );
            return;
        }

        if (diaryVideoPersistencePort.countTodayByUserUuid(userUuid) >= DAILY_VIDEO_LIMIT) {
            log.warn("video.uploaded 당일 영상 한도 초과 — skip userUuid={}", userUuid);
            return;
        }

        if (diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid)) {
            return;
        }

        diaryVideoPersistencePort.save(DiaryVideo.create(theme.getId(), videoUuid));
    }
}

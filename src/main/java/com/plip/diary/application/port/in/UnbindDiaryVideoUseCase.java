package com.plip.diary.application.port.in;

import java.util.UUID;

public interface UnbindDiaryVideoUseCase {

    void unbindDiaryVideo(UUID userUuid, Long diaryVideoId);
}

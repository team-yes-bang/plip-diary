package com.plip.diary.application.port.in;

import java.util.UUID;

public interface TransferDiaryVideoToTopicUseCase {

    void transfer(UUID userUuid, Long diaryVideoId, VideoTransferMode mode);
}

package com.plip.diary.application.port.in;

import java.util.UUID;

public interface BindVideoUseCase {

    /**
     * @return 신규 바인딩 생성 시 true, 검증 실패·멱등 skip 시 false
     */
    boolean bindVideo(UUID themeUuid, UUID videoUuid, UUID userUuid, String caption, String thumbnailUrl);
}

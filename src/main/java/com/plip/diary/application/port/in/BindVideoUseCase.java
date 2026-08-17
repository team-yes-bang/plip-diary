package com.plip.diary.application.port.in;

import java.util.UUID;

public interface BindVideoUseCase {

  /**
   * @return 바인딩 INSERT 성공 시 true, skip·멱등 시 false
   */
    boolean bindVideo(UUID themeUuid, UUID videoUuid, UUID userUuid);
}

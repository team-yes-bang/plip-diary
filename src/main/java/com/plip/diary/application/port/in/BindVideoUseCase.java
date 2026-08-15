package com.plip.diary.application.port.in;

import java.util.UUID;

public interface BindVideoUseCase {

    void bindVideo(UUID themeUuid, UUID videoUuid, UUID userUuid);
}

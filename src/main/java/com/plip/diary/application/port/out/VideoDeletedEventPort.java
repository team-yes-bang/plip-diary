package com.plip.diary.application.port.out;

import java.util.UUID;

public interface VideoDeletedEventPort {

    void publish(UUID videoUuid, UUID userUuid);
}

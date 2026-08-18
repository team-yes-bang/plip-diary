package com.plip.diary.application.port.out;

import java.util.UUID;

public interface VideoLinkedEventPort {

    void publish(UUID videoUuid);
}

package com.plip.diary.application.port.out;

import java.util.UUID;

public interface VideoUnlinkedEventPort {

    void publish(UUID videoUuid);
}

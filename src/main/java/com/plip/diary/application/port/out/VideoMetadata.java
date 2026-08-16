package com.plip.diary.application.port.out;

import java.util.UUID;

public record VideoMetadata(
        UUID videoUuid,
        String caption,
        String thumbnailUrl
) {
}

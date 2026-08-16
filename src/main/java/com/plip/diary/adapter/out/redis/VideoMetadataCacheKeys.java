package com.plip.diary.adapter.out.redis;

import java.util.UUID;

final class VideoMetadataCacheKeys {

    static final String VIDEO_META_KEY_PREFIX = "diary:video-meta:";

    private VideoMetadataCacheKeys() {
    }

    static String videoMetaKey(UUID userUuid, UUID videoUuid) {
        return VIDEO_META_KEY_PREFIX + userUuid + ":" + videoUuid;
    }
}

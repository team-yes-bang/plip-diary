package com.plip.diary.application.port.out;

import java.util.UUID;

/**
 * 영상 메타 projection 쓰기 포트 — 바인딩·해제 시 MongoDB upsert·delete.
 */
public interface VideoMetadataProjectionPort {

    void upsert(VideoMetadataProjection projection);

    void deleteByVideoUuid(UUID userUuid, UUID videoUuid);
}

package com.plip.diary.application.port.out;

import java.util.List;
import java.util.UUID;

/**
 * 영상 메타 Mongo projection 조회 포트 — CQRS Query side read.
 */
public interface VideoMetadataQueryPort {

    List<VideoMetadataProjection> findByUserUuidAndVideoUuids(UUID userUuid, List<UUID> videoUuids);
}

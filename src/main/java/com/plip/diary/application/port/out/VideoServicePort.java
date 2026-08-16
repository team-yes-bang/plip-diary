package com.plip.diary.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 타임라인 조회용 영상 메타(캡션·썸네일) enrichment 포트.
 * <p>Phase 4-2~4-4: {@code VideoServiceHttpAdapter}(조회 시 video-service REST batch).</p>
 * <p>Phase 5-3: Read Model Adapter로 구현 교체 — Service·Controller·본 인터페이스는 유지.</p>
 */
public interface VideoServicePort {

    Map<UUID, VideoMetadata> fetchVideoMetadata(UUID userUuid, List<UUID> videoUuids);
}

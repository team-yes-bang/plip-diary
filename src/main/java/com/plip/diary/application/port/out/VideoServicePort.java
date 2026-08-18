package com.plip.diary.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 타임라인 조회용 영상 메타(caption·thumbnail) enrichment 아웃바운드 포트.
 * <p>구현: {@link com.plip.diary.adapter.out.query.VideoMetadataQueryAdapter} — Redis look-aside → Mongo projection.</p>
 * <p>이름의 {@code VideoService}는 cross-service {@code video_uuid} 출처를 뜻하며, 조회 시 video-service HTTP 호출을 의미하지 않는다.</p>
 */
public interface VideoServicePort {

    Map<UUID, VideoMetadata> fetchVideoMetadata(UUID userUuid, List<UUID> videoUuids);
}

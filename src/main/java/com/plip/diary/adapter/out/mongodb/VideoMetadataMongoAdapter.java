package com.plip.diary.adapter.out.mongodb;

import com.plip.diary.application.port.out.VideoMetadataProjection;
import com.plip.diary.application.port.out.VideoMetadataProjectionPort;
import com.plip.diary.application.port.out.VideoMetadataQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * MongoDB 영상 메타 projection Adapter — CQRS Query side 저장소.
 * <p>Phase 5-1: stub(no-op). Phase 5-2: {@link DiaryVideoMetadataMongoRepository} 연동.</p>
 */
@Component
public class VideoMetadataMongoAdapter implements VideoMetadataProjectionPort, VideoMetadataQueryPort {

    @Override
    public List<VideoMetadataProjection> findByUserUuidAndVideoUuids(UUID userUuid, List<UUID> videoUuids) {
        if (videoUuids == null || videoUuids.isEmpty()) {
            return List.of();
        }
        return List.of();
    }

    @Override
    public void upsert(VideoMetadataProjection projection) {
        // Phase 5-2에서 구현
    }

    @Override
    public void deleteByVideoUuid(UUID userUuid, UUID videoUuid) {
        // Phase 5-2에서 구현
    }
}

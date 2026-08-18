package com.plip.diary.application.service;

import com.plip.diary.application.port.out.VideoMetadataCachePort;
import com.plip.diary.application.port.out.VideoMetadataProjection;
import com.plip.diary.application.port.out.VideoMetadataProjectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 영상 메타 projection 동기화 — 바인딩·해제 시 Mongo upsert·remove 및 Redis evict.
 */
@Service
@RequiredArgsConstructor
public class VideoMetadataSyncService {

    private final VideoMetadataProjectionPort videoMetadataProjectionPort;
    private final VideoMetadataCachePort videoMetadataCachePort;

    public void upsertFromBind(UUID userUuid, UUID videoUuid, String caption, String thumbnailUrl) {
        VideoMetadataProjection projection = new VideoMetadataProjection(
                videoUuid,
                userUuid,
                caption,
                thumbnailUrl,
                LocalDateTime.now()
        );
        videoMetadataProjectionPort.upsert(projection);
        videoMetadataCachePort.evict(userUuid, videoUuid);
    }

    public void remove(UUID userUuid, UUID videoUuid) {
        videoMetadataProjectionPort.deleteByVideoUuid(userUuid, videoUuid);
        videoMetadataCachePort.evict(userUuid, videoUuid);
    }
}

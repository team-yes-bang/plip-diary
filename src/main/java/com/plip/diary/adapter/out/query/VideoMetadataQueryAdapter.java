package com.plip.diary.adapter.out.query;

import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoMetadataCachePort;
import com.plip.diary.application.port.out.VideoMetadataProjection;
import com.plip.diary.application.port.out.VideoMetadataQueryPort;
import com.plip.diary.application.port.out.VideoServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CQRS Query side enrichment Adapter — Redis look-aside → Mongo projection.
 */
@Component
@RequiredArgsConstructor
public class VideoMetadataQueryAdapter implements VideoServicePort {

    private final VideoMetadataCachePort videoMetadataCachePort;
    private final VideoMetadataQueryPort videoMetadataQueryPort;

    @Override
    public Map<UUID, VideoMetadata> fetchVideoMetadata(UUID userUuid, List<UUID> videoUuids) {
        if (videoUuids.isEmpty()) {
            return Map.of();
        }

        Map<UUID, VideoMetadata> result = new HashMap<>(videoMetadataCachePort.get(userUuid, videoUuids));

        List<UUID> mongoMisses = videoUuids.stream()
                .filter(videoUuid -> !result.containsKey(videoUuid))
                .toList();
        if (mongoMisses.isEmpty()) {
            return result;
        }

        Map<UUID, VideoMetadata> fromMongo = loadFromMongo(userUuid, mongoMisses);
        if (!fromMongo.isEmpty()) {
            result.putAll(fromMongo);
            videoMetadataCachePort.put(userUuid, fromMongo);
        }
        return result;
    }

    private Map<UUID, VideoMetadata> loadFromMongo(UUID userUuid, List<UUID> videoUuids) {
        return videoMetadataQueryPort.findByUserUuidAndVideoUuids(userUuid, videoUuids).stream()
                .collect(Collectors.toMap(
                        VideoMetadataProjection::videoUuid,
                        projection -> new VideoMetadata(
                                projection.videoUuid(),
                                projection.caption(),
                                projection.thumbnailUrl()
                        ),
                        (first, second) -> first
                ));
    }
}

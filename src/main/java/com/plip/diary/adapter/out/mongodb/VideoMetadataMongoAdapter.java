package com.plip.diary.adapter.out.mongodb;

import com.plip.diary.application.port.out.VideoMetadataProjection;
import com.plip.diary.application.port.out.VideoMetadataProjectionPort;
import com.plip.diary.application.port.out.VideoMetadataQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * MongoDB 영상 메타 projection Adapter — CQRS Query side 저장소.
 */
@Component
@RequiredArgsConstructor
public class VideoMetadataMongoAdapter implements VideoMetadataProjectionPort, VideoMetadataQueryPort {

    private final DiaryVideoMetadataMongoRepository diaryVideoMetadataMongoRepository;

    @Override
    public List<VideoMetadataProjection> findByUserUuidAndVideoUuids(UUID userUuid, List<UUID> videoUuids) {
        if (videoUuids == null || videoUuids.isEmpty()) {
            return List.of();
        }
        return diaryVideoMetadataMongoRepository.findByUserUuidAndVideoUuidIn(userUuid, videoUuids).stream()
                .map(this::toProjection)
                .toList();
    }

    @Override
    public void upsert(VideoMetadataProjection projection) {
        DiaryVideoMetadataDocument document = diaryVideoMetadataMongoRepository
                .findById(projection.videoUuid())
                .orElseGet(DiaryVideoMetadataDocument::new);

        document.setVideoUuid(projection.videoUuid());
        document.setUserUuid(projection.userUuid());
        document.setCaption(projection.caption());
        document.setThumbnailUrl(projection.thumbnailUrl());
        document.setUpdatedAt(projection.updatedAt());

        diaryVideoMetadataMongoRepository.save(document);
    }

    @Override
    public void deleteByVideoUuid(UUID userUuid, UUID videoUuid) {
        diaryVideoMetadataMongoRepository.findById(videoUuid)
                .filter(document -> document.getUserUuid().equals(userUuid))
                .ifPresent(diaryVideoMetadataMongoRepository::delete);
    }

    private VideoMetadataProjection toProjection(DiaryVideoMetadataDocument document) {
        return new VideoMetadataProjection(
                document.getVideoUuid(),
                document.getUserUuid(),
                document.getCaption(),
                document.getThumbnailUrl(),
                document.getUpdatedAt()
        );
    }
}

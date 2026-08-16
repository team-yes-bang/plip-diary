package com.plip.diary.adapter.out.http;

import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.global.web.RequestHeaders;
import com.plip.diary.application.port.out.VideoServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Phase 4-2 interim — 조회 시 video-service HTTP batch 호출로 enrichment.
 * <p>5천만 사용자 규모 최종 조회 경로는 Phase 5-3 Read Model(CQRS) 전환. 본 Adapter는 그때 교체·fallback 용.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoServiceHttpAdapter implements VideoServicePort {

    private static final String METADATA_BATCH_PATH = "/api/videos/metadata/batch";

    private final RestClient videoServiceRestClient;

    @Override
    public Map<UUID, VideoMetadata> fetchVideoMetadata(UUID userUuid, List<UUID> videoUuids) {
        if (videoUuids.isEmpty()) {
            return Map.of();
        }

        try {
            VideoMetadataBatchResponse response = videoServiceRestClient.post()
                    .uri(METADATA_BATCH_PATH)
                    .header(RequestHeaders.USER_UUID_HEADER, userUuid.toString())
                    .body(new VideoMetadataBatchRequest(videoUuids))
                    .retrieve()
                    .body(VideoMetadataBatchResponse.class);

            if (response == null || response.videos() == null) {
                return Map.of();
            }

            return response.videos().stream()
                    .collect(Collectors.toMap(
                            item -> item.videoUuid(),
                            item -> new VideoMetadata(item.videoUuid(), item.caption(), item.thumbnailUrl()),
                            (first, second) -> first
                    ));
        } catch (RestClientException ex) {
            log.warn("video-service metadata batch 호출 실패 — enrichment 생략: {}", ex.getMessage());
            return Collections.emptyMap();
        } catch (RuntimeException ex) {
            // Eureka 미사용·video-service 미기동 시 LoadBalancer 등에서 RestClientException 외 예외 발생
            log.warn("video-service metadata batch 호출 실패 — enrichment 생략: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }
}

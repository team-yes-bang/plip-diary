package com.plip.diary.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * CQRS Query side(Mongo projection·Redis cache) 설정.
 * <p>기본 비활성 — Phase 4 HTTP enrichment 유지.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "diary.query-side")
public class QuerySideProperties {

    /**
     * Query side projection·캐시 조회 활성화.
     */
    private boolean enabled = false;

    /**
     * Phase 5-3: projection·캐시 미스 시 video-service HTTP fallback 허용 여부.
     */
    private boolean fallbackHttpEnabled = true;

    /**
     * Phase 5-3: Redis look-aside 캐시 TTL.
     */
    private Duration cacheTtl = Duration.ofHours(24);
}

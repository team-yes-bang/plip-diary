package com.plip.diary.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 타임라인 응답 Redis cache-aside 설정.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "diary.timeline-cache")
public class DiaryTimelineCacheProperties {

    private Duration cacheTtl = Duration.ofMinutes(10);
}

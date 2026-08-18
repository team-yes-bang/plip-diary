package com.plip.diary.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 영상 메타 Read Model Redis look-aside 캐시 설정.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "diary.video-metadata-cache")
public class VideoMetadataCacheProperties {

    private Duration cacheTtl = Duration.ofHours(24);
}

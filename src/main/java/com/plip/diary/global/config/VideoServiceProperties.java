package com.plip.diary.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.video-service")
public class VideoServiceProperties {

    /**
     * Eureka 서비스 id 기준 base URL. 로컬 단독 실행 시 video-service 직접 URL로 override.
     */
    private String baseUrl = "http://video";
}

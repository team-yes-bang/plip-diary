package com.plip.diary.global.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class VideoMetadataCachePropertiesTest {

    @Test
    void defaults_cacheTtlTwentyFourHours() {
        VideoMetadataCacheProperties properties = new VideoMetadataCacheProperties();

        assertThat(properties.getCacheTtl()).isEqualTo(Duration.ofHours(24));
    }
}

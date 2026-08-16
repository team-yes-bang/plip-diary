package com.plip.diary.global.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class QuerySidePropertiesTest {

    @Test
    void defaults_keepPhase4Behavior() {
        QuerySideProperties properties = new QuerySideProperties();

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.isFallbackHttpEnabled()).isTrue();
        assertThat(properties.getCacheTtl()).isEqualTo(Duration.ofHours(24));
    }
}

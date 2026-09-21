package com.plip.diary.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DiaryTimelineCacheProperties.class)
public class DiaryTimelineCacheConfig {
}

package com.plip.diary.adapter.out.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.diary.application.port.in.dto.DateTimeline;
import com.plip.diary.application.port.in.dto.HomeFeedSection;
import com.plip.diary.application.port.in.dto.ThemeTimelinePage;
import com.plip.diary.application.port.out.DiaryTimelineCachePort;
import com.plip.diary.global.config.DiaryTimelineCacheProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 타임라인 응답 Redis cache-aside Adapter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryTimelineRedisAdapter implements DiaryTimelineCachePort {

    private static final TypeReference<List<HomeFeedSection>> HOME_SECTIONS_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate stringRedisTemplate;
    private final DiaryTimelineCacheProperties properties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private Counter hitCounter;
    private Counter missCounter;

    @PostConstruct
    void initMetrics() {
        hitCounter = meterRegistry.counter("diary.timeline.cache", "result", "hit");
        missCounter = meterRegistry.counter("diary.timeline.cache", "result", "miss");
    }

    @Override
    public Optional<List<HomeFeedSection>> getHomeFeedSections(UUID userUuid) {
        return getFromCache(DiaryTimelineCacheKeys.homeKey(userUuid), HOME_SECTIONS_TYPE);
    }

    @Override
    public void putHomeFeedSections(UUID userUuid, List<HomeFeedSection> sections) {
        putToCache(userUuid, DiaryTimelineCacheKeys.homeKey(userUuid), sections);
    }

    @Override
    public Optional<DateTimeline> getDateTimeline(UUID userUuid, LocalDate date) {
        return getFromCache(DiaryTimelineCacheKeys.dateKey(userUuid, date), DateTimeline.class);
    }

    @Override
    public void putDateTimeline(UUID userUuid, LocalDate date, DateTimeline timeline) {
        putToCache(userUuid, DiaryTimelineCacheKeys.dateKey(userUuid, date), timeline);
    }

    @Override
    public Optional<ThemeTimelinePage> getThemeTimeline(UUID userUuid, Long themeId, String cursor, int limit) {
        return getFromCache(DiaryTimelineCacheKeys.themeKey(userUuid, themeId, cursor, limit), ThemeTimelinePage.class);
    }

    @Override
    public void putThemeTimeline(UUID userUuid, Long themeId, String cursor, int limit, ThemeTimelinePage page) {
        putToCache(userUuid, DiaryTimelineCacheKeys.themeKey(userUuid, themeId, cursor, limit), page);
    }

    @Override
    public void evictByUserUuid(UUID userUuid) {
        try {
            String indexKey = DiaryTimelineCacheKeys.indexKey(userUuid);
            Set<String> members = stringRedisTemplate.opsForSet().members(indexKey);
            if (members != null && !members.isEmpty()) {
                List<String> allKeys = new ArrayList<>(members);
                allKeys.add(indexKey);
                stringRedisTemplate.delete(allKeys);
            }
        } catch (Exception ex) {
            log.warn("타임라인 캐시 evict 실패 userUuid={}", userUuid, ex);
        }
    }

    private <T> Optional<T> getFromCache(String key, Class<T> type) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) {
                missCounter.increment();
                return Optional.empty();
            }
            hitCounter.increment();
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception ex) {
            log.warn("타임라인 캐시 역직렬화 실패 key={}", key, ex);
            missCounter.increment();
            return Optional.empty();
        }
    }

    private <T> Optional<T> getFromCache(String key, TypeReference<T> typeRef) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) {
                missCounter.increment();
                return Optional.empty();
            }
            hitCounter.increment();
            return Optional.of(objectMapper.readValue(json, typeRef));
        } catch (Exception ex) {
            log.warn("타임라인 캐시 역직렬화 실패 key={}", key, ex);
            missCounter.increment();
            return Optional.empty();
        }
    }

    private void putToCache(UUID userUuid, String key, Object value) {
        try {
            Duration ttl = properties.getCacheTtl();
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, ttl);
            stringRedisTemplate.opsForSet().add(DiaryTimelineCacheKeys.indexKey(userUuid), key);
            stringRedisTemplate.expire(DiaryTimelineCacheKeys.indexKey(userUuid), ttl.plusMinutes(1));
        } catch (Exception ex) {
            log.warn("타임라인 캐시 저장 실패 key={}", key, ex);
        }
    }
}

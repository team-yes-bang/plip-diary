package com.plip.diary.application.port.out;

import com.plip.diary.application.port.in.dto.DateTimeline;
import com.plip.diary.application.port.in.dto.HomeFeedSection;
import com.plip.diary.application.port.in.dto.ThemeTimelinePage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 타임라인 응답 Redis cache-aside 포트.
 */
public interface DiaryTimelineCachePort {

    Optional<List<HomeFeedSection>> getHomeFeedSections(UUID userUuid);

    void putHomeFeedSections(UUID userUuid, List<HomeFeedSection> sections);

    Optional<DateTimeline> getDateTimeline(UUID userUuid, LocalDate date);

    void putDateTimeline(UUID userUuid, LocalDate date, DateTimeline timeline);

    Optional<ThemeTimelinePage> getThemeTimeline(UUID userUuid, Long themeId, String cursor, int limit);

    void putThemeTimeline(UUID userUuid, Long themeId, String cursor, int limit, ThemeTimelinePage page);

    void evictByUserUuid(UUID userUuid);
}

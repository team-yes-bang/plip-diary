package com.plip.diary.application.service;

import com.plip.diary.application.port.in.GetDateTimelineUseCase;
import com.plip.diary.application.port.in.dto.DateTimeline;
import com.plip.diary.application.port.in.dto.DateTimelineSection;
import com.plip.diary.application.port.in.dto.DateTimelineVideo;
import com.plip.diary.application.port.in.dto.DateWindowDay;
import com.plip.diary.application.port.in.dto.DateWindowTimeline;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoServicePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.time.KstDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateTimelineService implements GetDateTimelineUseCase {

    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final VideoServicePort videoMetadataEnrichmentPort;

    @Override
    public DateTimeline getDateTimeline(UUID userUuid, LocalDate date) {
        LocalDateTime start = KstDateTimes.startOfDay(date);
        LocalDateTime end = KstDateTimes.startOfNextDay(date);

        List<DiaryVideo> videos = diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(userUuid, start, end);
        if (videos.isEmpty()) {
            return new DateTimeline(date, List.of());
        }

        List<DateTimelineSection> sections = buildSectionsForDate(userUuid, date, videos);
        return new DateTimeline(date, sections);
    }

    @Override
    public DateWindowTimeline getDateWindowTimeline(UUID userUuid, LocalDate focusDate, int window) {
        if (window <= 0) {
            throw new IllegalArgumentException("window must be positive");
        }

        LocalDate today = KstDateTimes.today();
        LocalDate startDate = focusDate.minusDays(window);
        LocalDate endDate = focusDate.plusDays(window);
        if (endDate.isAfter(today)) {
            endDate = today;
        }

        LocalDateTime rangeStart = KstDateTimes.startOfDay(startDate);
        LocalDateTime rangeEnd = KstDateTimes.startOfNextDay(endDate);

        List<DiaryVideo> videos = diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(
                userUuid,
                rangeStart,
                rangeEnd
        );

        Map<LocalDate, List<DiaryVideo>> videosByDate = videos.stream()
                .collect(Collectors.groupingBy(video -> KstDateTimes.toLocalDate(video.getCreatedAt())));

        List<UUID> videoUuids = videos.stream()
                .map(DiaryVideo::getVideoUuid)
                .toList();
        Map<UUID, VideoMetadata> metadataByVideoUuid = videoUuids.isEmpty()
                ? Map.of()
                : videoMetadataEnrichmentPort.fetchVideoMetadata(userUuid, videoUuids);

        List<DiaryTheme> themes = diaryThemePersistencePort.findAllByUserUuid(userUuid);

        List<DateWindowDay> days = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<DiaryVideo> dayVideos = videosByDate.getOrDefault(date, List.of());
            List<DateTimelineSection> sections = buildSectionsForDate(
                    themes,
                    dayVideos,
                    metadataByVideoUuid
            );
            days.add(new DateWindowDay(date, sections));
        }

        return new DateWindowTimeline(focusDate, days);
    }

    private List<DateTimelineSection> buildSectionsForDate(UUID userUuid, LocalDate date, List<DiaryVideo> videos) {
        Map<Long, List<DiaryVideo>> videosByThemeId = videos.stream()
                .collect(Collectors.groupingBy(DiaryVideo::getThemeId));

        List<UUID> videoUuids = videos.stream()
                .map(DiaryVideo::getVideoUuid)
                .toList();
        Map<UUID, VideoMetadata> metadataByVideoUuid = videoMetadataEnrichmentPort.fetchVideoMetadata(
                userUuid,
                videoUuids
        );

        return diaryThemePersistencePort.findAllByUserUuid(userUuid).stream()
                .filter(theme -> videosByThemeId.containsKey(theme.getId()))
                .map(theme -> toSection(theme, videosByThemeId.get(theme.getId()), metadataByVideoUuid))
                .toList();
    }

    private List<DateTimelineSection> buildSectionsForDate(
            List<DiaryTheme> themes,
            List<DiaryVideo> videos,
            Map<UUID, VideoMetadata> metadataByVideoUuid
    ) {
        if (videos.isEmpty()) {
            return List.of();
        }

        Map<Long, List<DiaryVideo>> videosByThemeId = videos.stream()
                .collect(Collectors.groupingBy(DiaryVideo::getThemeId));

        return themes.stream()
                .filter(theme -> videosByThemeId.containsKey(theme.getId()))
                .map(theme -> toSection(theme, videosByThemeId.get(theme.getId()), metadataByVideoUuid))
                .toList();
    }

    private DateTimelineSection toSection(
            DiaryTheme theme,
            List<DiaryVideo> videos,
            Map<UUID, VideoMetadata> metadataByVideoUuid
    ) {
        List<DateTimelineVideo> timelineVideos = videos.stream()
                .map(video -> toTimelineVideo(video, metadataByVideoUuid.get(video.getVideoUuid())))
                .toList();
        return new DateTimelineSection(theme.getId(), theme.getName(), timelineVideos);
    }

    private DateTimelineVideo toTimelineVideo(DiaryVideo video, VideoMetadata metadata) {
        String caption = metadata != null ? metadata.caption() : null;
        String thumbnailUrl = metadata != null ? metadata.thumbnailUrl() : null;
        return new DateTimelineVideo(
                video.getId(),
                video.getVideoUuid(),
                caption,
                thumbnailUrl,
                video.getCreatedAt()
        );
    }
}

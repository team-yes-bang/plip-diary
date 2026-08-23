package com.plip.diary.application.service;

import com.plip.diary.application.port.in.GetThemeTimelineUseCase;
import com.plip.diary.application.port.in.dto.ThemeTimelinePage;
import com.plip.diary.application.port.in.dto.ThemeTimelineSection;
import com.plip.diary.application.port.in.dto.ThemeTimelineVideo;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoServicePort;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.exception.ThemeNotFoundException;
import com.plip.diary.global.pagination.TimelineCursor;
import com.plip.diary.global.time.KstDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThemeTimelineService implements GetThemeTimelineUseCase {

    static final int DEFAULT_LIMIT = 50;
    static final int MAX_LIMIT = 50;

    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final VideoServicePort videoMetadataEnrichmentPort;

    @Override
    public ThemeTimelinePage getThemeTimeline(UUID userUuid, Long themeId, String cursor, int limit) {
        diaryThemePersistencePort.findByIdAndUserUuid(themeId, userUuid)
                .orElseThrow(ThemeNotFoundException::new);

        int pageSize = normalizeLimit(limit);
        TimelineCursor.Decoded decodedCursor = TimelineCursor.decode(cursor);

        List<DiaryVideo> fetched = diaryVideoPersistencePort.findByThemeIdAndUserUuidWithCursor(
                themeId,
                userUuid,
                decodedCursor,
                pageSize + 1
        );

        boolean hasMore = fetched.size() > pageSize;
        List<DiaryVideo> pageVideos = hasMore ? fetched.subList(0, pageSize) : fetched;

        if (pageVideos.isEmpty()) {
            return new ThemeTimelinePage(List.of(), null, false);
        }

        List<UUID> videoUuids = pageVideos.stream()
                .map(DiaryVideo::getVideoUuid)
                .toList();
        Map<UUID, VideoMetadata> metadataByVideoUuid = videoMetadataEnrichmentPort.fetchVideoMetadata(
                userUuid,
                videoUuids
        );

        Map<LocalDate, List<DiaryVideo>> videosByDate = pageVideos.stream()
                .collect(Collectors.groupingBy(video -> KstDateTimes.toLocalDate(video.getCreatedAt())));

        List<ThemeTimelineSection> sections = videosByDate.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<DiaryVideo>>comparingByKey().reversed())
                .map(entry -> toSection(entry.getKey(), entry.getValue(), metadataByVideoUuid))
                .toList();

        String nextCursor = hasMore
                ? TimelineCursor.encode(pageVideos.get(pageVideos.size() - 1))
                : null;

        return new ThemeTimelinePage(sections, nextCursor, hasMore);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private ThemeTimelineSection toSection(
            LocalDate date,
            List<DiaryVideo> videos,
            Map<UUID, VideoMetadata> metadataByVideoUuid
    ) {
        List<ThemeTimelineVideo> timelineVideos = videos.stream()
                .sorted(Comparator.comparing(DiaryVideo::getCreatedAt).reversed())
                .map(video -> toTimelineVideo(video, metadataByVideoUuid.get(video.getVideoUuid())))
                .toList();
        return new ThemeTimelineSection(date, timelineVideos);
    }

    private ThemeTimelineVideo toTimelineVideo(DiaryVideo video, VideoMetadata metadata) {
        String caption = metadata != null ? metadata.caption() : null;
        String thumbnailUrl = metadata != null ? metadata.thumbnailUrl() : null;
        return new ThemeTimelineVideo(
                video.getId(),
                video.getVideoUuid(),
                caption,
                thumbnailUrl,
                video.getCreatedAt()
        );
    }
}

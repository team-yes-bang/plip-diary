package com.plip.diary.application.service;

import com.plip.diary.application.port.in.GetDateTimelineUseCase;
import com.plip.diary.application.port.in.dto.DateTimeline;
import com.plip.diary.application.port.in.dto.DateTimelineSection;
import com.plip.diary.application.port.in.dto.DateTimelineVideo;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateTimelineService implements GetDateTimelineUseCase {

    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final VideoServicePort videoServicePort;

    @Override
    public DateTimeline getDateTimeline(UUID userUuid, LocalDate date) {
        LocalDateTime start = KstDateTimes.startOfDay(date);
        LocalDateTime end = KstDateTimes.startOfNextDay(date);

        List<DiaryVideo> videos = diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(userUuid, start, end);
        if (videos.isEmpty()) {
            return new DateTimeline(date, List.of());
        }

        Map<Long, List<DiaryVideo>> videosByThemeId = videos.stream()
                .collect(Collectors.groupingBy(DiaryVideo::getThemeId));

        List<UUID> videoUuids = videos.stream()
                .map(DiaryVideo::getVideoUuid)
                .toList();
        Map<UUID, VideoMetadata> metadataByVideoUuid = videoServicePort.fetchVideoMetadata(userUuid, videoUuids);

        List<DateTimelineSection> sections = diaryThemePersistencePort.findAllByUserUuid(userUuid).stream()
                .filter(theme -> videosByThemeId.containsKey(theme.getId()))
                .map(theme -> toSection(theme, videosByThemeId.get(theme.getId()), metadataByVideoUuid))
                .toList();

        return new DateTimeline(date, sections);
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

package com.plip.diary.application.service;

import com.plip.diary.application.port.in.GetThemeTimelineUseCase;
import com.plip.diary.application.port.in.dto.ThemeTimeline;
import com.plip.diary.application.port.in.dto.ThemeTimelineSection;
import com.plip.diary.application.port.in.dto.ThemeTimelineVideo;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoServicePort;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.exception.ThemeNotFoundException;
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

    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final VideoServicePort videoServicePort;

    @Override
    public ThemeTimeline getThemeTimeline(UUID userUuid, Long themeId) {
        diaryThemePersistencePort.findByIdAndUserUuid(themeId, userUuid)
                .orElseThrow(ThemeNotFoundException::new);

        List<DiaryVideo> videos = diaryVideoPersistencePort.findByThemeIdAndUserUuid(themeId, userUuid);
        if (videos.isEmpty()) {
            return new ThemeTimeline(List.of());
        }

        List<UUID> videoUuids = videos.stream()
                .map(DiaryVideo::getVideoUuid)
                .toList();
        Map<UUID, VideoMetadata> metadataByVideoUuid = videoServicePort.fetchVideoMetadata(userUuid, videoUuids);

        Map<LocalDate, List<DiaryVideo>> videosByDate = videos.stream()
                .collect(Collectors.groupingBy(video -> KstDateTimes.toKstLocalDate(video.getCreatedAt())));

        List<ThemeTimelineSection> sections = videosByDate.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<DiaryVideo>>comparingByKey().reversed())
                .map(entry -> toSection(entry.getKey(), entry.getValue(), metadataByVideoUuid))
                .toList();

        return new ThemeTimeline(sections);
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

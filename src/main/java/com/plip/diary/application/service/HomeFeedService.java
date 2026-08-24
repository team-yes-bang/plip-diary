package com.plip.diary.application.service;

import com.plip.diary.application.port.in.GetHomeFeedUseCase;
import com.plip.diary.application.port.in.dto.HomeFeed;
import com.plip.diary.application.port.in.dto.HomeFeedSection;
import com.plip.diary.application.port.in.dto.HomeFeedVideo;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeFeedService implements GetHomeFeedUseCase {

    static final int SECTION_COUNT = 3;
    static final int MAX_VIDEOS_PER_SECTION = 3;
    private static final int LOOKBACK_DAYS = 365;

    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final VideoServicePort videoMetadataEnrichmentPort;

    @Override
    public HomeFeed getHomeFeed(UUID userUuid) {
        LocalDate today = KstDateTimes.today();
        LocalDateTime start = KstDateTimes.startOfDay(today.minusDays(LOOKBACK_DAYS - 1L));
        LocalDateTime end = KstDateTimes.startOfTomorrow();

        List<DiaryVideo> videos = diaryVideoPersistencePort.findByUserUuidAndCreatedAtRange(userUuid, start, end);
        Map<LocalDate, List<DiaryVideo>> videosByDate = videos.stream()
                .collect(Collectors.groupingBy(video -> KstDateTimes.toLocalDate(video.getCreatedAt())));

        List<LocalDate> sectionDates = new ArrayList<>();
        sectionDates.add(today);

        videosByDate.keySet().stream()
                .filter(date -> date.isBefore(today))
                .sorted(Comparator.reverseOrder())
                .limit(SECTION_COUNT - 1L)
                .forEach(sectionDates::add);

        List<DiaryTheme> themes = diaryThemePersistencePort.findAllByUserUuid(userUuid);
        Map<Long, DiaryTheme> themeById = themes.stream()
                .collect(Collectors.toMap(DiaryTheme::getId, Function.identity()));

        if (sectionDates.size() == 1 && videosByDate.getOrDefault(today, List.of()).isEmpty()) {
            return new HomeFeed(List.of(new HomeFeedSection(today, List.of())), themes);
        }

        List<DiaryVideo> selectedVideos = sectionDates.stream()
                .flatMap(date -> videosByDate.getOrDefault(date, List.of()).stream())
                .toList();

        List<UUID> videoUuids = selectedVideos.stream()
                .map(DiaryVideo::getVideoUuid)
                .toList();
        Map<UUID, VideoMetadata> metadataByVideoUuid = videoUuids.isEmpty()
                ? Map.of()
                : videoMetadataEnrichmentPort.fetchVideoMetadata(userUuid, videoUuids);

        List<HomeFeedSection> sections = sectionDates.stream()
                .map(date -> toSection(
                        date,
                        videosByDate.getOrDefault(date, List.of()),
                        themeById,
                        metadataByVideoUuid
                ))
                .toList();

        return new HomeFeed(sections, themes);
    }

    private HomeFeedSection toSection(
            LocalDate date,
            List<DiaryVideo> videos,
            Map<Long, DiaryTheme> themeById,
            Map<UUID, VideoMetadata> metadataByVideoUuid
    ) {
        List<HomeFeedVideo> feedVideos = videos.stream()
                .sorted(Comparator.comparing(DiaryVideo::getCreatedAt).reversed())
                .limit(MAX_VIDEOS_PER_SECTION)
                .map(video -> toFeedVideo(
                        video,
                        themeById.get(video.getThemeId()),
                        metadataByVideoUuid.get(video.getVideoUuid())
                ))
                .toList();
        return new HomeFeedSection(date, feedVideos);
    }

    private HomeFeedVideo toFeedVideo(DiaryVideo video, DiaryTheme theme, VideoMetadata metadata) {
        String caption = metadata != null ? metadata.caption() : null;
        String thumbnailUrl = metadata != null ? metadata.thumbnailUrl() : null;
        Long themeId = theme != null ? theme.getId() : video.getThemeId();
        String themeName = theme != null ? theme.getName() : null;
        return new HomeFeedVideo(
                video.getId(),
                themeId,
                themeName,
                video.getVideoUuid(),
                caption,
                thumbnailUrl,
                video.getCreatedAt()
        );
    }
}

package com.plip.diary.adapter.in.web.dto;

import com.plip.diary.application.port.in.dto.HomeFeed;
import com.plip.diary.application.port.in.dto.HomeFeedSection;
import com.plip.diary.application.port.in.dto.HomeFeedVideo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "홈 피드 응답")
public record HomeFeedResponse(

        @Schema(description = "날짜별 영상 섹션 (오늘 KST 필수 포함, 최대 3일, 최신 날짜 우선)")
        List<HomeFeedSectionResponse> sections,

        @Schema(description = "활성 테마 목록 (GET /themes 와 동일 스키마)")
        List<ThemeResponse> themes
) {
    public static HomeFeedResponse from(HomeFeed feed) {
        return new HomeFeedResponse(
                feed.sections().stream()
                        .map(HomeFeedSectionResponse::from)
                        .toList(),
                feed.themes().stream()
                        .map(ThemeResponse::from)
                        .toList()
        );
    }
}

@Schema(description = "홈 피드 날짜 섹션")
record HomeFeedSectionResponse(

        @Schema(description = "작성일 (KST, ISO-8601 date)", example = "2026-08-15")
        LocalDate date,

        @Schema(description = "해당 날짜의 영상 목록 (최대 3건, 최신순)")
        List<HomeFeedVideoResponse> videos
) {
    static HomeFeedSectionResponse from(HomeFeedSection section) {
        return new HomeFeedSectionResponse(
                section.date(),
                section.videos().stream()
                        .map(HomeFeedVideoResponse::from)
                        .toList()
        );
    }
}

@Schema(description = "홈 피드 영상 항목")
record HomeFeedVideoResponse(

        @Schema(description = "다이어리 영상 ID", example = "10")
        Long id,

        @Schema(description = "테마 ID", example = "1")
        Long themeId,

        @Schema(description = "테마 이름", example = "일상")
        String themeName,

        @Schema(description = "영상 UUID (video-service 발급, diary 참조)", example = "01912345-6789-7abc-def0-123456789abd")
        UUID videoUuid,

        @Schema(description = "캡션 (Read Model enrichment)")
        String caption,

        @Schema(description = "썸네일 URL (Read Model enrichment)")
        String thumbnailUrl,

        @Schema(description = "바인딩 시각")
        LocalDateTime createdAt
) {
    static HomeFeedVideoResponse from(HomeFeedVideo video) {
        return new HomeFeedVideoResponse(
                video.id(),
                video.themeId(),
                video.themeName(),
                video.videoUuid(),
                video.caption(),
                video.thumbnailUrl(),
                video.createdAt()
        );
    }
}

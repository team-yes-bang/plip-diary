package com.plip.diary.adapter.in.web.dto;

import com.plip.diary.application.port.in.dto.ThemeTimeline;
import com.plip.diary.application.port.in.dto.ThemeTimelineSection;
import com.plip.diary.application.port.in.dto.ThemeTimelineVideo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "테마별 다이어리 타임라인 응답")
public record ThemeTimelineResponse(

        @Schema(description = "날짜별 영상 섹션 (영상이 있는 날짜만, 최신 날짜 우선)")
        List<ThemeTimelineSectionResponse> sections
) {
    public static ThemeTimelineResponse from(ThemeTimeline timeline) {
        return new ThemeTimelineResponse(
                timeline.sections().stream()
                        .map(ThemeTimelineSectionResponse::from)
                        .toList()
        );
    }
}

@Schema(description = "테마별 타임라인 날짜 섹션")
record ThemeTimelineSectionResponse(

        @Schema(description = "작성일 (KST, ISO-8601 date)", example = "2026-08-01")
        LocalDate date,

        @Schema(description = "해당 날짜·테마의 영상 목록")
        List<ThemeTimelineVideoResponse> videos
) {
    static ThemeTimelineSectionResponse from(ThemeTimelineSection section) {
        return new ThemeTimelineSectionResponse(
                section.date(),
                section.videos().stream()
                        .map(ThemeTimelineVideoResponse::from)
                        .toList()
        );
    }
}

@Schema(description = "테마별 타임라인 영상 항목")
record ThemeTimelineVideoResponse(

        @Schema(description = "다이어리 영상 ID", example = "10")
        Long id,

        @Schema(description = "영상 UUID (video-service 참조)", example = "01912345-6789-7abc-def0-123456789abd")
        UUID videoUuid,

        @Schema(description = "캡션 (video-service enrichment)")
        String caption,

        @Schema(description = "썸네일 URL (video-service enrichment)")
        String thumbnailUrl,

        @Schema(description = "바인딩 시각")
        LocalDateTime createdAt
) {
    static ThemeTimelineVideoResponse from(ThemeTimelineVideo video) {
        return new ThemeTimelineVideoResponse(
                video.id(),
                video.videoUuid(),
                video.caption(),
                video.thumbnailUrl(),
                video.createdAt()
        );
    }
}

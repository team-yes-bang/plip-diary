package com.plip.diary.adapter.in.web.dto;

import com.plip.diary.application.port.in.dto.DateTimeline;
import com.plip.diary.application.port.in.dto.DateTimelineSection;
import com.plip.diary.application.port.in.dto.DateTimelineVideo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "날짜별 다이어리 타임라인 응답")
public record DateTimelineResponse(

        @Schema(description = "조회 날짜 (KST, ISO-8601 date)", example = "2026-08-01")
        LocalDate date,

        @Schema(description = "테마별 영상 섹션 (해당 날짜에 영상이 있는 테마만)")
        List<DateTimelineSectionResponse> sections
) {
    public static DateTimelineResponse from(DateTimeline timeline) {
        return new DateTimelineResponse(
                timeline.date(),
                timeline.sections().stream()
                        .map(DateTimelineSectionResponse::from)
                        .toList()
        );
    }
}

@Schema(description = "날짜별 타임라인 테마 섹션")
record DateTimelineSectionResponse(

        @Schema(description = "테마 ID", example = "1")
        Long themeId,

        @Schema(description = "테마 이름", example = "일상")
        String themeName,

        @Schema(description = "해당 테마·날짜의 영상 목록")
        List<DateTimelineVideoResponse> videos
) {
    static DateTimelineSectionResponse from(DateTimelineSection section) {
        return new DateTimelineSectionResponse(
                section.themeId(),
                section.themeName(),
                section.videos().stream()
                        .map(DateTimelineVideoResponse::from)
                        .toList()
        );
    }
}

@Schema(description = "날짜별 타임라인 영상 항목")
record DateTimelineVideoResponse(

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
    static DateTimelineVideoResponse from(DateTimelineVideo video) {
        return new DateTimelineVideoResponse(
                video.id(),
                video.videoUuid(),
                video.caption(),
                video.thumbnailUrl(),
                video.createdAt()
        );
    }
}

package com.plip.diary.adapter.in.web.dto;

import com.plip.diary.application.port.in.dto.DateWindowDay;
import com.plip.diary.application.port.in.dto.DateWindowTimeline;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "날짜별 다이어리 window 조회 응답")
public record DateWindowTimelineResponse(

        @Schema(description = "포커스 날짜 (KST, ISO-8601 date)", example = "2026-08-24")
        LocalDate focusDate,

        @Schema(description = "window 범위 내 날짜별 섹션 (기록 없는 날짜는 sections 빈 배열)")
        List<DateWindowDayResponse> days
) {
    public static DateWindowTimelineResponse from(DateWindowTimeline timeline) {
        return new DateWindowTimelineResponse(
                timeline.focusDate(),
                timeline.days().stream()
                        .map(DateWindowDayResponse::from)
                        .toList()
        );
    }
}

@Schema(description = "window 조회 날짜 항목")
record DateWindowDayResponse(

        @Schema(description = "날짜 (KST, ISO-8601 date)", example = "2026-08-24")
        LocalDate date,

        @Schema(description = "테마별 영상 섹션 (해당 날짜에 영상이 있는 테마만)")
        List<DateTimelineSectionResponse> sections
) {
    static DateWindowDayResponse from(DateWindowDay day) {
        return new DateWindowDayResponse(
                day.date(),
                day.sections().stream()
                        .map(DateTimelineSectionResponse::from)
                        .toList()
        );
    }
}

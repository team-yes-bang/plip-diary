package com.plip.diary.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "월별 캘린더 인디케이터 응답")
public record CalendarResponse(

        @Schema(description = "조회 연도", example = "2026")
        int year,

        @Schema(description = "조회 월", example = "8")
        int month,

        @Schema(description = "영상 작성일 목록 (KST, ISO-8601 date)")
        List<LocalDate> dates
) {
}

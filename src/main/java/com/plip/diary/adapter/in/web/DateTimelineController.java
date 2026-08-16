package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.in.web.dto.DateTimelineResponse;
import com.plip.diary.application.port.in.GetDateTimelineUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Date Timeline", description = "날짜별 다이어리 타임라인 API")
@RestController
@RequestMapping("/api/diaries/dates")
@RequiredArgsConstructor
public class DateTimelineController {

    static final String USER_UUID_HEADER = ThemeController.USER_UUID_HEADER;

    private final GetDateTimelineUseCase getDateTimelineUseCase;

    @Operation(
            summary = "날짜별 다이어리 조회",
            description = "특정일(KST) 영상을 테마별 섹션으로 반환. `diary_videos` + video-service 썸네일·캡션 enrichment. "
                    + "테마 전체 목록은 `GET /api/diaries/themes` 별도 호출."
    )
    @GetMapping("/{date}")
    public DateTimelineResponse getDateTimeline(
            @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return DateTimelineResponse.from(getDateTimelineUseCase.getDateTimeline(userUuid, date));
    }
}

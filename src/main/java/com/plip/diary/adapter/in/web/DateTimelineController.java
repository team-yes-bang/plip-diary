package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.in.web.dto.DateTimelineResponse;
import com.plip.diary.adapter.in.web.dto.DateWindowTimelineResponse;
import com.plip.diary.adapter.in.web.dto.ThemeTimelineResponse;
import com.plip.diary.application.port.in.GetDateTimelineUseCase;
import com.plip.diary.application.port.in.GetThemeTimelineUseCase;
import com.plip.diary.application.service.ThemeTimelineService;
import com.plip.diary.global.config.SwaggerConfig;
import com.plip.diary.global.pagination.TimelineCursor;
import com.plip.diary.global.web.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Timeline", description = "다이어리 타임라인 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH_SCHEME)
@RestController
@RequiredArgsConstructor
public class DateTimelineController {

    static final String USER_UUID_HEADER = RequestHeaders.USER_UUID_HEADER;

    private final GetDateTimelineUseCase getDateTimelineUseCase;
    private final GetThemeTimelineUseCase getThemeTimelineUseCase;

    @Operation(
            summary = "날짜별 다이어리 조회",
            description = "특정일(KST) 영상을 테마별 섹션으로 반환. `window=1`이면 전·당·다음날 3일치를 함께 반환한다. "
                    + "`diary_videos` + Read Model(캡션·썸네일) enrichment. "
                    + "테마 전체 목록은 `GET /api/v1/diaries/themes` 별도 호출."
    )
    @GetMapping("/api/v1/diaries/dates/{date}")
    public Object getDateTimeline(
            @Parameter(hidden = true) @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "포커스 날짜 기준 ±N일 window (0이면 단일 날짜)")
            @RequestParam(defaultValue = "0") int window
    ) {
        if (window > 0) {
            return DateWindowTimelineResponse.from(getDateTimelineUseCase.getDateWindowTimeline(userUuid, date, window));
        }
        return DateTimelineResponse.from(getDateTimelineUseCase.getDateTimeline(userUuid, date));
    }

    @Operation(
            summary = "테마별 다이어리 조회",
            description = "특정 테마의 영상을 날짜별 섹션(KST date)으로 반환. "
                    + "영상 50건 초과 시 keyset pagination — 1차는 cursor 생략, "
                    + "2차부터 직전 응답 nextCursor를 cursor query에 그대로 전달. "
                    + "`diary_videos` + Read Model(캡션·썸네일) enrichment. "
                    + "테마 메타는 `GET /api/v1/diaries/themes/{id}` 별도 호출."
    )
    @GetMapping("/api/v1/diaries/themes/{id}/timeline")
    public ThemeTimelineResponse getThemeTimeline(
            @Parameter(hidden = true) @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @PathVariable Long id,
            @Parameter(description = TimelineCursor.OPENAPI_DESCRIPTION, example = TimelineCursor.OPENAPI_EXAMPLE)
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지당 영상 수 (최대 50)")
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ThemeTimelineResponse.from(getThemeTimelineUseCase.getThemeTimeline(userUuid, id, cursor, limit));
    }
}

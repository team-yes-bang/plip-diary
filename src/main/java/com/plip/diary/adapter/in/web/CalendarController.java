package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.in.web.dto.CalendarResponse;
import com.plip.diary.application.port.in.GetCalendarUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Calendar", description = "캘린더 API")
@RestController
@RequestMapping("/api/v1/diaries/calendar")
@RequiredArgsConstructor
public class CalendarController {

    static final String USER_UUID_HEADER = ThemeController.USER_UUID_HEADER;

    private final GetCalendarUseCase getCalendarUseCase;

    @Operation(
            summary = "월별 작성일 조회",
            description = "캘린더 dot 표시용 작성일 목록. 활성 테마·영상만 포함. 영상 메타·썸네일 미포함."
    )
    @GetMapping
    public CalendarResponse getCalendar(
            @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @RequestParam int year,
            @RequestParam int month
    ) {
        var dates = getCalendarUseCase.getCalendarDates(userUuid, year, month);
        return new CalendarResponse(year, month, dates);
    }
}

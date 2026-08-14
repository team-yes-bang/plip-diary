package com.plip.diary.adapter.in.web.dto;

import com.plip.diary.domain.model.DiaryTheme;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "테마 목록 응답")
public record ThemeListResponse(

        @Schema(description = "활성 테마 목록")
        List<ThemeResponse> themes
) {
    public static ThemeListResponse from(List<DiaryTheme> themes) {
        return new ThemeListResponse(themes.stream().map(ThemeResponse::from).toList());
    }
}

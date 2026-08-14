package com.plip.diary.adapter.in.web.dto;

import com.plip.diary.domain.model.DiaryTheme;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "테마 응답")
public record ThemeResponse(

        @Schema(description = "테마 ID", example = "1")
        Long themeId,

        @Schema(description = "테마 이름", example = "일상")
        String name,

        @Schema(description = "생성 시각")
        LocalDateTime createdAt,

        @Schema(description = "수정 시각")
        LocalDateTime updatedAt
) {
    public static ThemeResponse from(DiaryTheme theme) {
        return new ThemeResponse(
                theme.getThemeId(),
                theme.getName(),
                theme.getCreatedAt(),
                theme.getUpdatedAt()
        );
    }
}

package com.plip.diary.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "테마 수정 요청")
public record UpdateThemeRequest(

        @Schema(description = "테마 이름", example = "여행 기록")
        @NotBlank
        @Size(max = 50)
        String name
) {
}

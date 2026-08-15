package com.plip.diary.adapter.in.web.dto;

import com.plip.diary.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 오류 응답")
public record ErrorResponse(

        @Schema(description = "오류 코드", example = "THEME_001")
        String code,

        @Schema(description = "오류 메시지", example = "테마를 찾을 수 없습니다.")
        String message
) {
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }
}

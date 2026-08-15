package com.plip.diary.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Theme
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "THEME_001", "테마를 찾을 수 없습니다."),
    THEME_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "THEME_002", "테마는 최대 5개까지 생성할 수 있습니다."),
    THEME_LAST_REMAINING(HttpStatus.CONFLICT, "THEME_003", "마지막 남은 테마는 삭제할 수 없습니다."),
    THEME_NAME_DUPLICATE(HttpStatus.CONFLICT, "THEME_004", "이미 사용 중인 테마 이름입니다."),

    // Common (user-service COMMON_* 정합)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다.");

    public static final int MAX_ACTIVE_THEMES = 5;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

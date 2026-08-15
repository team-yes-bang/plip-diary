package com.plip.diary.global.exception;

public class ThemeLimitExceededException extends BusinessException {

    public ThemeLimitExceededException() {
        super(ErrorCode.THEME_LIMIT_EXCEEDED);
    }
}

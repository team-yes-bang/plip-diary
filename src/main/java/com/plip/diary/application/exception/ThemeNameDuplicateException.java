package com.plip.diary.application.exception;

public class ThemeNameDuplicateException extends BusinessException {

    public ThemeNameDuplicateException() {
        super(ErrorCode.THEME_NAME_DUPLICATE);
    }
}

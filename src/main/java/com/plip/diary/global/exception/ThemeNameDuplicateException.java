package com.plip.diary.global.exception;

public class ThemeNameDuplicateException extends BusinessException {

    public ThemeNameDuplicateException() {
        super(ErrorCode.THEME_NAME_DUPLICATE);
    }
}

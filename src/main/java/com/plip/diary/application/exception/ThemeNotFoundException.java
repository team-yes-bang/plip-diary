package com.plip.diary.application.exception;

public class ThemeNotFoundException extends BusinessException {

    public ThemeNotFoundException() {
        super(ErrorCode.THEME_NOT_FOUND);
    }
}

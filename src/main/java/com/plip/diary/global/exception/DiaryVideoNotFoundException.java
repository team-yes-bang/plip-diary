package com.plip.diary.global.exception;

public class DiaryVideoNotFoundException extends BusinessException {

    public DiaryVideoNotFoundException() {
        super(ErrorCode.DIARY_VIDEO_NOT_FOUND);
    }
}

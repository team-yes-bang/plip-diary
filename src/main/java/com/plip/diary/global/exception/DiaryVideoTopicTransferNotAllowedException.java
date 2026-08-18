package com.plip.diary.global.exception;

public class DiaryVideoTopicTransferNotAllowedException extends BusinessException {

    public DiaryVideoTopicTransferNotAllowedException() {
        super(ErrorCode.DIARY_VIDEO_TOPIC_TRANSFER_NOT_ALLOWED);
    }
}

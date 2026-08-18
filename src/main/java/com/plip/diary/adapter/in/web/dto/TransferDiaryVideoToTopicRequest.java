package com.plip.diary.adapter.in.web.dto;

import com.plip.diary.application.port.in.VideoTransferMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "다이어리 영상 → 토픽 이동 요청 (diary 담당 구간)")
public record TransferDiaryVideoToTopicRequest(

        @Schema(description = "MOVE: topic-service 바인딩 완료 후 다이어리 타임라인에서 제거. COPY는 topic-service API 사용")
        @NotNull
        VideoTransferMode mode
) {
}

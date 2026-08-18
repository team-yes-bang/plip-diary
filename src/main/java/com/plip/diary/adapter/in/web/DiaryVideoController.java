package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.in.web.dto.TransferDiaryVideoToTopicRequest;
import com.plip.diary.application.port.in.TransferDiaryVideoToTopicUseCase;
import com.plip.diary.application.port.in.UnbindDiaryVideoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Diary Video", description = "다이어리 영상 API")
@RestController
@RequestMapping("/api/diaries/videos")
@RequiredArgsConstructor
public class DiaryVideoController {

    static final String USER_UUID_HEADER = ThemeController.USER_UUID_HEADER;

    private final UnbindDiaryVideoUseCase unbindDiaryVideoUseCase;
    private final TransferDiaryVideoToTopicUseCase transferDiaryVideoToTopicUseCase;

    @Operation(
            summary = "다이어리 영상 → 토픽 복사·이동",
            description = "topic-service에 영상 등록이 끝난 뒤(MOVE), 다이어리 타임라인에서 제거. "
                    + "`diary.video.unlinked` 발행. COPY는 topic-service API 사용."
    )
    @PostMapping("/{diaryVideoId}/topic-transfer")
    public ResponseEntity<Void> transferDiaryVideoToTopic(
            @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @PathVariable Long diaryVideoId,
            @Valid @RequestBody TransferDiaryVideoToTopicRequest request
    ) {
        transferDiaryVideoToTopicUseCase.transfer(userUuid, diaryVideoId, request.mode());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "다이어리 영상 바인딩 해제",
            description = "타임라인에서 영상 항목 제거. `diary_videos` Soft Delete 후 `diary.video.unlinked` Kafka 발행."
    )
    @DeleteMapping("/{diaryVideoId}")
    public ResponseEntity<Void> unbindDiaryVideo(
            @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @PathVariable Long diaryVideoId
    ) {
        unbindDiaryVideoUseCase.unbindDiaryVideo(userUuid, diaryVideoId);
        return ResponseEntity.noContent().build();
    }
}

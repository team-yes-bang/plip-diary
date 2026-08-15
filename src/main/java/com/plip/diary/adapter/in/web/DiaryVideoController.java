package com.plip.diary.adapter.in.web;

import com.plip.diary.application.port.in.UnbindDiaryVideoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(
            summary = "다이어리 영상 바인딩 해제",
            description = "타임라인에서 영상 항목 제거. `diary_videos` Soft Delete 후 `diary.video.deleted` Kafka 발행."
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

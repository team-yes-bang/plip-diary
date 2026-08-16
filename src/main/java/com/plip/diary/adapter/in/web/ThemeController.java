package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.in.web.dto.CreateThemeRequest;
import com.plip.diary.adapter.in.web.dto.ThemeListResponse;
import com.plip.diary.adapter.in.web.dto.ThemeResponse;
import com.plip.diary.adapter.in.web.dto.UpdateThemeRequest;
import com.plip.diary.application.port.in.CreateThemeUseCase;
import com.plip.diary.application.port.in.DeleteThemeUseCase;
import com.plip.diary.application.port.in.GetThemeUseCase;
import com.plip.diary.application.port.in.ListThemesUseCase;
import com.plip.diary.application.port.in.UpdateThemeUseCase;
import com.plip.diary.global.web.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Theme", description = "다이어리 테마 API")
@RestController
@RequestMapping("/api/diaries/themes")
@RequiredArgsConstructor
public class ThemeController {

    public static final String USER_UUID_HEADER = RequestHeaders.USER_UUID_HEADER;

    private final CreateThemeUseCase createThemeUseCase;
    private final ListThemesUseCase listThemesUseCase;
    private final GetThemeUseCase getThemeUseCase;
    private final UpdateThemeUseCase updateThemeUseCase;
    private final DeleteThemeUseCase deleteThemeUseCase;

    @Operation(summary = "테마 목록 조회", description = "사용자의 활성 테마 목록 반환")
    @GetMapping
    public ThemeListResponse listThemes(@RequestHeader(USER_UUID_HEADER) UUID userUuid) {
        return ThemeListResponse.from(listThemesUseCase.listThemes(userUuid));
    }

    @Operation(
            summary = "테마 메타 단건 조회",
            description = "테마 이름·생성일 등 **메타 정보만** 반환. "
                    + "테마에 포함된 영상 목록은 `GET /api/diaries/themes/{id}/timeline` 사용."
    )
    @GetMapping("/{id}")
    public ThemeResponse getTheme(
            @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @PathVariable Long id
    ) {
        return ThemeResponse.from(getThemeUseCase.getTheme(userUuid, id));
    }

    @Operation(summary = "테마 생성", description = "활성 테마는 사용자당 최대 5개까지 생성 가능")
    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(
            @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @Valid @RequestBody CreateThemeRequest request
    ) {
        var theme = createThemeUseCase.createTheme(userUuid, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ThemeResponse.from(theme));
    }

    @Operation(summary = "테마 이름 수정")
    @PatchMapping("/{id}")
    public ThemeResponse updateTheme(
            @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @PathVariable Long id,
            @Valid @RequestBody UpdateThemeRequest request
    ) {
        var theme = updateThemeUseCase.updateTheme(userUuid, id, request.name());
        return ThemeResponse.from(theme);
    }

    @Operation(summary = "테마 삭제", description = "테마와 연관 영상을 Soft Delete 처리")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(
            @RequestHeader(USER_UUID_HEADER) UUID userUuid,
            @PathVariable Long id
    ) {
        deleteThemeUseCase.deleteTheme(userUuid, id);
        return ResponseEntity.noContent().build();
    }
}

package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.in.web.dto.HomeFeedResponse;
import com.plip.diary.application.port.in.GetHomeFeedUseCase;
import com.plip.diary.global.config.SwaggerConfig;
import com.plip.diary.global.web.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Home", description = "홈 피드 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH_SCHEME)
@RestController
@RequiredArgsConstructor
public class HomeFeedController {

    static final String USER_UUID_HEADER = RequestHeaders.USER_UUID_HEADER;

    private final GetHomeFeedUseCase getHomeFeedUseCase;

    @Operation(
            summary = "홈 피드 조회",
            description = "KST 오늘(0건이어도 포함) + 영상이 있는 최신 2일, 일자별 최대 3건. "
                    + "`diary_videos` + Read Model(캡션·썸네일) enrichment. "
                    + "활성 테마 목록은 응답 `themes`에 포함. 테마 CRUD·기타 화면은 `GET /api/v1/diaries/themes` 사용. "
                    + "특정일 전체는 `GET /api/v1/diaries/dates/{date}`."
    )
    @GetMapping("/api/v1/diaries/home")
    public HomeFeedResponse getHomeFeed(
            @Parameter(hidden = true) @RequestHeader(USER_UUID_HEADER) UUID userUuid
    ) {
        return HomeFeedResponse.from(getHomeFeedUseCase.getHomeFeed(userUuid));
    }
}

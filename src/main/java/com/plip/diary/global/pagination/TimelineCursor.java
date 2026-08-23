package com.plip.diary.global.pagination;

import com.plip.diary.domain.model.DiaryVideo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 테마 타임라인 keyset cursor — {@code createdAt|id} 형식.
 */
public final class TimelineCursor {

    /** OpenAPI/Swagger — 요청 {@code cursor}·응답 {@code nextCursor} 공통 설명 */
    public static final String OPENAPI_DESCRIPTION = """
            테마 타임라인 keyset pagination 커서. 첫 요청은 생략한다. \
            이전 응답의 nextCursor 값을 그대로 cursor query에 넣는다. \
            형식: {diary_videos.created_at}|{diary_videos.id} — \
            ISO-8601 LocalDateTime(예: 2026-08-23T06:00:00) + '|' + 다이어리 영상 숫자 id. \
            sections.date(KST)나 video_uuid가 아니다.""";

    public static final String OPENAPI_EXAMPLE = "2026-08-23T06:00:00|87";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String SEPARATOR = "|";

    private TimelineCursor() {
    }

    public static String encode(DiaryVideo video) {
        return encode(video.getCreatedAt(), video.getId());
    }

    public static String encode(LocalDateTime createdAt, Long id) {
        return FORMATTER.format(createdAt) + SEPARATOR + id;
    }

    public static Decoded decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        int separatorIndex = cursor.lastIndexOf(SEPARATOR);
        if (separatorIndex <= 0 || separatorIndex >= cursor.length() - 1) {
            throw new IllegalArgumentException("Invalid timeline cursor: " + cursor);
        }

        String createdAtRaw = cursor.substring(0, separatorIndex);
        String idRaw = cursor.substring(separatorIndex + 1);

        try {
            LocalDateTime createdAt = LocalDateTime.parse(createdAtRaw, FORMATTER);
            Long id = Long.valueOf(idRaw);
            return new Decoded(createdAt, id);
        } catch (DateTimeParseException | NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid timeline cursor: " + cursor, ex);
        }
    }

    public record Decoded(LocalDateTime createdAt, Long id) {
    }
}

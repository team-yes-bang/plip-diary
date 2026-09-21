package com.plip.diary.adapter.out.redis;

import java.time.LocalDate;
import java.util.UUID;

final class DiaryTimelineCacheKeys {

    static final String INDEX_PREFIX = "diary:tl-idx:";
    static final String HOME_PREFIX = "diary:tl:home:";
    static final String DATE_PREFIX = "diary:tl:date:";
    static final String THEME_PREFIX = "diary:tl:theme:";

    private DiaryTimelineCacheKeys() {
    }

    static String indexKey(UUID userUuid) {
        return INDEX_PREFIX + userUuid;
    }

    static String homeKey(UUID userUuid) {
        return HOME_PREFIX + userUuid;
    }

    static String dateKey(UUID userUuid, LocalDate date) {
        return DATE_PREFIX + userUuid + ":" + date;
    }

    static String themeKey(UUID userUuid, Long themeId, String cursor, int limit) {
        String cursorPart = cursor != null ? cursor : "_";
        return THEME_PREFIX + userUuid + ":" + themeId + ":" + cursorPart + ":" + limit;
    }
}

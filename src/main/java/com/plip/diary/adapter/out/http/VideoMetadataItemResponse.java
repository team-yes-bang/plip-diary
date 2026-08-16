package com.plip.diary.adapter.out.http;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

record VideoMetadataItemResponse(
        @JsonAlias("video_uuid")
        UUID videoUuid,
        String caption,
        @JsonAlias("thumbnail_url")
        String thumbnailUrl
) {
}

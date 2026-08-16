package com.plip.diary.adapter.out.mongodb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "diary_video_metadata")
@CompoundIndex(name = "idx_user_uuid", def = "{'user_uuid': 1}")
class DiaryVideoMetadataDocument {

    @Id
    private UUID videoUuid;

    @Field("user_uuid")
    private UUID userUuid;

    private String caption;

    @Field("thumbnail_url")
    private String thumbnailUrl;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}

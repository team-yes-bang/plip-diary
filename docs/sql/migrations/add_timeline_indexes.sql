-- 타임라인 조회 최적화 인덱스 (Phase 3)

-- video_uuid 단건 조회 지원 (existsByThemeIdAndVideoUuid 등)
CREATE INDEX idx_video_uuid ON diary_videos (video_uuid);

-- 100만 사용자 규모 도달 시 user_uuid 비정규화 고려:
-- ALTER TABLE diary_videos ADD COLUMN user_uuid BINARY(16);
-- UPDATE diary_videos dv JOIN diary_themes dt ON dv.theme_id = dt.id SET dv.user_uuid = dt.user_uuid;
-- ALTER TABLE diary_videos MODIFY COLUMN user_uuid BINARY(16) NOT NULL;
-- CREATE INDEX idx_user_created ON diary_videos (user_uuid, created_at DESC, deleted_at);

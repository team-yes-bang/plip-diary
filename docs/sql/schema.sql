CREATE TABLE diary_themes (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    theme_uuid BINARY(16)   NOT NULL,
    user_uuid  BINARY(16)   NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME     NULL,
    UNIQUE INDEX uk_theme_uuid (theme_uuid),
    INDEX idx_user_deleted (user_uuid, deleted_at)
);

CREATE TABLE diary_videos (
    id         BIGINT     AUTO_INCREMENT PRIMARY KEY,
    theme_id   BIGINT     NOT NULL,
    video_uuid BINARY(16) NOT NULL,
    created_at DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME   NULL,
    CONSTRAINT fk_videos_themes FOREIGN KEY (theme_id) REFERENCES diary_themes(id),
    INDEX idx_theme_created (theme_id, created_at DESC, deleted_at)
);

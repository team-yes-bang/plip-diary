package com.plip.diary.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "diary_videos")
public class DiaryVideoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_video_id")
    private Long diaryVideoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private DiaryThemeEntity theme;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "video_uuid", nullable = false, length = 16)
    private UUID videoUuid;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private DiaryVideoEntity(
            Long diaryVideoId,
            DiaryThemeEntity theme,
            UUID videoUuid,
            LocalDateTime createdAt,
            LocalDateTime deletedAt
    ) {
        this.diaryVideoId = diaryVideoId;
        this.theme = theme;
        this.videoUuid = videoUuid;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}

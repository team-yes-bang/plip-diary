# diary.video.uploaded v1

video-service에서 **다이어리 영상** 업로드 완료 시 발행하는 이벤트. diary-service가 구독하여 `diary_videos`에 바인딩한다.

> 토픽(그룹) 영상 업로드는 `topic.video.uploaded`(topic-service 구독). 본 토픽은 diary-service 전용.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `diary.video.uploaded` |
| Consumer Group | `diary-service` |
| Message Key | `videoUuid` |
| Value format | JSON |
| Consumer | **diary-service** |

## Payload (video-service 발행 기준)

```json
{
  "themeUuid": "01912345-6789-7abc-def0-123456789abc",
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "userUuid": "01912345-6789-7abc-def0-123456789abe",
  "caption": "캡션",
  "thumbnailUrl": "https://cdn.example/thumb.jpg",
  "occurredAt": "2026-08-12T11:00:00"
}
```

| 필드 | 타입 | 필수 | diary-service 사용 |
| --- | --- | --- | --- |
| `themeUuid` | string (UUIDv7) | Y | Y — `diary_themes.theme_uuid` lookup |
| `videoUuid` | string (UUIDv7) | Y | Y — `diary_videos.video_uuid` |
| `userUuid` | string (UUIDv7) | Y | Y — 소유권 검증 |
| `caption` | string | N | Y — Mongo projection upsert |
| `thumbnailUrl` | string | N | Y — Mongo projection upsert |
| `occurredAt` | string (ISO 8601) | N | N |

## Consumer 동작 (diary-service)

1. `themeUuid`, `videoUuid`, `userUuid` 추출 — 누락 시 warn + skip
2. `theme_uuid`로 활성 `diary_themes` 조회 — 없으면 warn + skip
3. `user_uuid` 일치 검증 — 불일치 시 warn + skip
4. KST 당일 유저 전체 20건 limit — 초과 시 warn + skip
5. 동일 `(theme_id, video_uuid)` 활성 row 존재 시 멱등 skip
6. 신규 바인딩 시 `diary_videos` INSERT → afterCommit `diary.video.linked` 발행
7. **항상** Mongo `diary_video_metadata` upsert + Redis evict (멱등·재처리 복구)

## 발행 시점 (video-service)

- 다이어리 영상 업로드 완료 후 `video` row 생성 직후

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | `video.uploaded` 대체 — 구독 주체(diary) 명시 |

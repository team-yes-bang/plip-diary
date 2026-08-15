# video.uploaded v1

video-service에서 영상 업로드 완료 시 발행하는 이벤트. diary-service는 이 이벤트를 구독하여 `diary_videos`에 `video_uuid`를 바인딩한다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `video.uploaded` |
| Consumer Group | `diary-service` |
| Message Key | `videoUuid` (video-service 발행 기준) |
| Value format | JSON |

## Payload (video-service 발행 기준)

```json
{
  "themeUuid": "01912345-6789-7abc-def0-123456789abc",
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "userUuid": "01912345-6789-7abc-def0-123456789abe",
  "occurredAt": "2026-08-12T11:00:00"
}
```

| 필드 | 타입 | 필수 | diary-service 사용 |
| --- | --- | --- | --- |
| `themeUuid` | string (UUIDv7) | Y | Y — `diary_themes.theme_uuid` lookup |
| `videoUuid` | string (UUIDv7) | Y | Y — `diary_videos.video_uuid` |
| `userUuid` | string (UUIDv7) | Y | Y — 소유권 검증 |
| `occurredAt` | string (ISO 8601) | N | N |

> diary Consumer는 `themeUuid`, `videoUuid`, `userUuid`를 사용한다. `@JsonAlias`로 snake_case도 수용.

## Consumer 동작 (diary-service)

1. `themeUuid`, `videoUuid`, `userUuid` 추출
2. `theme_uuid`로 활성 `diary_themes` 조회 — 없으면 warn + skip
3. `user_uuid` 일치 검증 — 불일치 시 warn + skip
4. KST 당일 유저 전체 20건 limit (`deleted_at IS NULL`, 테마 구분 없음) — 초과 시 warn + skip
5. 동일 `(theme_id, video_uuid)` 활성 row 존재 시 멱등 skip
6. `diary_videos` INSERT

## 발행 시점 (video-service)

- 영상 업로드 완료 후 `video` row 생성 직후

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | 최초 정의 — video-service payload(camelCase) 정합 |

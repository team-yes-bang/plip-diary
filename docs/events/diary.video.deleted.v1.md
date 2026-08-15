# diary.video.deleted v1

diary-service에서 다이어리 타임라인 항목(`diary_videos`) Soft Delete 시 발행하는 이벤트. video-service는 이 이벤트를 구독하여 `video` row를 Soft Delete한다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `diary.video.deleted` |
| Message Key | `videoUuid` (diary-service 발행 기준) |
| Value format | JSON |

## Payload (diary-service 발행 기준)

```json
{
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "userUuid": "01912345-6789-7abc-def0-123456789abe",
  "occurredAt": "2026-08-12T11:00:00"
}
```

| 필드 | 타입 | 필수 | video-service 사용 |
| --- | --- | --- | --- |
| `videoUuid` | string (UUIDv7) | Y | Y — `video.video_uuid` lookup |
| `userUuid` | string (UUIDv7) | Y | Y — 소유권 검증 |
| `occurredAt` | string (ISO 8601) | N | N |

> `@JsonAlias`로 snake_case(`video_uuid`, `user_uuid`, `occurred_at`)도 수용 가능.

## Producer 동작 (diary-service)

1. `DELETE /api/diaries/videos/{diaryVideoId}` — 본인·활성 `diary_videos` Soft Delete
2. 트랜잭션 커밋 후 `diary.video.deleted` 발행

## Consumer 동작 (video-service)

1. `videoUuid`, `userUuid` 추출
2. `video_uuid`로 활성 `video` 조회 — 없으면 skip
3. `user_uuid` 일치 검증 — 불일치 시 skip
4. `video` Soft Delete

## 발행 시점 (diary-service)

- 다이어리 타임라인에서 영상 항목 제거 API 성공 후 (DB 커밋 이후)

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | 최초 정의 — diary-service payload(camelCase) |

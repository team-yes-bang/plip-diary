# video.caption.updated v1

video-service에서 영상 캡션 수정 시 발행하는 이벤트. diary-service는 이 이벤트를 구독하여 Mongo projection(`diary_video_metadata`)의 **caption만** patch한다.

> **정본 소유:** video-service. diary는 Consumer 동작·기대 payload를 본 문서에 기록하고 video-service에 통보한다.
>
> **제품 맥락:** diary FE 그리드 카드(홈·날짜·테마 목록)에 썸네일 이미지·캡션을 함께 표시. 썸네일은 업로드 시 1회 생성 후 변경 없음 — 본 이벤트로 thumbnail 갱신하지 않음.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `video.caption.updated` |
| Consumer Group | `diary-service` |
| Message Key | `videoUuid` |
| Value format | JSON |

## Payload (diary-service Consumer 기대)

```json
{
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "userUuid": "01912345-6789-7abc-def0-123456789abe",
  "caption": "수정된 캡션",
  "occurredAt": "2026-08-16T12:00:00"
}
```

| 필드 | 타입 | 필수 | diary-service 사용 |
| --- | --- | --- | --- |
| `videoUuid` | string (UUIDv7) | Y | Y — projection `_id` lookup |
| `userUuid` | string (UUIDv7) | Y | Y — 소유자 검증·Redis evict |
| `caption` | string | Y | Y — projection caption 갱신 |
| `occurredAt` | string (ISO 8601) | N | N |

> `@JsonAlias`로 snake_case(`video_uuid`, `user_uuid`) 수용. `caption` null이면 skip.

## Consumer 동작 (diary-service)

1. `videoUuid`, `userUuid`, `caption` 추출 — 누락 시 warn + skip
2. Mongo `diary_video_metadata` caption patch, `updated_at` 갱신 (`thumbnail_url` 변경 없음)
3. Redis `diary:video-meta:{userUuid}:{videoUuid}` evict

## 발행 시점 (video-service)

- 영상 캡션 수정 API 성공 후 (F-06, 개별 뷰어)

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | Phase 5-2 — diary Consumer 스펙 선정의 (video-service 통보용) |

# diary.video.unlinked v1

diary-service에서 다이어리 영상 바인딩 해제 시 발행하는 이벤트. **구독 주체: video-service** (토픽 서비스는 구독하지 않음).

> HTTP 동기 응답이 아니다. diary-service가 authoritative store에 바인딩 해제를 반영한 **후** video-service에 알리는 비동기 이벤트이다. GC 배치(30일)는 이 이벤트 기준으로 판단하며, ms~s 지연은 수명주기에 무의미하다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `diary.video.unlinked` |
| Message Key | `videoUuid` |
| Value format | JSON |
| Consumer | **video-service** |

## Payload (diary-service 발행 기준)

```json
{
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "occurredAt": "2026-08-12T11:00:00"
}
```

| 필드 | 타입 | 필수 | video-service 사용 |
| --- | --- | --- | --- |
| `videoUuid` | string (UUIDv7) | Y | Y — 참조 검증·고아 판별 대상 |
| `occurredAt` | string (ISO 8601) | N | N |

> `@JsonAlias`로 snake_case(`video_uuid`, `occurred_at`) 수용.

## Producer 동작 (diary-service)

1. `DELETE /api/v1/diaries/videos/{diaryVideoId}` — 바인딩 해제
2. `POST /api/v1/diaries/videos/{diaryVideoId}/topic-transfer` — 토픽 이동(`MOVE`) 시 다이어리 Soft Delete
3. Mongo projection delete + Redis evict
4. 트랜잭션 커밋 후 `diary.video.unlinked` 발행

## Consumer 동작 (video-service)

1. `videoUuid` 추출
2. 다이어리·토픽 등 **다른 서비스 활성 바인딩** 조회
3. **활성 바인딩 있음** — 아무 작업 없음
4. **완전 고아** — `ZADD orphan_videos:zset <현재+30일 timestamp> <video_uuid>`

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | 최초 정의 — `video.unlinked`에서 diary 구독 주체 명시형으로 변경 |

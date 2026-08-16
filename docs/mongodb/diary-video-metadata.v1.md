# diary_video_metadata v1

diary-service **소유** MongoDB projection 컬렉션. Phase 5-2 Kafka Consumer로 적재, Phase 5-3 조회 enrichment 소스.

> CQRS Query side materialized view. MySQL Write Model(`diary_videos`)과 비동기 동기화.

## Collection

| 항목 | 값 |
| --- | --- |
| Database | `plip_diary_read` (로컬·docker 기본) |
| Collection | `diary_video_metadata` |
| Document class | `DiaryVideoMetadataDocument` (`adapter/out/mongodb/`) |

## Document 스키마

```json
{
  "_id": "01912345-6789-7abc-def0-123456789abd",
  "user_uuid": "01912345-6789-7abc-def0-123456789abe",
  "caption": "캡션",
  "thumbnail_url": "https://cdn.example/thumb.jpg",
  "updated_at": "2026-08-16T07:30:00"
}
```

| 필드 | BSON 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `_id` | UUID (Binary) | Y | `video_uuid` — video-service 전역 식별자, upsert 멱등 키 |
| `user_uuid` | UUID (Binary) | Y | 소유자. 조회·삭제 시 필터 |
| `caption` | string | N | 목록 UI 캡션 (video-service 원본 동기화) |
| `thumbnail_url` | string | N | CDN 썸네일 URL |
| `updated_at` | datetime | Y | projection 최종 갱신 시각 (UTC naive, `LocalDateTime`) |

## 인덱스

| 이름 | 키 | 용도 |
| --- | --- | --- |
| `_id_` | `_id` | `video_uuid` 단건 upsert·delete |
| `idx_user_uuid` | `user_uuid` | 유저 범위 batch 조회 |

> 샤딩 키 후보: `user_uuid` (5천만 사용자 규모 — 인프라 합의 후 적용).

## 동기화 트리거 (Phase 5-2)

| 이벤트 | 발행 서비스 | diary 동작 | 스펙 문서 |
| --- | --- | --- | --- |
| `video.uploaded` (+ 메타 확장) | video-service | upsert | [video.uploaded.v1.md](../events/video.uploaded.v1.md) (diary Consumer 관점) |
| `video.caption.updated` | video-service | patch — caption·thumbnail | video-service `docs/events/` (canonical) |
| `diary.video.deleted` | diary-service | delete | [diary.video.deleted.v1.md](../events/diary.video.deleted.v1.md) |

## 조회 사용 (Phase 5-3)

- `VideoMetadataQueryPort` → `VideoServicePort.fetchVideoMetadata` 대체 (`VideoMetadataQueryAdapter`)
- Redis 키: `diary:video-meta:{userUuid}:{videoUuid}` (TTL: `diary.query-side.cache-ttl`, 기본 24h)

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | Phase 5-1 — 컬렉션·필드·인덱스 정의 |

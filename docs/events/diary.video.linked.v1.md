# diary.video.linked v1

diary-service에서 **다이어리 바인딩이 DB에 확정된 뒤** 발행하는 이벤트. video-service가 구독하여 cross-service **참조 수명주기**를 갱신한다.

> HTTP 동기 응답이 아니다. `diary.video.uploaded` 처리 **후** diary가 authoritative하게 “바인딩 완료”를 알리는 **비동기 도메인 이벤트**이다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `diary.video.linked` |
| Message Key | `videoUuid` |
| Value format | JSON |
| Consumer | **video-service** |

## Payload

```json
{
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "occurredAt": "2026-08-12T11:00:00"
}
```

## Producer 동작 (diary-service)

1. `diary.video.uploaded` Consumer — 신규 `diary_videos` INSERT 성공
2. 토픽→다이어리 등 **동일 Consumer 경로**로 재바인딩 성공
3. 트랜잭션 커밋 후 발행 (멱등 skip·검증 실패 시 미발행)

## Consumer 동작 (video-service)

1. `videoUuid` 추출
2. Redis `orphan_videos:zset`에 존재 시 `ZREM` (재바인딩·복사 시 GC 대기 취소)
3. (내부) cross-service 활성 참조 카운트·상태 갱신

## uploaded 발행 시점에 video-service가 직접 처리하지 않는 이유

- `diary.video.uploaded` 시점에는 diary INSERT **전**이다. 한도·테마·멱등 검증 결과를 video-service가 알 수 없다.
- diary 검증 실패 시 video만 “바인딩됨”으로 처리하면 **유령 참조**가 생긴다.
- 지연은 Kafka 수 ms~s 수준이며, GC는 30일 지연이므로 linked 비동기는 수명주기에 무리 없다.

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | 구독 주체 명시, REST Producer 제거 |

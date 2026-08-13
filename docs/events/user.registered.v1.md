# user.registered v1

user-service에서 신규 가입 완료 시 발행하는 이벤트. diary-service는 이 이벤트를 구독하여 기본 테마(`일상`)를 자동 생성한다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `user.registered` |
| Consumer Group | `diary-service` |
| Message Key | `userUuid` (user-service 발행 기준) |
| Value format | JSON |

## Payload (user-service 발행 기준)

```json
{
  "userUuid": "01912345-6789-7abc-def0-123456789abc",
  "email": "user@example.com",
  "nickname": "플립이",
  "occurredAt": "2026-08-12T11:00:00"
}
```

| 필드 | 타입 | 필수 | diary-service 사용 |
| --- | --- | --- | --- |
| `userUuid` | string (UUIDv7) | Y | Y — 기본 테마 생성 대상 |
| `email` | string | Y | N |
| `nickname` | string | Y | N |
| `occurredAt` | string (ISO 8601) | Y | N |

> diary Consumer는 `userUuid`만 사용한다. `@JsonAlias`로 `user_uuid`(snake_case)도 수용.

## Consumer 동작 (diary-service)

1. `userUuid` 추출
2. `diary_themes`에 활성(`deleted_at IS NULL`) 기본 테마(`name = '일상'`) 존재 여부 확인
3. 없으면 INSERT, 있으면 skip (멱등)

## 발행 시점 (user-service)

- `POST /api/v1/auth/signup/local` — 이메일 가입
- `POST /api/v1/auth/login/social/{provider}` — 최초 소셜 가입

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | 최초 정의 — user-service payload(camelCase) 정합 |

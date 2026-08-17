> ## PR 제목 규칙
>
> `[#{Issue Number}] {Type} : 작업 내용`
>
> 예시
> - [#12] Feature : 로그인 API 구현
> - [#25] Fix : JWT 토큰 만료 오류 수정
> - [#31] Docs : README 수정
>
> 상세: 레포 루트 `GIT_CONVENTION.md`

---

## 작업 내용

- 작업 내용을 작성해주세요.

## 관련 이슈

- Close #

## 변경 사항

- [ ]
- [ ]
- [ ]

## 테스트

- [ ] `./gradlew test` 통과 (또는 로컬 동등 확인)
- [ ] (해당 시) 단위·통합 테스트 추가·통과
- [ ] (DDL 변경 시) `docs/sql/schema.sql` 갱신·커밋
- [ ] 수동 테스트 (아래 — 페이즈 유형에 맞는 형식)

### REST API 페이즈

**공통** — Base URL, API (메서드·경로), 헤더·파라미터, 사전 조건(시드 SQL → 확인 SELECT)

| # | 시나리오 | 입력 | 기대 결과 |
| --- | --- | --- | --- |
| 1 | | | |

시드 SQL (본문에 전문 포함)

### Kafka Consumer·Read Model 동기화 페이즈 (5-2 등)

**요약만** 작성 ([plip-diary-project.mdc §4-1-B-B](.cursor/rules/plip-diary-project.mdc)). kafka-ui 단계·Fixture·JSON·시드 SQL **전문은 PR에 넣지 않음** — 페이즈 완료 결과창 §4에 상세 출력.

**수동 테스트 요약**
- 검증 대상:
- 미검증:

| # | 시나리오 | 핵심 기대 |
| --- | --- | --- |
| 1 | | |

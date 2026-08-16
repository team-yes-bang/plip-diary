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
- [ ] 수동 테스트 (아래 시나리오)

**공통**

- Base URL:
- 헤더·파라미터:
- 사전 조건: 수동 테스트 시드 SQL 적용 (`docs/sql/seed/{phase-subtitle}.sql`)

| # | 시나리오 | Swagger 위치 | 입력 | 기대 결과 |
| --- | --- | --- | --- | --- |
| 1 | | | | |

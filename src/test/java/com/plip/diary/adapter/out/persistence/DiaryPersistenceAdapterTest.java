package com.plip.diary.adapter.out.persistence;

import com.plip.diary.adapter.out.persistence.theme.DiaryThemePersistenceAdapter;
import com.plip.diary.adapter.out.persistence.video.DiaryVideoPersistenceAdapter;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.time.KstDateTimes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DiaryPersistenceAdapterTest {

    @Autowired
    private DiaryThemePersistenceAdapter diaryThemePersistenceAdapter;

    @Autowired
    private DiaryVideoPersistenceAdapter diaryVideoPersistenceAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveAndFindTheme() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.create(userUuid, "일상", UUID.randomUUID());

        DiaryTheme saved = diaryThemePersistenceAdapter.save(theme);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getThemeUuid()).isNotNull();
        assertThat(saved.getUserUuid()).isEqualTo(userUuid);
        assertThat(saved.getName()).isEqualTo("일상");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        DiaryTheme found = diaryThemePersistenceAdapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getUserUuid()).isEqualTo(userUuid);
    }

    @Test
    void findById_excludesSoftDeleted() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme saved = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "일상", UUID.randomUUID()));
        diaryThemePersistenceAdapter.softDelete(saved.getId());

        assertThat(diaryThemePersistenceAdapter.findById(saved.getId())).isEmpty();
    }

    @Test
    void saveAndFindVideo() {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(UUID.randomUUID(), "일상", UUID.randomUUID())
        );
        UUID videoUuid = UUID.randomUUID();
        DiaryVideo video = DiaryVideo.create(theme.getId(), videoUuid);

        DiaryVideo saved = diaryVideoPersistenceAdapter.save(video);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getThemeId()).isEqualTo(theme.getId());
        assertThat(saved.getVideoUuid()).isEqualTo(videoUuid);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        DiaryVideo found = diaryVideoPersistenceAdapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getVideoUuid()).isEqualTo(videoUuid);
    }

    @Test
    void existsByThemeIdAndVideoUuid() {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(UUID.randomUUID(), "일상", UUID.randomUUID())
        );
        UUID videoUuid = UUID.randomUUID();
        diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), videoUuid));

        assertThat(diaryVideoPersistenceAdapter.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid)).isTrue();
        assertThat(diaryVideoPersistenceAdapter.existsByThemeIdAndVideoUuid(theme.getId(), UUID.randomUUID())).isFalse();
    }

    @Test
    void countTodayByUserUuid() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryVideo first = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));
        DiaryVideo second = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));

        LocalDateTime todayUtc = KstDateTimes.startOfToday().plusHours(1);
        updateCreatedAt(first.getId(), todayUtc);
        updateCreatedAt(second.getId(), todayUtc.plusMinutes(1));

        assertThat(diaryVideoPersistenceAdapter.countTodayByUserUuid(userUuid)).isEqualTo(2);
    }

    @Test
    void findDistinctWrittenDatesInMonth_groupsByKstDate() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryVideo first = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));
        DiaryVideo second = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));
        DiaryVideo third = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));

        LocalDateTime aug1Kst = KstDateTimes.startOfMonth(2026, 8).plusHours(1);
        updateCreatedAt(first.getId(), aug1Kst);
        updateCreatedAt(second.getId(), aug1Kst.plusHours(2));
        updateCreatedAt(third.getId(), KstDateTimes.startOfNextMonth(2026, 8));

        List<LocalDate> dates = diaryVideoPersistenceAdapter.findDistinctWrittenDatesInMonth(userUuid, 2026, 8);

        assertThat(dates).containsExactly(LocalDate.of(2026, 8, 1));
    }

    @Test
    void findByThemeIdAndUserUuid_excludesSoftDeletedAndOtherUsers() {
        UUID userUuid = UUID.randomUUID();
        UUID otherUserUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryTheme otherTheme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(otherUserUuid, "타인", UUID.randomUUID())
        );

        DiaryVideo active = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), UUID.randomUUID())
        );
        DiaryVideo deleted = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), UUID.randomUUID())
        );
        diaryVideoPersistenceAdapter.softDelete(deleted.getId());
        diaryVideoPersistenceAdapter.save(DiaryVideo.create(otherTheme.getId(), UUID.randomUUID()));

        List<DiaryVideo> videos = diaryVideoPersistenceAdapter.findByThemeIdAndUserUuid(theme.getId(), userUuid);

        assertThat(videos).hasSize(1);
        assertThat(videos.get(0).getId()).isEqualTo(active.getId());
    }

    private void updateCreatedAt(Long videoId, LocalDateTime createdAt) {
        jdbcTemplate.update("UPDATE diary_videos SET created_at = ? WHERE id = ?", createdAt, videoId);
    }
}

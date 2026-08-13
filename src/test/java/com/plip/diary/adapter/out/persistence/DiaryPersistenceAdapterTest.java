package com.plip.diary.adapter.out.persistence;

import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    void saveAndFindTheme() {
        UUID userUuid = UUID.randomUUID();
        DiaryTheme theme = DiaryTheme.create(userUuid, "일상");

        DiaryTheme saved = diaryThemePersistenceAdapter.save(theme);

        assertThat(saved.getThemeId()).isNotNull();
        assertThat(saved.getUserUuid()).isEqualTo(userUuid);
        assertThat(saved.getName()).isEqualTo("일상");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        DiaryTheme found = diaryThemePersistenceAdapter.findById(saved.getThemeId()).orElseThrow();
        assertThat(found.getThemeId()).isEqualTo(saved.getThemeId());
        assertThat(found.getUserUuid()).isEqualTo(userUuid);
    }

    @Test
    void saveAndFindVideo() {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(UUID.randomUUID(), "일상")
        );
        UUID videoUuid = UUID.randomUUID();
        DiaryVideo video = DiaryVideo.create(theme.getThemeId(), videoUuid);

        DiaryVideo saved = diaryVideoPersistenceAdapter.save(video);

        assertThat(saved.getDiaryVideoId()).isNotNull();
        assertThat(saved.getThemeId()).isEqualTo(theme.getThemeId());
        assertThat(saved.getVideoUuid()).isEqualTo(videoUuid);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();

        DiaryVideo found = diaryVideoPersistenceAdapter.findById(saved.getDiaryVideoId()).orElseThrow();
        assertThat(found.getDiaryVideoId()).isEqualTo(saved.getDiaryVideoId());
        assertThat(found.getVideoUuid()).isEqualTo(videoUuid);
    }
}

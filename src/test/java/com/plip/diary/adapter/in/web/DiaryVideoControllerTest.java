package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.out.persistence.theme.DiaryThemePersistenceAdapter;
import com.plip.diary.adapter.out.persistence.video.DiaryVideoPersistenceAdapter;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DiaryVideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiaryThemePersistenceAdapter diaryThemePersistenceAdapter;

    @Autowired
    private DiaryVideoPersistenceAdapter diaryVideoPersistenceAdapter;

    private UUID userUuid;

    @BeforeEach
    void setUp() {
        userUuid = UUID.randomUUID();
    }

    @Test
    void unbindDiaryVideo_softDeletesActiveRow() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryVideo saved = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), UUID.randomUUID())
        );

        mockMvc.perform(delete("/api/v1/diaries/videos/{diaryVideoId}", saved.getId())
                        .header(DiaryVideoController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isNoContent());

        assertThat(diaryVideoPersistenceAdapter.findById(saved.getId())).isEmpty();
    }

    @Test
    void unbindDiaryVideo_notFound_whenOtherUser() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryVideo saved = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), UUID.randomUUID())
        );
        UUID otherUser = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/diaries/videos/{diaryVideoId}", saved.getId())
                        .header(DiaryVideoController.USER_UUID_HEADER, otherUser))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VIDEO_001"));
    }

    @Test
    void unbindDiaryVideo_notFound_whenAlreadyDeleted() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryVideo saved = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme.getId(), UUID.randomUUID())
        );
        diaryVideoPersistenceAdapter.softDelete(saved.getId());

        mockMvc.perform(delete("/api/v1/diaries/videos/{diaryVideoId}", saved.getId())
                        .header(DiaryVideoController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VIDEO_001"));
    }
}

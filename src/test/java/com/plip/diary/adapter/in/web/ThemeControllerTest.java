package com.plip.diary.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.diary.adapter.in.web.dto.CreateThemeRequest;
import com.plip.diary.adapter.in.web.dto.UpdateThemeRequest;
import com.plip.diary.adapter.out.persistence.theme.DiaryThemePersistenceAdapter;
import com.plip.diary.adapter.out.persistence.video.DiaryVideoPersistenceAdapter;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void listThemes_returnsActiveThemesOnly() throws Exception {
        diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "일상", UUID.randomUUID()));
        diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "여행", UUID.randomUUID()));
        DiaryTheme deleted = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "삭제됨", UUID.randomUUID()));
        diaryThemePersistenceAdapter.softDelete(deleted.getId());

        mockMvc.perform(get("/api/diaries/themes")
                        .header(ThemeController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themes.length()").value(2))
                .andExpect(jsonPath("$.themes[0].name").value("일상"))
                .andExpect(jsonPath("$.themes[1].name").value("여행"));
    }

    @Test
    void listThemes_empty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/diaries/themes")
                        .header(ThemeController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themes.length()").value(0));
    }

    @Test
    void getTheme_returnsTheme() throws Exception {
        DiaryTheme saved = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "일상", UUID.randomUUID()));

        mockMvc.perform(get("/api/diaries/themes/{id}", saved.getId())
                        .header(ThemeController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("일상"));
    }

    @Test
    void getTheme_otherUser_returnsNotFound() throws Exception {
        DiaryTheme saved = diaryThemePersistenceAdapter.save(DiaryTheme.create(UUID.randomUUID(), "일상", UUID.randomUUID()));

        mockMvc.perform(get("/api/diaries/themes/{id}", saved.getId())
                        .header(ThemeController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_001"));
    }

    @Test
    void createTheme_returnsCreated() throws Exception {
        var request = new CreateThemeRequest("여행");

        mockMvc.perform(post("/api/diaries/themes")
                        .header(ThemeController.USER_UUID_HEADER, userUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.themeUuid").isNotEmpty())
                .andExpect(jsonPath("$.name").value("여행"));
    }

    @Test
    void createTheme_limitExceeded_returnsConflict() throws Exception {
        for (int i = 0; i < 5; i++) {
            diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "테마" + i, UUID.randomUUID()));
        }

        var request = new CreateThemeRequest("초과");

        mockMvc.perform(post("/api/diaries/themes")
                        .header(ThemeController.USER_UUID_HEADER, userUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("THEME_002"))
                .andExpect(jsonPath("$.message").value("테마는 최대 5개까지 생성할 수 있습니다."));
    }

    @Test
    void createTheme_duplicateName_returnsConflict() throws Exception {
        diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "여행", UUID.randomUUID()));
        var request = new CreateThemeRequest("여행");

        mockMvc.perform(post("/api/diaries/themes")
                        .header(ThemeController.USER_UUID_HEADER, userUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("THEME_004"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 테마 이름입니다."));
    }

    @Test
    void createTheme_sameNameAfterSoftDelete_returnsCreated() throws Exception {
        DiaryTheme deleted = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "여행", UUID.randomUUID()));
        diaryThemePersistenceAdapter.softDelete(deleted.getId());
        var request = new CreateThemeRequest("여행");

        mockMvc.perform(post("/api/diaries/themes")
                        .header(ThemeController.USER_UUID_HEADER, userUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("여행"));
    }

    @Test
    void updateTheme_duplicateName_returnsConflict() throws Exception {
        diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "일상", UUID.randomUUID()));
        DiaryTheme saved = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "여행", UUID.randomUUID()));
        var request = new UpdateThemeRequest("일상");

        mockMvc.perform(patch("/api/diaries/themes/{id}", saved.getId())
                        .header(ThemeController.USER_UUID_HEADER, userUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("THEME_004"));
    }

    @Test
    void createTheme_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/diaries/themes")
                        .header(ThemeController.USER_UUID_HEADER, userUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }

    @Test
    void updateTheme_returnsOk() throws Exception {
        DiaryTheme saved = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "일상", UUID.randomUUID()));
        var request = new UpdateThemeRequest("여행");

        mockMvc.perform(patch("/api/diaries/themes/{id}", saved.getId())
                        .header(ThemeController.USER_UUID_HEADER, userUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("여행"));
    }

    @Test
    void updateTheme_otherUser_returnsNotFound() throws Exception {
        DiaryTheme saved = diaryThemePersistenceAdapter.save(DiaryTheme.create(UUID.randomUUID(), "일상", UUID.randomUUID()));
        var request = new UpdateThemeRequest("여행");

        mockMvc.perform(patch("/api/diaries/themes/{id}", saved.getId())
                        .header(ThemeController.USER_UUID_HEADER, userUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_001"));
    }

    @Test
    void deleteTheme_lastRemaining_returnsConflict() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "일상", UUID.randomUUID()));

        mockMvc.perform(delete("/api/diaries/themes/{id}", theme.getId())
                        .header(ThemeController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("THEME_003"))
                .andExpect(jsonPath("$.message").value("마지막 남은 테마는 삭제할 수 없습니다."));
    }

    @Test
    void deleteTheme_softDeletesVideos() throws Exception {
        DiaryTheme theme1 = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "일상", UUID.randomUUID()));
        DiaryTheme theme2 = diaryThemePersistenceAdapter.save(DiaryTheme.create(userUuid, "여행", UUID.randomUUID()));
        DiaryVideo video = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(theme1.getId(), UUID.randomUUID())
        );

        mockMvc.perform(delete("/api/diaries/themes/{id}", theme1.getId())
                        .header(ThemeController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isNoContent());

        assertThat(diaryThemePersistenceAdapter.findById(theme1.getId())).isEmpty();
        assertThat(diaryVideoPersistenceAdapter.findById(video.getId())).isEmpty();

        mockMvc.perform(get("/api/diaries/themes/{id}", theme1.getId())
                        .header(ThemeController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_001"));

        mockMvc.perform(get("/api/diaries/themes")
                        .header(ThemeController.USER_UUID_HEADER, userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themes.length()").value(1))
                .andExpect(jsonPath("$.themes[0].id").value(theme2.getId()));
    }
}

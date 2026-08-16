package com.plip.diary.adapter.in.web;

import com.plip.diary.adapter.out.persistence.theme.DiaryThemePersistenceAdapter;
import com.plip.diary.adapter.out.persistence.video.DiaryVideoPersistenceAdapter;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.time.KstDateTimes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiaryThemePersistenceAdapter diaryThemePersistenceAdapter;

    @Autowired
    private DiaryVideoPersistenceAdapter diaryVideoPersistenceAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID userUuid;

    @BeforeEach
    void setUp() {
        userUuid = UUID.randomUUID();
    }

    @Test
    void getCalendar_returnsDistinctWrittenDatesInMonth() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryVideo first = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));
        DiaryVideo second = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));

        LocalDateTime aug1Kst = KstDateTimes.startOfMonth(2026, 8);
        LocalDateTime aug15Kst = aug1Kst.plusDays(14).plusHours(12);
        updateCreatedAt(first.getId(), aug1Kst.plusHours(1));
        updateCreatedAt(second.getId(), aug15Kst);

        mockMvc.perform(get("/api/diaries/calendar")
                        .header(CalendarController.USER_UUID_HEADER, userUuid)
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(8))
                .andExpect(jsonPath("$.dates.length()").value(2))
                .andExpect(jsonPath("$.dates[0]").value("2026-08-01"))
                .andExpect(jsonPath("$.dates[1]").value("2026-08-15"));
    }

    @Test
    void getCalendar_excludesSoftDeletedVideosAndThemes() throws Exception {
        DiaryTheme activeTheme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryTheme deletedTheme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "삭제됨", UUID.randomUUID())
        );
        diaryVideoPersistenceAdapter.save(DiaryVideo.create(deletedTheme.getId(), UUID.randomUUID()));
        diaryThemePersistenceAdapter.softDelete(deletedTheme.getId());

        DiaryVideo active = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(activeTheme.getId(), UUID.randomUUID())
        );
        DiaryVideo deleted = diaryVideoPersistenceAdapter.save(
                DiaryVideo.create(activeTheme.getId(), UUID.randomUUID())
        );
        diaryVideoPersistenceAdapter.softDelete(deleted.getId());

        LocalDateTime aug1Kst = KstDateTimes.startOfMonth(2026, 8).plusHours(2);
        updateCreatedAt(active.getId(), aug1Kst);

        mockMvc.perform(get("/api/diaries/calendar")
                        .header(CalendarController.USER_UUID_HEADER, userUuid)
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dates.length()").value(1))
                .andExpect(jsonPath("$.dates[0]").value("2026-08-01"));
    }

    @Test
    void getCalendar_emptyWhenNoVideosInMonth() throws Exception {
        mockMvc.perform(get("/api/diaries/calendar")
                        .header(CalendarController.USER_UUID_HEADER, userUuid)
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dates.length()").value(0));
    }

    @Test
    void getCalendar_invalidMonth_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/diaries/calendar")
                        .header(CalendarController.USER_UUID_HEADER, userUuid)
                        .param("year", "2026")
                        .param("month", "13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }

    @Test
    void getCalendar_doesNotReturnOtherUserDates() throws Exception {
        DiaryTheme theme = diaryThemePersistenceAdapter.save(
                DiaryTheme.create(userUuid, "일상", UUID.randomUUID())
        );
        DiaryVideo video = diaryVideoPersistenceAdapter.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));
        updateCreatedAt(video.getId(), KstDateTimes.startOfMonth(2026, 8).plusHours(3));

        UUID otherUser = UUID.randomUUID();
        mockMvc.perform(get("/api/diaries/calendar")
                        .header(CalendarController.USER_UUID_HEADER, otherUser)
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dates.length()").value(0));
    }

    private void updateCreatedAt(Long videoId, LocalDateTime createdAt) {
        jdbcTemplate.update("UPDATE diary_videos SET created_at = ? WHERE id = ?", createdAt, videoId);
    }
}

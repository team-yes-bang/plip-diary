package com.plip.diary.application.service;

import com.plip.diary.global.exception.ErrorCode;
import com.plip.diary.global.exception.ThemeLastRemainingException;
import com.plip.diary.global.exception.ThemeLimitExceededException;
import com.plip.diary.global.exception.ThemeNameDuplicateException;
import com.plip.diary.global.exception.ThemeNotFoundException;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.UuidGeneratorPort;
import com.plip.diary.domain.model.DiaryTheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Mock
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Mock
    private UuidGeneratorPort uuidGeneratorPort;

    @InjectMocks
    private ThemeService themeService;

    private final UUID userUuid = UUID.randomUUID();

    @Test
    void listThemes_returnsThemes() {
        DiaryTheme theme = DiaryTheme.reconstitute(1L, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.now(), LocalDateTime.now(), null);
        when(diaryThemePersistencePort.findAllByUserUuid(userUuid)).thenReturn(List.of(theme));

        List<DiaryTheme> themes = themeService.listThemes(userUuid);

        assertThat(themes).hasSize(1);
        assertThat(themes.get(0).getName()).isEqualTo("일상");
    }

    @Test
    void getTheme_success() {
        Long id = 1L;
        DiaryTheme theme = DiaryTheme.reconstitute(id, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.now(), LocalDateTime.now(), null);
        when(diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid))
                .thenReturn(Optional.of(theme));

        DiaryTheme found = themeService.getTheme(userUuid, id);

        assertThat(found.getId()).isEqualTo(id);
    }

    @Test
    void getTheme_notFound() {
        Long id = 99L;
        when(diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.getTheme(userUuid, id))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void createTheme_duplicateName() {
        when(diaryThemePersistencePort.countByUserUuid(userUuid)).thenReturn(1L);
        when(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "여행")).thenReturn(true);

        assertThatThrownBy(() -> themeService.createTheme(userUuid, "여행"))
                .isInstanceOf(ThemeNameDuplicateException.class);

        verify(diaryThemePersistencePort, never()).save(any());
    }

    @Test
    void updateTheme_duplicateName() {
        Long id = 2L;
        DiaryTheme existing = DiaryTheme.reconstitute(id, UUID.randomUUID(), userUuid, "여행",
                LocalDateTime.now(), LocalDateTime.now(), null);
        when(diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid))
                .thenReturn(Optional.of(existing));
        when(diaryThemePersistencePort.existsByUserUuidAndNameExcludingId(userUuid, "일상", id))
                .thenReturn(true);

        assertThatThrownBy(() -> themeService.updateTheme(userUuid, id, "일상"))
                .isInstanceOf(ThemeNameDuplicateException.class);

        verify(diaryThemePersistencePort, never()).save(any());
    }

    @Test
    void createTheme_success() {
        UUID themeUuid = UUID.randomUUID();
        when(diaryThemePersistencePort.countByUserUuid(userUuid)).thenReturn(2L);
        when(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "여행")).thenReturn(false);
        when(uuidGeneratorPort.generate()).thenReturn(themeUuid);
        when(diaryThemePersistencePort.save(any(DiaryTheme.class))).thenAnswer(invocation -> {
            DiaryTheme theme = invocation.getArgument(0);
            return DiaryTheme.reconstitute(10L, theme.getThemeUuid(), theme.getUserUuid(), theme.getName(),
                    LocalDateTime.now(), LocalDateTime.now(), null);
        });

        DiaryTheme created = themeService.createTheme(userUuid, "여행");

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getName()).isEqualTo("여행");
    }

    @Test
    void createTheme_limitExceeded() {
        when(diaryThemePersistencePort.countByUserUuid(userUuid))
                .thenReturn((long) ErrorCode.MAX_ACTIVE_THEMES);

        assertThatThrownBy(() -> themeService.createTheme(userUuid, "여행"))
                .isInstanceOf(ThemeLimitExceededException.class);

        verify(diaryThemePersistencePort, never()).save(any());
    }

    @Test
    void updateTheme_success() {
        Long id = 1L;
        DiaryTheme existing = DiaryTheme.reconstitute(id, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.now(), LocalDateTime.now(), null);
        when(diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid))
                .thenReturn(Optional.of(existing));
        when(diaryThemePersistencePort.existsByUserUuidAndNameExcludingId(userUuid, "여행", id))
                .thenReturn(false);
        when(diaryThemePersistencePort.save(any(DiaryTheme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiaryTheme updated = themeService.updateTheme(userUuid, id, "여행");

        assertThat(updated.getName()).isEqualTo("여행");
    }

    @Test
    void updateTheme_notFound() {
        Long id = 99L;
        when(diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.updateTheme(userUuid, id, "여행"))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void deleteTheme_lastRemaining_throws() {
        Long id = 1L;
        DiaryTheme existing = DiaryTheme.reconstitute(id, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.now(), LocalDateTime.now(), null);
        when(diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid))
                .thenReturn(Optional.of(existing));
        when(diaryThemePersistencePort.countByUserUuid(userUuid)).thenReturn(1L);

        assertThatThrownBy(() -> themeService.deleteTheme(userUuid, id))
                .isInstanceOf(ThemeLastRemainingException.class);

        verify(diaryThemePersistencePort, never()).softDelete(id);
        verify(diaryVideoPersistencePort, never()).softDeleteAllByThemeId(id);
    }

    @Test
    void deleteTheme_success() {
        Long id = 1L;
        DiaryTheme existing = DiaryTheme.reconstitute(id, UUID.randomUUID(), userUuid, "일상",
                LocalDateTime.now(), LocalDateTime.now(), null);
        when(diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid))
                .thenReturn(Optional.of(existing));
        when(diaryThemePersistencePort.countByUserUuid(userUuid)).thenReturn(2L);

        themeService.deleteTheme(userUuid, id);

        verify(diaryVideoPersistencePort).softDeleteAllByThemeId(id);
        verify(diaryThemePersistencePort).softDelete(id);
    }

    @Test
    void deleteTheme_notFound() {
        Long id = 99L;
        when(diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.deleteTheme(userUuid, id))
                .isInstanceOf(ThemeNotFoundException.class);

        verify(diaryThemePersistencePort, never()).softDelete(id);
        verify(diaryVideoPersistencePort, never()).softDeleteAllByThemeId(id);
    }
}

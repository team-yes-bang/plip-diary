package com.plip.diary.application.service;

import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.UuidGeneratorPort;
import com.plip.diary.domain.model.DiaryTheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDefaultThemeServiceTest {

    @Mock
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Mock
    private UuidGeneratorPort uuidGeneratorPort;

    @InjectMocks
    private CreateDefaultThemeService createDefaultThemeService;

    @Test
    void createDefaultTheme_savesWhenNotExists() {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        when(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, CreateDefaultThemeService.DEFAULT_THEME_NAME))
                .thenReturn(false);
        when(uuidGeneratorPort.generate()).thenReturn(themeUuid);

        createDefaultThemeService.createDefaultTheme(userUuid);

        ArgumentCaptor<DiaryTheme> captor = ArgumentCaptor.forClass(DiaryTheme.class);
        verify(diaryThemePersistencePort).save(captor.capture());
        DiaryTheme saved = captor.getValue();
        assertThat(saved.getThemeUuid()).isEqualTo(themeUuid);
        assertThat(saved.getUserUuid()).isEqualTo(userUuid);
        assertThat(saved.getName()).isEqualTo(CreateDefaultThemeService.DEFAULT_THEME_NAME);
    }

    @Test
    void createDefaultTheme_skipsWhenAlreadyExists() {
        UUID userUuid = UUID.randomUUID();
        when(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, CreateDefaultThemeService.DEFAULT_THEME_NAME))
                .thenReturn(true);

        createDefaultThemeService.createDefaultTheme(userUuid);

        verify(diaryThemePersistencePort, never()).save(any());
        verify(diaryThemePersistencePort).existsByUserUuidAndName(
                eq(userUuid),
                eq(CreateDefaultThemeService.DEFAULT_THEME_NAME)
        );
    }
}

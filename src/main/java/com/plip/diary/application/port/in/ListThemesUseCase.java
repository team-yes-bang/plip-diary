package com.plip.diary.application.port.in;

import com.plip.diary.domain.model.DiaryTheme;

import java.util.List;
import java.util.UUID;

public interface ListThemesUseCase {

    List<DiaryTheme> listThemes(UUID userUuid);
}

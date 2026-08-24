package com.plip.diary.application.port.in.dto;

import com.plip.diary.domain.model.DiaryTheme;

import java.util.List;

public record HomeFeed(
        List<HomeFeedSection> sections,
        List<DiaryTheme> themes
) {
}

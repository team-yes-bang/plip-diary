package com.plip.diary.application.port.out;

import com.plip.diary.domain.model.DiaryVideo;

import java.util.Optional;

public interface DiaryVideoPersistencePort {

    DiaryVideo save(DiaryVideo video);

    Optional<DiaryVideo> findById(Long diaryVideoId);
}

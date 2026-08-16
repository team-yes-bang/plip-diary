package com.plip.diary.application.port.in;

import com.plip.diary.application.port.in.dto.HomeFeed;

import java.util.UUID;

public interface GetHomeFeedUseCase {

    HomeFeed getHomeFeed(UUID userUuid);
}

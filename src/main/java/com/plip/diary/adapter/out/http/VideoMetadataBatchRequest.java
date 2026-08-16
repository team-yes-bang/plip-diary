package com.plip.diary.adapter.out.http;

import java.util.List;
import java.util.UUID;

record VideoMetadataBatchRequest(List<UUID> videoUuids) {
}

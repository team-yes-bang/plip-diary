package com.plip.diary.adapter.out.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface DiaryVideoMetadataMongoRepository extends MongoRepository<DiaryVideoMetadataDocument, UUID> {

    List<DiaryVideoMetadataDocument> findByUserUuidAndVideoUuidIn(UUID userUuid, Collection<UUID> videoUuids);
}

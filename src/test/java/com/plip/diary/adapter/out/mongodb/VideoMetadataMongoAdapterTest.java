package com.plip.diary.adapter.out.mongodb;

import com.plip.diary.application.port.out.VideoMetadataProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoMetadataMongoAdapterTest {

    @Mock
    private DiaryVideoMetadataMongoRepository diaryVideoMetadataMongoRepository;

    @InjectMocks
    private VideoMetadataMongoAdapter adapter;

    @Test
    void findByUserUuidAndVideoUuids_returnsEmptyForBlankInput() {
        assertThat(adapter.findByUserUuidAndVideoUuids(UUID.randomUUID(), List.of())).isEmpty();
        assertThat(adapter.findByUserUuidAndVideoUuids(UUID.randomUUID(), null)).isEmpty();
    }

    @Test
    void findByUserUuidAndVideoUuids_mapsDocuments() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryVideoMetadataDocument document = new DiaryVideoMetadataDocument();
        document.setVideoUuid(videoUuid);
        document.setUserUuid(userUuid);
        document.setCaption("캡션");
        document.setThumbnailUrl("https://cdn/thumb.jpg");
        document.setUpdatedAt(LocalDateTime.of(2026, 8, 16, 7, 30));

        when(diaryVideoMetadataMongoRepository.findByUserUuidAndVideoUuidIn(userUuid, List.of(videoUuid)))
                .thenReturn(List.of(document));

        List<VideoMetadataProjection> result = adapter.findByUserUuidAndVideoUuids(userUuid, List.of(videoUuid));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).videoUuid()).isEqualTo(videoUuid);
        assertThat(result.get(0).caption()).isEqualTo("캡션");
    }

    @Test
    void upsert_savesDocument() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        VideoMetadataProjection projection = new VideoMetadataProjection(
                videoUuid,
                userUuid,
                "캡션",
                "https://cdn/thumb.jpg",
                LocalDateTime.of(2026, 8, 16, 7, 30)
        );

        when(diaryVideoMetadataMongoRepository.findById(videoUuid)).thenReturn(Optional.empty());

        adapter.upsert(projection);

        ArgumentCaptor<DiaryVideoMetadataDocument> captor = ArgumentCaptor.forClass(DiaryVideoMetadataDocument.class);
        verify(diaryVideoMetadataMongoRepository).save(captor.capture());
        DiaryVideoMetadataDocument saved = captor.getValue();
        assertThat(saved.getVideoUuid()).isEqualTo(videoUuid);
        assertThat(saved.getUserUuid()).isEqualTo(userUuid);
        assertThat(saved.getCaption()).isEqualTo("캡션");
    }

    @Test
    void deleteByVideoUuid_deletesWhenUserMatches() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryVideoMetadataDocument document = new DiaryVideoMetadataDocument();
        document.setVideoUuid(videoUuid);
        document.setUserUuid(userUuid);

        when(diaryVideoMetadataMongoRepository.findById(videoUuid)).thenReturn(Optional.of(document));

        adapter.deleteByVideoUuid(userUuid, videoUuid);

        verify(diaryVideoMetadataMongoRepository).delete(document);
    }

    @Test
    void deleteByVideoUuid_skipsWhenUserMismatch() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryVideoMetadataDocument document = new DiaryVideoMetadataDocument();
        document.setVideoUuid(videoUuid);
        document.setUserUuid(UUID.randomUUID());

        when(diaryVideoMetadataMongoRepository.findById(videoUuid)).thenReturn(Optional.of(document));

        adapter.deleteByVideoUuid(userUuid, videoUuid);

        verify(diaryVideoMetadataMongoRepository, never()).delete(any());
    }
}

package com.plip.diary.adapter.out.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceHttpAdapterTest {

    @Mock
    private RestClient videoServiceRestClient;

    @InjectMocks
    private VideoServiceHttpAdapter videoServiceHttpAdapter;

    @Test
    void fetchVideoMetadata_returnsEmptyWhenLoadBalancerFails() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();

        when(videoServiceRestClient.post()).thenThrow(new IllegalStateException("No instances available for video"));

        Map<UUID, com.plip.diary.application.port.out.VideoMetadata> result =
                videoServiceHttpAdapter.fetchVideoMetadata(userUuid, List.of(videoUuid));

        assertThat(result).isEmpty();
    }

    @Test
    void fetchVideoMetadata_returnsEmptyForEmptyInput() {
        assertThat(videoServiceHttpAdapter.fetchVideoMetadata(UUID.randomUUID(), List.of())).isEmpty();
    }
}

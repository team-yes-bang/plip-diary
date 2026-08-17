package com.plip.diary;

import com.plip.diary.adapter.out.mongodb.VideoMetadataMongoAdapter;
import com.plip.diary.adapter.out.redis.VideoMetadataRedisAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class DiaryApplicationTests {

    @MockitoBean
    private VideoMetadataMongoAdapter videoMetadataMongoAdapter;

    @MockitoBean
    private VideoMetadataRedisAdapter videoMetadataRedisAdapter;

    @Test
    void contextLoads() {
    }
}

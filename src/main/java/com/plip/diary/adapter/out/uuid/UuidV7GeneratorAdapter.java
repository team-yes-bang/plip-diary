package com.plip.diary.adapter.out.uuid;

import com.github.f4b6a3.uuid.UuidCreator;
import com.plip.diary.application.port.out.UuidGeneratorPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidV7GeneratorAdapter implements UuidGeneratorPort {

    @Override
    public UUID generate() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}

package com.carddemo.parity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads a {@link ParitySummary} from JSON conforming to the frozen contract. */
public final class ParitySummaryReader {

    private final ObjectMapper mapper;

    public ParitySummaryReader() {
        this.mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public ParitySummary read(Path json) throws IOException {
        try (InputStream in = Files.newInputStream(json)) {
            return read(in);
        }
    }

    public ParitySummary read(InputStream json) throws IOException {
        return mapper.readValue(json, ParitySummary.class);
    }

    public ParitySummary read(String json) throws IOException {
        return mapper.readValue(json, ParitySummary.class);
    }
}

package com.rapid7.insightappsec.intg.jenkins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

public class MappingConfiguration {

    public static final ObjectMapper OBJECT_MAPPER_INSTANCE = createObjectMapper();

    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        return mapper;
    }
}

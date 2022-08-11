package com.bitfye.common.client.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    public static final ObjectMapper objectMapper = createObjectMapper();

    static ObjectMapper createObjectMapper() {
        return createObjectMapper(PropertyNamingStrategy.KEBAB_CASE);
    }

    public static ObjectMapper createObjectMapperUsingKebabCase() {
        return createObjectMapper(PropertyNamingStrategy.KEBAB_CASE);
    }

    public static ObjectMapper createObjectMapperUsingLowerCamelCase() {
        return createObjectMapper(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    }

    public static ObjectMapper createObjectMapperUsingLowerCamelCaseWithoutNull() {
        return createObjectMapperWithoutNull(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    }

    public static ObjectMapper createObjectMapper(PropertyNamingStrategy strategy) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(strategy);
        mapper.setSerializationInclusion(Include.ALWAYS);
        // disabled features:
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    static ObjectMapper createObjectMapperWithoutNull(PropertyNamingStrategy strategy) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(strategy);
        mapper.setSerializationInclusion(Include.NON_NULL);
        // disabled features:
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static String writeValue(ObjectMapper mapper, Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(ObjectMapper mapper, String str, Class<T> clazz) {
        try {
            return mapper.readValue(str, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeValue(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String str, Class<T> clazz) {
        try {
            return objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String str, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(str, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJsonString(Object object) {

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("jackson toJsonString error,cause: {}", e.getMessage(), e);
        }

        return null;
    }

    public static Map<String, Object> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }
}

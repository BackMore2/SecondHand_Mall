package com.backmore.secondhand_mall.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象转换为JSON字符串
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Convert object to json failed", e);
            return "[]";
        }
    }

    /**
     * 将JSON字符串转换为List
     */
    public static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            logger.error("Parse json to list failed", e);
            return new ArrayList<>();
        }
    }

    /**
     * 将JSON字符串转换为对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (json == null || json.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("Parse json to object failed", e);
            return null;
        }
    }

    /**
     * 将图片列表转换为JSON字符串
     */
    public static String imagesToJson(List<String> images) {
        return toJson(images);
    }

    /**
     * 将JSON字符串转换为图片列表
     */
    public static List<String> jsonToImages(String json) {
        return fromJson(json, new TypeReference<List<String>>() {});
    }
} 
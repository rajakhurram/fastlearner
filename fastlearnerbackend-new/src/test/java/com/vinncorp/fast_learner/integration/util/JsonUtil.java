package com.vinncorp.fast_learner.integration.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

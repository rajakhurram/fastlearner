package com.vinncorp.fast_learner.integration;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TokenUtils {

    public static String JWT_TOKEN;
    public static String EMAIL = "instructor@mailinator.com";

    public static String getToken(MockMvc mockMvc) throws Exception {
        if (JWT_TOKEN != null) {
            System.out.println("-----------------TOKEN ALREADY EXIST-----------------");
            return JWT_TOKEN;
        }
        System.out.println("-----------------FETCHING TOKEN FROM API CALL-----------------");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/local-login")
                        .header("Origin", "fastlearner.ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"pakista@mailinator.com\", \"password\": \"abc123\"}"))  //123456
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(responseString);
        JWT_TOKEN = jsonObject.getString("token");
        return JWT_TOKEN;
    }

    public static String getRefreshToken(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/local-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"pakista@mailinator.com\", \"password\": \"abc123\"}"))  //123456
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(responseString);
        return jsonObject.getString("refreshToken");
    }
}

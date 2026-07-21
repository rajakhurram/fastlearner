package com.vinncorp.fast_learner.services.open_ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.request.quiz.QuizQuestionReport;
import com.vinncorp.fast_learner.response.quiz.QuizReportResult;
import dev.ai4j.openai4j.chat.ChatCompletionRequest;
import dev.langchain4j.data.message.ChatMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService implements IOpenAIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIService(@Value("${openai.api.key-value}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey) // ✅ apiKey available
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    public Boolean validateAnswer(String question,String instructorAnswer, String studentAnswer) {
        String prompt = "You are an intelligent answer evaluator.\n\n" +
                "Question: " + question + "\n\n" +
                "Instructor's Answer: " + instructorAnswer + "\n\n" +
                "Student's Answer: " + studentAnswer + "\n\n" +
                "Task:\n" +
                "Compare the student's answer with the instructor's answer based on meaning, intent, and context.\n" +
                "Do not rely on exact wording.\n" +
                "If the student's answer conveys the same meaning or is sufficiently correct, return true.\n" +
                "If it is incorrect, irrelevant, or missing key meaning, return false.\n\n" +
                "Strictly reply with only one word: true or false.";

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                },
                "max_tokens", 5
        );

        Map response = this.webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String content = (String) ((Map)((Map)((java.util.List)response.get("choices")).get(0))
                .get("message")).get("content");

        return content.trim().toLowerCase().contains("true");
    }


    public QuizReportResult fetchQuizTopicReport(List<QuizQuestionReport> quizQuestionReports) {
        try {
            int totalQuestions = quizQuestionReports.size();
            long correctAnswers = quizQuestionReports.stream()
                    .filter(QuizQuestionReport::getIsStudentAnswerCorrect)
                    .count();
            int score = (int) correctAnswers * 10;

            // Prepare minimal quiz data (without answers correctness evaluation, since it's already known)
            Map<String, Object> quizData = new HashMap<>();
            quizData.put("quizQuestions", quizQuestionReports);

            String prompt = "You are an intelligent report generator for a student quiz system. " +
                    "You are given quiz data with student answers and correctness already provided in the field `isStudentAnswerCorrect`. " +
                    "DO NOT check or validate answers yourself. " +
                    "Each correct answer is worth 10 marks. The following values are already calculated:\n" +
                    "- totalQuestions: " + totalQuestions + "\n" +
                    "- correctAnswers: " + correctAnswers + "\n" +
                    "- score: " + score + "\n\n" +
                    "Now, based on the quiz data, generate a professional and visually appealing HTML report. " +
                    "This report should be formatted with semantic HTML tags (<h1>, <h2>, <p>, <ul>, <li>, etc.), " +
                    "and should include the following sections:\n\n" +
                    "1. <h1>Quiz Performance Report</h1>\n" +
                    "2. <h2>Summary</h2> - Show total questions, correct answers, score, and percentage.\n" +
                    "3. <h2>Weaknesses</h2> - A bulleted list of student weaknesses based on incorrect answers.\n" +
                    "4. <h2>Recommendations</h2> - A bulleted list of practical advice to improve.\n" +
                    "5. <h2>Practice Suggestions</h2> - Specific study or practice actions to enhance skills.\n" +
                    "6. <h2>Final Thoughts</h2> - A short reflection or analysis of the student’s performance.\n" +
                    "7. <h2>Motivation</h2> - A few encouraging sentences to boost confidence.\n" +
                    "8. <h2>Feedback</h2> - A short paragraph summarizing overall performance quality.\n\n" +
                    "Return ONLY valid HTML (without markdown, JSON, or explanations).";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");
            requestBody.put("input", prompt);

            String rawResponse = webClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(rawResponse);
            String resultHtml = root.path("output").get(0).path("content").get(0).path("text").asText();

            QuizReportResult gptResponse = new QuizReportResult();
            // Merge local calculations + GPT insights
            gptResponse.setHtmlReport(resultHtml); // ✅ assuming you add this new field in QuizReportResult
            gptResponse.setTotalQuestions(totalQuestions);
            gptResponse.setCorrectAnswers((int) correctAnswers);
            gptResponse.setScore(score);
            return gptResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch quiz report", e);
        }
    }

    String extractJson(String text) {
        // If response contains ```json ... ```
        if (text.contains("```")) {
            int start = text.indexOf("```");
            int end = text.lastIndexOf("```");
            if (start != -1 && end != -1 && end > start) {
                text = text.substring(start + 3, end);
            }
        }
        // Remove leading `json` if present
        return text.replaceFirst("^json", "").trim();
    }


}

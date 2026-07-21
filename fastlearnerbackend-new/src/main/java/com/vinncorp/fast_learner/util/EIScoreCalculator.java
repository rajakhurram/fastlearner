package com.vinncorp.fast_learner.util;

import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EIScoreCalculator {
    private final Map<String, Integer> optionsScore = Map.ofEntries(
            Map.entry("Does not apply at all", 1),
            Map.entry("Applies rarely (less than half the time)", 2),
            Map.entry("Applies half the time", 3),
            Map.entry("The statement applies often (more than half the time)", 4),
            Map.entry("Always applies", 5)
    );

    // Map question text to category and question number (1-50)
    private final Map<Integer, String> questionNumberToCategory = new HashMap<>();

    public EIScoreCalculator() {
        // Self-Awareness questions (positions based on formula)
        for (int q : new int[]{1, 6, 11, 16, 21, 26, 31, 36, 41, 46}) {
            questionNumberToCategory.put(q, "SA");
        }
        // Managing Emotions questions
        for (int q : new int[]{2, 7, 12, 17, 22, 27, 32, 37, 42, 47}) {
            questionNumberToCategory.put(q, "ME");
        }
        // Motivating Oneself questions
        for (int q : new int[]{3, 8, 13, 18, 23, 28, 33, 38, 43, 48}) {
            questionNumberToCategory.put(q, "MO");
        }
        // Empathy questions
        for (int q : new int[]{4, 9, 14, 19, 24, 29, 34, 39, 44, 49}) {
            questionNumberToCategory.put(q, "E");
        }
        // Social Skills questions
        for (int q : new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50}) {
            questionNumberToCategory.put(q, "SS");
        }
    }

    public String calculateScore(List<ValidationAnswerRequest> validationAnswerList) {
        int selfAwarenessTotal = 0;
        int managingEmotionsTotal = 0;
        int motivatingOneselfTotal = 0;
        int empathyTotal = 0;
        int socialSkillTotal = 0;

        // Calculate totals for each category
        // Questions are in order 1-50 in the list
        for (int i = 0; i < validationAnswerList.size(); i++) {
            ValidationAnswerRequest answer = validationAnswerList.get(i);
            int questionNumber = i + 1; // Questions are numbered 1-50, not 0-49
            String answerText = answer.getAnswerText();
            Integer score = optionsScore.getOrDefault(answerText, 0);

            String category = questionNumberToCategory.get(questionNumber);
            if (category == null) continue;

            switch (category) {
                case "SA":
                    selfAwarenessTotal += score;
                    break;
                case "ME":
                    managingEmotionsTotal += score;
                    break;
                case "MO":
                    motivatingOneselfTotal += score;
                    break;
                case "E":
                    empathyTotal += score;
                    break;
                case "SS":
                    socialSkillTotal += score;
                    break;
            }
        }

        // Calculate overall EI score
        int overallEIScore = selfAwarenessTotal + managingEmotionsTotal +
                motivatingOneselfTotal + empathyTotal + socialSkillTotal;

        // Build the scores output
        StringBuilder scores = new StringBuilder("\n### Emotional Intelligence Assessment Scores\n---\n");
        scores.append(String.format("**Self-awareness (SA):** %d/50 - %s\n",
                selfAwarenessTotal, getCategoryLevel(selfAwarenessTotal)));
        scores.append(String.format("**Managing emotions (ME):** %d/50 - %s\n",
                managingEmotionsTotal, getCategoryLevel(managingEmotionsTotal)));
        scores.append(String.format("**Motivating oneself (MO):** %d/50 - %s\n",
                motivatingOneselfTotal, getCategoryLevel(motivatingOneselfTotal)));
        scores.append(String.format("**Empathy (E):** %d/50 - %s\n",
                empathyTotal, getCategoryLevel(empathyTotal)));
        scores.append(String.format("**Social Skill (SS):** %d/50 - %s\n\n",
                socialSkillTotal, getCategoryLevel(socialSkillTotal)));
        scores.append(String.format("**Overall EI Score:** %d/250\n", overallEIScore));

        return scores.toString();
    }

    private String getCategoryLevel(int score) {
        if (score >= 40) return "High";
        if (score >= 30) return "Average";
        return "Low";
    }
}
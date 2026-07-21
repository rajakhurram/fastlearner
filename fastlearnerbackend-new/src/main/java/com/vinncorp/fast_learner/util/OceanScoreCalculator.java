package com.vinncorp.fast_learner.util;

import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;

@Component
public class OceanScoreCalculator {

    private final Map<String, Integer> optionsScore = Map.ofEntries(
            Map.entry("Disagree", 1),
            Map.entry("Slightly disagree", 2),
            Map.entry("Neutral", 3),
            Map.entry("Slightly agree", 4),
            Map.entry("Agree", 5)
    );

    public String calculateScore(List<ValidationAnswerRequest> validationAnswerList) {
        StringBuilder scores = new StringBuilder("\n### Scores \n---\n");

        // Verify we have exactly 50 answers
        if (validationAnswerList == null || validationAnswerList.size() != 50) {
            throw new IllegalArgumentException("OCEAN test requires exactly 50 answers");
        }

        // Convert all responses to Scored Points (SP)
        int[] sp = new int[51]; // 1-indexed array (0 will be unused)

        for (int i = 0; i < validationAnswerList.size(); i++) {
            ValidationAnswerRequest answer = validationAnswerList.get(i);
            int questionNumber = i + 1;

            // Get Response Value (RV) from answerText
            String answerText = answer.getAnswerText();
            if (answerText == null || !optionsScore.containsKey(answerText)) {
                throw new IllegalArgumentException(
                        "Invalid answer text for question " + questionNumber + ": " + answerText
                );
            }

            int rv = optionsScore.get(answerText);

            // Determine if this is a reverse question
            boolean isReverse = isReverseQuestion(questionNumber);

            // Calculate SP (Scored Point)
            if (isReverse) {
                sp[questionNumber] = 6 - rv; // Reverse transformation
            } else {
                sp[questionNumber] = rv; // Standard transformation
            }
        }

        // Calculate each trait using the EXACT formulas
        int E = 20 + sp[1] - sp[6] + sp[11] - sp[16] + sp[21] - sp[26] + sp[31] - sp[36] + sp[41] - sp[46];
        int A = 14 - sp[2] + sp[7] - sp[12] + sp[17] - sp[22] + sp[27] - sp[32] + sp[37] + sp[42] + sp[47];
        int C = 14 + sp[3] - sp[8] + sp[13] - sp[18] + sp[23] - sp[28] + sp[33] - sp[38] + sp[43] + sp[48];
        int N = 38 - sp[4] + sp[9] - sp[14] + sp[19] - sp[24] - sp[29] - sp[34] - sp[39] - sp[44] - sp[49];
        int O = 8 + sp[5] - sp[10] + sp[15] - sp[20] + sp[25] - sp[30] + sp[35] + sp[40] + sp[45] + sp[50];

        // Format the results
        scores.append("**Extraversion (E):** ").append(E).append("\n");
        scores.append("**Agreeableness (A):** ").append(A).append("\n");
        scores.append("**Conscientiousness (C):** ").append(C).append("\n");
        scores.append("**Neuroticism (N):** ").append(N).append("\n");
        scores.append("**Openness (O):** ").append(O).append("\n");

        return scores.toString();
    }

    private boolean isReverseQuestion(int questionNumber) {
        // Based on the OCEAN test pattern, questions with (-) sign are reverse scored
        // Reverse questions list from the scoring formula
        int[] reverseQuestions = {
                // Extraversion: Q6, Q16, Q26, Q36, Q46
                6, 16, 26, 36, 46,
                // Agreeableness: Q2, Q12, Q22, Q32
                2, 12, 22, 32,
                // Conscientiousness: Q8, Q18, Q28, Q38
                8, 18, 28, 38,
                // Neuroticism: Q4, Q14, Q24, Q29, Q34, Q39, Q44, Q49
                4, 14, 24, 29, 34, 39, 44, 49,
                // Openness: Q10, Q20, Q30
                10, 20, 30
        };

        for (int reverseQ : reverseQuestions) {
            if (questionNumber == reverseQ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Alternative method if you want to return individual scores as a Map
     */
    public Map<String, Integer> getOceanScoresMap(List<ValidationAnswerRequest> validationAnswerList) {
        // Verify we have exactly 50 answers
        if (validationAnswerList == null || validationAnswerList.size() != 50) {
            throw new IllegalArgumentException("OCEAN test requires exactly 50 answers");
        }

        // Convert all responses to Scored Points (SP)
        int[] sp = new int[51];

        for (int i = 0; i < validationAnswerList.size(); i++) {
            ValidationAnswerRequest answer = validationAnswerList.get(i);
            int questionNumber = i + 1;
            String answerText = answer.getAnswerText();

            if (answerText == null || !optionsScore.containsKey(answerText)) {
                throw new IllegalArgumentException(
                        "Invalid answer text for question " + questionNumber + ": " + answerText
                );
            }

            int rv = optionsScore.get(answerText);
            boolean isReverse = isReverseQuestion(questionNumber);

            sp[questionNumber] = isReverse ? (6 - rv) : rv;
        }

        // Calculate scores
        int E = 20 + sp[1] - sp[6] + sp[11] - sp[16] + sp[21] - sp[26] + sp[31] - sp[36] + sp[41] - sp[46];
        int A = 14 - sp[2] + sp[7] - sp[12] + sp[17] - sp[22] + sp[27] - sp[32] + sp[37] + sp[42] + sp[47];
        int C = 14 + sp[3] - sp[8] + sp[13] - sp[18] + sp[23] - sp[28] + sp[33] - sp[38] + sp[43] + sp[48];
        int N = 38 - sp[4] + sp[9] - sp[14] + sp[19] - sp[24] - sp[29] - sp[34] - sp[39] - sp[44] - sp[49];
        int O = 8 + sp[5] - sp[10] + sp[15] - sp[20] + sp[25] - sp[30] + sp[35] + sp[40] + sp[45] + sp[50];

        return Map.of(
                "Extraversion", E,
                "Agreeableness", A,
                "Conscientiousness", C,
                "Neuroticism", N,
                "Openness", O
        );
    }
}
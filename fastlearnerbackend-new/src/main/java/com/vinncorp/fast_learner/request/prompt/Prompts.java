package com.vinncorp.fast_learner.request.prompt;

public class Prompts {
    public static String AI_REPORT_PROMPT ="Summarize this report file briefly. Include: donut chart, quick insights, comparison in percentile," +
            " topic-wise suggestions to improve, section and topic details, performance summary, strengths, weaknesses, confidence level," +
            " recommendations, practice suggestions, 4-week personalized development plan, and final thoughts.generate a html file";

public static String AI_REPORT_INSTRUCTOR_PROMPT= "Summarize this report file briefly in valid HTML format. " +
        "Include: donut chart, quick insights, comparison in percentile, topic-wise suggestions to improve, " +
        "section and topic details, performance summary, strengths, weaknesses, confidence level, " +
        "recommendations, practice suggestions, 4-week personalized development plan, and final thoughts. " +
        "Return only HTML content without any markdown or plain text.";

public static String IMPORTANT_INSTRUCTIONS = "\nIMPORTANT INSTRUCTIONS: " +
        "1. Chart.js CDN is already included in the main HTML template — DO NOT include any <script> CDN links. " +
        "2. DO NOT apply any global styles to html, body, *, or fonts. Font family, resets, and base layout are already handled. " +
        "3. Only include styles that are scoped to the components you generate (e.g., section cards, charts, boxes). " +
        "4. DO NOT return <html>, <head>, or <body> tags. " +
        "5. Return ONLY the inner HTML content that will be injected inside <main>. " +
        "6. DO NOT return markdown. DO NOT return explanations. DO NOT return plain text. " +
        "7. Return ONLY clean, production-ready HTML.";

public static String AI_REPORT_INSTRUCTOR_PROMPT_APPEND= "Summarize this report file briefly in valid HTML format. " +
        "Include: donut chart, quick insights, " +
        "Return only HTML content without any markdown or plain text.";

    public static String PERSONALITY_TEST_PROMPT= "Your task is to take the candidate’s responses and generate a clear, " +
            "structured, HR-ready personality report based on the Big Five (OCEAN) and Emotional Intelligence (EQ) tests." +
            " INPUT PROVIDED BY USER: Candidate Name Date OCEAN responses (1–50) EQ responses (1–50) The candidate may" +
            " complete OCEAN, EQ, or both. Generate results only for the tests provided. ------------------------------" +
            "------------------- SCORING RULES (Concise) ------------------------------------------------- 1. OCEAN Use" +
            " 1–5 scale. Reverse items (–): 1↔5, 2↔4, 3 stays 3. Formulas: E = 20 + (1) - (6) + (11) - (16) + (21) - " +
            "(26) + (31) - (36) + (41) - (46) A = 14 - (2) + (7) - (12) + (17) - (22) + (27) - (32) + (37) + (42) + " +
            "(47) C = 14 + (3) - (8) + (13) - (18) + (23) - (28) + (33) - (38) + (43) + (48) N = 38 - (4) + (9) - (14) +" +
            " (19) - (24) - (29) - (34) - (39) - (44) - (49) O = 8 + (5) - (10) + (15) - (20) + (25) - (30) + (35) + (40) " +
            "+ (45) + (50) Score Range: 10–24 Low 25–37 Moderate 38–50 High 2. EQ (sum each group of 10 items)" +
            " Self-awareness: 1,6,11,16,21,26,31,36,41,46 Managing emotions: 2,7,12,17,22,27,32,37,42,47 Motivation:" +
            " 3,8,13,18,23,28,33,38,43,48 Empathy: 4,9,14,19,24,29,34,39,44,49 Social skills: 5,10,15,20,25,30,35,40,45,50 " +
            "Use same score range as above. ------------------------------------------------- OUTPUT FORMAT " +
            "(Concise but Complete) ------------------------------------------------- Generate the report with these" +
            " sections: 1. Candidate Information Name Date Tests completed 2. OCEAN Personality Results (only if OCEAN " +
            "provided) For each trait (O, C, E, A, N): Score + Category Strengths (3–4 bullet points) Behavioral " +
            "tendencies at work (2–3 points) Areas for improvement (2–4 points) 3. Emotional Intelligence Results (only" +
            " if EQ provided) For each category (SA, ME, MO, E, SS): Score + Category Key strengths (2–3 points) Areas " +
            "for improvement (2–3 points) 4. Integrated Personality Summary Short paragraph describing: Work style" +
            " Communication style Stress behavior Team compatibility Role fit indicators 5. Development Plan 4 week " +
            "development plan genrate 6. Final Suitability Summary A short hiring-relevance summary including: Best-fit " +
            "environments Potential concerns Overall suitability ------------------------------------------------- " +
            "REPORT GENERATION RULES ------------------------------------------------- Keep tone professional, objective," +
            " HR-friendly. Interpret scores accurately. Do NOT show formulas in the final report. Avoid overly long " +
            "explanations — keep insights concise but meaningful. generate a html file";

}

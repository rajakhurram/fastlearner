package com.vinncorp.fast_learner.services.reports;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestReportService {
    private final TemplateEngine templateEngine;

    public String generateReport(
            String studentName,
            String instructorName,
            String aiGeneratedContent,
            String topicTitle,
            Integer durationInMinutes) {

        String studentInitials = getInitials(studentName);
        String formattedStudentName = formatedName(studentName);
        String formattedInstructorName = formatedName(instructorName);
        String reportDate = LocalDate.now().toString();


        return ReportString.template()
                .replace("{{TOPIC_TITLE}}", topicTitle)
                .replace("{{STUDENT_INITIALS}}", studentInitials)
                .replace("{{STUDENT_NAME}}", formattedStudentName)
                .replace("{{REPORT_DATE}}", reportDate)
                .replace("{{DURATION}}", durationInMinutes.toString())
                .replace("{{INSTRUCTOR_NAME}}", formattedInstructorName)
                .replace("{{REPORT_CONTENT}}", aiGeneratedContent);
    }


    private String getInitials(String name) {
        String[] parts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) initials.append(part.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    private String formatedName(String name) {
        String[] parts = name.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }
        return sb.toString().trim();
    }
}

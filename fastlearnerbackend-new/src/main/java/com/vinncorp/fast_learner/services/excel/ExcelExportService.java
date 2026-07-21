package com.vinncorp.fast_learner.services.excel;

import com.itextpdf.layout.properties.TextAlignment;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnwserDto;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionDto;
import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import com.vinncorp.fast_learner.response.quiz.QuestionEvaluationResponse;

import com.vinncorp.fast_learner.util.InMemoryMultipartFile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;


@Service
public class ExcelExportService {


    public byte[] exportToPdf(List<QuestionEvaluationResponse> responses) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("Quiz Evaluation Report")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        int questionNumber = 1;

        for (QuestionEvaluationResponse q : responses) {
            // Question header
            document.add(new Paragraph("Question " + questionNumber + ": " + q.getQuestionText())
                    .setBold()
                    .setFontSize(12));

            // Correct Answers
            document.add(new Paragraph("Correct Answer(s): " + String.join(", ", q.getCorrectAnswers()))
                    .setFontSize(11)
                    .setMarginLeft(10));

            // Student Answers
            document.add(new Paragraph("Student Answer(s): " + String.join(", ", q.getStudentAnswers()))
                    .setFontSize(11)
                    .setMarginLeft(10));

            // Is Correct
            String correctness = q.getIsCorrect() ? "True" : "False";
            document.add(new Paragraph("Is Correct: " + correctness)
                    .setFontSize(11)
                    .setMarginLeft(10)
                    .setBold());

            // Separator line
            document.add(new Paragraph("---------------------------------------------")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(10)
                    .setMarginBottom(10));

            questionNumber++;
        }

        document.close();
        return out.toByteArray();
    }

    //for preview report
    public byte[] exportQuizToPdf(List<QuizQuestionDto> questions) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Title
        document.add(new Paragraph("Quiz Questions Report")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        int questionNumber = 1;

        for (QuizQuestionDto question : questions) {
            // Question Header
            document.add(new Paragraph("Question " + questionNumber + ": " + question.getQuestionText())
                    .setBold()
                    .setFontSize(12)
                    .setMarginBottom(5));

            // Answers List
            if (question.getQuizQuestionAnwsers() != null && !question.getQuizQuestionAnwsers().isEmpty()) {
                for (QuizQuestionAnwserDto ans : question.getQuizQuestionAnwsers()) {
                    String prefix = ans.isCorrectAnswer() ? "✅ Correct: " : "• ";
                    document.add(new Paragraph(prefix + ans.getAnswer())
                            .setFontSize(11)
                            .setMarginLeft(15)
                            .setFontColor(ans.isCorrectAnswer()
                                    ? com.itextpdf.kernel.colors.ColorConstants.GREEN
                                    : com.itextpdf.kernel.colors.ColorConstants.BLACK));
                }
            } else {
                document.add(new Paragraph("No answers available.")
                        .setItalic()
                        .setFontSize(11)
                        .setMarginLeft(15));
            }

            // Explanation
            if (question.getExplanation() != null && !question.getExplanation().isBlank()) {
                document.add(new Paragraph("Explanation: " + question.getExplanation())
                        .setFontSize(11)
                        .setMarginLeft(10)
                        .setMarginTop(5)
                        .setItalic());
            }

            // Separator line
            document.add(new Paragraph("---------------------------------------------")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(10)
                    .setMarginBottom(10)
                    .setMarginTop(10));

            questionNumber++;
        }

        document.close();
        return out.toByteArray();
    }


    public byte[] exportPdfForPersonalityTest(List<ValidationAnswerRequest> responses) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Title
        document.add(new Paragraph("Personality Test Report")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        int questionNumber = 1;

        for (ValidationAnswerRequest q : responses) {
            // to calculate reverse scoring in OCEAN test.
            String questionSign = (questionNumber % 2 != 0) ? "+ " : "- ";
            // Question text
            document.add(new Paragraph(questionSign + "Question " + questionNumber + ": " + q.getQuestionText())
                    .setBold()
                    .setFontSize(12));

            // Student selected answer(s)
            if (q.getAnswerText() != null && !q.getAnswerText().isEmpty()) {
                document.add(new Paragraph("Answer: " + q.getAnswerText())
                        .setFontSize(11)
                        .setMarginLeft(0));
            }

            // Separator line
            document.add(new Paragraph("---------------------------------------------")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(10)
                    .setMarginBottom(10));

            questionNumber++;
        }

        document.close();
        return out.toByteArray();
    }

    public byte[] convertTextToPdf(String text) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Add a proper header to make it recognizable as an answer sheet
        document.add(new Paragraph("Student Answer Submission")
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("_______________________________________________")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

//        // Add metadata section
//        document.add(new Paragraph("Student Information:")
//                .setBold()
//                .setFontSize(11)
//                .setMarginBottom(5));
//
//        document.add(new Paragraph("Name: [Not Provided]")
//                .setFontSize(10)
//                .setMarginLeft(10)
//                .setMarginBottom(3));
//
//        document.add(new Paragraph("Roll Number: [Not Provided]")
//                .setFontSize(10)
//                .setMarginLeft(10)
//                .setMarginBottom(3));
//
//        document.add(new Paragraph("Email: [Not Provided]")
//                .setFontSize(10)
//                .setMarginLeft(10)
//                .setMarginBottom(15));

        // Add answer section header
        document.add(new Paragraph("Student Response:")
                .setBold()
                .setFontSize(12)
                .setMarginBottom(8));

        // Add the actual text content with proper formatting
        // Split by lines to maintain structure
        String[] lines = text.split("\n");
        for (String line : lines) {
            document.add(new Paragraph(line.trim())
                    .setFontSize(11)
                    .setMarginLeft(10)
                    .setMarginBottom(3));
        }

        document.close();

        return byteArrayOutputStream.toByteArray();
    }

    public InMemoryMultipartFile convertBytesToMultipartFile(byte[] bytes, String fileName, String contentType) {
        return new InMemoryMultipartFile(bytes, "file", fileName, contentType);
    }
}

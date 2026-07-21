package com.vinncorp.fast_learner.services.excel;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.request.quiz.CreateQuizQuestionAnswerRequest;
import com.vinncorp.fast_learner.request.quiz.CreateQuizQuestionRequest;
import com.vinncorp.fast_learner.response.quiz.BulkQuestionParseError;
import com.vinncorp.fast_learner.response.quiz.BulkQuizQuestionsParseResponse;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TestBulkQuestionExcelParser {

    public static final String EXCEL_FORMAT_GUIDE = """
            Upload an .xlsx file with a header row and one question per row.
            
            Required columns:
            - questionText: The question text
            - questionType: SINGLE_CHOICE | MULTIPLE_CHOICE | TRUE_FALSE | TEXT_FIELD
            - correctAnswers: For SINGLE_CHOICE / MULTIPLE_CHOICE use option letters (e.g. A or A,C) or full option text; for TRUE_FALSE use True/False; for TEXT_FIELD use expected answer text
            
            Optional columns:
            - explanation: Explanation shown after answering
            - questionImageUrl: Image URL for the question
            
            Option columns (at least one required except for rules below):
            - A, B, C, D, E, F
            
            Rules by questionType:
            - SINGLE_CHOICE: At least 2 options; exactly one value in correctAnswers
            - MULTIPLE_CHOICE: At least 2 options; one or more values in correctAnswers
            - TRUE_FALSE: A=True and B=False (auto-filled if blank); correctAnswers is True or False
            - TEXT_FIELD: Use column A or correctAnswers for the expected answer (single answer)
            """;

    private static final int MAX_ROWS = 500;
    private static final List<String> OPTION_COLUMNS = List.of("a", "b", "c", "d", "e", "f");

    public BulkQuizQuestionsParseResponse parse(MultipartFile file) throws BadRequestException, IOException {
        validateFile(file);

        List<CreateQuizQuestionRequest> questions = new ArrayList<>();
        List<BulkQuestionParseError> errors = new ArrayList<>();

        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                throw new BadRequestException("Excel file must contain a header row and at least one question row.");
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columnIndex = mapHeaderColumns(headerRow);
            validateRequiredHeaders(columnIndex);

            int lastRow = sheet.getLastRowNum();
            int dataRows = 0;
            for (int rowIdx = sheet.getFirstRowNum() + 1; rowIdx <= lastRow; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowEmpty(row, columnIndex)) {
                    continue;
                }
                dataRows++;
                if (dataRows > MAX_ROWS) {
                    throw new BadRequestException("Excel file cannot contain more than " + MAX_ROWS + " questions.");
                }
                int displayRow = rowIdx + 1;
                try {
                    questions.add(parseQuestionRow(row, columnIndex));
                } catch (BadRequestException ex) {
                    errors.add(BulkQuestionParseError.builder()
                            .rowNumber(displayRow)
                            .message(ex.getMessage())
                            .build());
                }
            }

            if (dataRows == 0) {
                throw new BadRequestException("No question rows found in the Excel file.");
            }
        }

        return BulkQuizQuestionsParseResponse.builder()
                .questions(questions)
                .errors(errors)
                .excelFormatGuide(EXCEL_FORMAT_GUIDE)
                .totalRowsParsed(questions.size() + errors.size())
                .successCount(questions.size())
                .errorCount(errors.size())
                .build();
    }

    private void validateFile(MultipartFile file) throws BadRequestException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Excel file is required.");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            throw new BadRequestException("Only .xlsx files are supported.");
        }
    }

    private Map<String, Integer> mapHeaderColumns(Row headerRow) throws BadRequestException {
        Map<String, Integer> columnIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = normalizeHeader(getCellString(cell));
            if (!header.isEmpty()) {
                columnIndex.put(header, cell.getColumnIndex());
            }
        }
        if (columnIndex.isEmpty()) {
            throw new BadRequestException("Header row is missing or empty.");
        }
        return columnIndex;
    }

    private void validateRequiredHeaders(Map<String, Integer> columnIndex) throws BadRequestException {
        List<String> required = List.of("questiontext", "questiontype", "correctanswers");
        List<String> missing = required.stream()
                .filter(h -> !columnIndex.containsKey(h))
                .map(this::toDisplayHeader)
                .toList();
        if (!missing.isEmpty()) {
            throw new BadRequestException("Missing required column(s): " + String.join(", ", missing));
        }
    }

    private CreateQuizQuestionRequest parseQuestionRow(Row row, Map<String, Integer> columnIndex)
            throws BadRequestException {
        String questionText = requireCell(row, columnIndex, "questiontext", "questionText");
        QuestionType questionType = parseQuestionType(requireCell(row, columnIndex, "questiontype", "questionType"));
        String explanation = optionalCell(row, columnIndex, "explanation");
        String questionImageUrl = optionalCell(row, columnIndex, "questionImageUrl");

        List<String> optionTexts = readOptions(row, columnIndex);
        String correctAnswersRaw = requireCell(row, columnIndex, "correctanswers", "correctAnswers");

        if (questionType == QuestionType.TRUE_FALSE) {
            optionTexts = normalizeTrueFalseOptions(optionTexts);
        }

        List<CreateQuizQuestionAnswerRequest> answers = buildAnswers(optionTexts, correctAnswersRaw, questionType);
        validateQuestionRules(questionType, answers);

        CreateQuizQuestionRequest question = new CreateQuizQuestionRequest();
        question.setQuestionText(questionText);
        question.setQuestionType(questionType);
        question.setExplanation(emptyToNull(explanation));
        question.setQuestionImageUrl(emptyToNull(questionImageUrl));
        question.setAnswers(answers);
        question.setDelete(false);
        return question;
    }

    private List<String> readOptions(Row row, Map<String, Integer> columnIndex) {
        List<String> options = new ArrayList<>();
        for (String letter : OPTION_COLUMNS) {
            if (!columnIndex.containsKey(letter)) {
                continue;
            }
            String value = optionalCell(row, columnIndex, letter);
            if (value != null && !value.isBlank()) {
                options.add(value.trim());
            }
        }
        return options;
    }

    private List<String> normalizeTrueFalseOptions(List<String> optionTexts) {
        if (optionTexts.size() >= 2) {
            return List.of(optionTexts.get(0).trim(), optionTexts.get(1).trim());
        }
        return List.of("True", "False");
    }

    private List<CreateQuizQuestionAnswerRequest> buildAnswers(
            List<String> optionTexts,
            String correctAnswersRaw,
            QuestionType questionType) throws BadRequestException {

        Set<String> correctSet = parseCorrectAnswers(correctAnswersRaw);

        if (questionType == QuestionType.TEXT_FIELD) {
            String expected = correctSet.stream().findFirst()
                    .orElse(optionTexts.isEmpty() ? null : optionTexts.get(0).trim());
            if (expected == null || expected.isBlank()) {
                throw new BadRequestException("TEXT_FIELD requires correctAnswers or column A.");
            }
            return List.of(answer(expected, null, true, optionLetter(0)));
        }

        if (optionTexts.isEmpty()) {
            throw new BadRequestException("At least one option column (A..F) is required.");
        }

        Set<Integer> correctIndices = resolveCorrectOptionIndices(correctSet, optionTexts, questionType);

        List<CreateQuizQuestionAnswerRequest> answers = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String optionValue = optionTexts.get(i);
            boolean isImageUrl = isLikelyUrl(optionValue);
            answers.add(answer(
                    isImageUrl ? "" : optionValue,
                    isImageUrl ? optionValue : null,
                    correctIndices.contains(i),
                    optionLetter(i)));
        }

        long correctCount = answers.stream().filter(CreateQuizQuestionAnswerRequest::getIsCorrectAnswer).count();
        if (correctCount == 0) {
            throw new BadRequestException(
                    "correctAnswers must be option letter(s) (A, B, C, ...) or match option text. Provided: "
                            + correctAnswersRaw);
        }
        return answers;
    }

    /**
     * Resolves correct option indices from correctAnswers tokens.
     * Supports letter refs (A, B, C) for choice types and legacy full option text matching.
     */
    private Set<Integer> resolveCorrectOptionIndices(
            Set<String> correctTokens,
            List<String> optionTexts,
            QuestionType questionType) throws BadRequestException {

        Set<Integer> indices = new LinkedHashSet<>();
        if (correctTokens.isEmpty()) {
            return indices;
        }

        boolean useLetterRefs = questionType == QuestionType.SINGLE_CHOICE
                || questionType == QuestionType.MULTIPLE_CHOICE;

        for (String token : correctTokens) {
            if (useLetterRefs && isOptionLetter(token)) {
                int index = optionLetterToIndex(token);
                if (index < 0 || index >= optionTexts.size()) {
                    throw new BadRequestException("Invalid option letter '" + token.toUpperCase()
                            + "' for " + optionTexts.size() + " option(s).");
                }
                indices.add(index);
            } else {
                boolean matched = false;
                for (int i = 0; i < optionTexts.size(); i++) {
                    if (optionTexts.get(i).equalsIgnoreCase(token.trim())) {
                        indices.add(i);
                        matched = true;
                    }
                }
                if (!matched && !useLetterRefs) {
                    // TRUE_FALSE / TEXT_FIELD rely on text match only
                } else if (!matched && useLetterRefs) {
                    throw new BadRequestException("correctAnswers token '" + token
                            + "' does not match any option letter or option text.");
                }
            }
        }
        return indices;
    }

    private boolean isOptionLetter(String token) {
        return token != null && token.trim().matches("^[A-Za-z]$");
    }

    private int optionLetterToIndex(String letter) {
        return Character.toUpperCase(letter.trim().charAt(0)) - 'A';
    }

    private String optionLetter(int zeroBasedIndex) {
        return String.valueOf((char) ('A' + zeroBasedIndex));
    }

    private Set<String> parseCorrectAnswers(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateQuestionRules(QuestionType questionType, List<CreateQuizQuestionAnswerRequest> answers)
            throws BadRequestException {
        long correctCount = answers.stream().filter(CreateQuizQuestionAnswerRequest::getIsCorrectAnswer).count();

        switch (questionType) {
            case SINGLE_CHOICE -> {
                if (answers.size() < 2) {
                    throw new BadRequestException("SINGLE_CHOICE requires at least 2 options.");
                }
                if (correctCount != 1) {
                    throw new BadRequestException("SINGLE_CHOICE requires exactly one correct answer.");
                }
            }
            case MULTIPLE_CHOICE -> {
                if (answers.size() < 2) {
                    throw new BadRequestException("MULTIPLE_CHOICE requires at least 2 options.");
                }
                if (correctCount < 1) {
                    throw new BadRequestException("MULTIPLE_CHOICE requires at least one correct answer.");
                }
            }
            case TRUE_FALSE -> {
                if (answers.size() != 2) {
                    throw new BadRequestException("TRUE_FALSE requires exactly two options (True and False).");
                }
                List<String> texts = answers.stream().map(CreateQuizQuestionAnswerRequest::getAnswerText).toList();
                if (!texts.stream().anyMatch(t -> t.equalsIgnoreCase("True"))
                        || !texts.stream().anyMatch(t -> t.equalsIgnoreCase("False"))) {
                    throw new BadRequestException("TRUE_FALSE options must be True and False.");
                }
                if (correctCount != 1) {
                    throw new BadRequestException("TRUE_FALSE requires exactly one correct answer (True or False).");
                }
            }
            case TEXT_FIELD -> {
                if (answers.size() != 1) {
                    throw new BadRequestException("TEXT_FIELD allows only one answer.");
                }
                if (correctCount != 1) {
                    throw new BadRequestException("TEXT_FIELD requires one correct answer.");
                }
            }
        }
    }

    private QuestionType parseQuestionType(String raw) throws BadRequestException {
        try {
            return QuestionType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid questionType '" + raw
                    + "'. Use SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, or TEXT_FIELD.");
        }
    }

    private CreateQuizQuestionAnswerRequest answer(String text, String imageUrl, boolean correct, String answerOrder) {
        CreateQuizQuestionAnswerRequest a = new CreateQuizQuestionAnswerRequest();
        a.setAnswerText(text);
        a.setAnswerImageUrl(imageUrl);
        a.setIsCorrectAnswer(correct);
        a.setAnswerOrder(answerOrder);
        a.setDelete(false);
        return a;
    }

    private boolean isLikelyUrl(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim().toLowerCase();
        return v.startsWith("http://") || v.startsWith("https://");
    }

    private boolean isRowEmpty(Row row, Map<String, Integer> columnIndex) {
        Integer qCol = columnIndex.get("questiontext");
        if (qCol == null) {
            return true;
        }
        return getCellString(row.getCell(qCol)).isBlank();
    }

    private String requireCell(Row row, Map<String, Integer> columnIndex, String... keys)
            throws BadRequestException {
        String value = optionalCell(row, columnIndex, keys);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing required value for " + toDisplayHeader(keys[0]));
        }
        return value.trim();
    }

    private String optionalCell(Row row, Map<String, Integer> columnIndex, String... keys) {
        for (String key : keys) {
            Integer idx = columnIndex.get(normalizeHeader(key));
            if (idx != null) {
                return getCellString(row.getCell(idx)).trim();
            }
        }
        return "";
    }

    private String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    yield String.valueOf((long) num);
                }
                yield String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase().replace(" ", "").replace("_", "");
    }

    private String toDisplayHeader(String normalizedKey) {
        return switch (normalizedKey) {
            case "questiontext" -> "questionText";
            case "questiontype" -> "questionType";
            case "correctanswers" -> "correctAnswers";
            case "questionimageurl" -> "questionImageUrl";
            default -> normalizedKey;
        };
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

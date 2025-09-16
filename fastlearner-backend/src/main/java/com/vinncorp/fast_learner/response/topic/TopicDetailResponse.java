package com.vinncorp.fast_learner.response.topic;

import com.vinncorp.fast_learner.dtos.docs.DocumentDto;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnswer;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicDetailResponse {

    private Long topicId;
    private Long sectionId;
    private String topicName;
    private String topicType;
    private long topicDuration;
    private int sequenceNumber;
    private Long videoId;
    private String filename;
    private Long articleId;
    private String article;
    private String videoUrl;
    private Long quizId;
    private String quizTitle;
    private Integer randomQuestion;
    private Integer durationInMinutes;
    private Double passingCriteria;
    private QuizQuestionAnswer quizQuestionAnswer;
    private List<DocumentDto> docs;
    private Long progressId;
    private Long seekTime;
    private Boolean isCompleted;
    private String vttContent;

    public static List<DocumentDto> convertToDocumentList(ArrayList<LinkedHashMap<String, Object>> list) {
        List<DocumentDto> documentList = new ArrayList<>();
        for (LinkedHashMap<String, Object> map : list) {
            DocumentDto document = DocumentDto.builder()
                    .id((Integer) map.get("id"))
                    .name((String) map.get("name"))
                    .url((String) map.get("url"))
                    .build();

            documentList.add(document);
        }
        return documentList;
    }

    public static List<TopicDetailResponse> from(List<Tuple> tuples) {
        return tuples.stream()
                .map(e -> {
                    ArrayList<LinkedHashMap<String, Object>> documentObjects = (ArrayList<LinkedHashMap<String, Object>>) e.get("document_object");
                    List<DocumentDto> documents = null;
                    if (documentObjects != null) {
                        documents = convertToDocumentList(documentObjects);
                        if (documents != null && !documents.isEmpty() && documents.get(0).getId() == null) {
                            documents = null;
                        }
                    }


                    Long topicId = (Long) e.get("id");
//                    QuizQuestionAnswer quizQuestionAnswer = quizQuestionAnswers.stream().filter(q -> q.getTopicId().equals(topicId)).findAny().orElse(null);
                    TopicDetailResponse data = TopicDetailResponse.builder()
                            .topicId((Long) e.get("id"))
                            .docs(documents)
                            .sectionId((Long) e.get("section_id"))
                            .topicName((String) e.get("name"))
                            .topicType((String) e.get("type_name"))
                            .sequenceNumber(e.get("sequence_number") != null ? Integer.parseInt("" +  e.get("sequence_number")) : 0)
                            .topicDuration(Objects.isNull(e.get("duration_in_sec")) ? 0 : Long.parseLong("" + e.get("duration_in_sec")))
                            .videoId((Long) e.get("video_id"))
                            .filename((String) e.get("filename"))
                            .videoUrl((String) e.get("videourl"))
                            .quizId((Long) e.get("quiz_id"))
                            .quizTitle((String) e.get("title"))
                            .durationInMinutes((Integer) e.get("duration_in_minutes"))
                            .passingCriteria((Double) e.get("passing_criteria"))
                            .randomQuestion((Integer) e.get("random_question"))
                            .progressId((Long) e.get("progress_id"))
                            .seekTime((Long) e.get("seek_time"))
                            .isCompleted(e.get("is_completed") != null && Boolean.parseBoolean( "" + e.get("is_completed")))
                            .articleId((Long) e.get("article_id"))
                            .article(e.get("content") != null ? HtmlUtils.htmlUnescape((String) e.get("content")) : null)
                            .quizQuestionAnswer(null)
                            .vttContent((String) e.get("vtt_content"))
                            .build();
                    return data;
                })
                .toList();
    }

}

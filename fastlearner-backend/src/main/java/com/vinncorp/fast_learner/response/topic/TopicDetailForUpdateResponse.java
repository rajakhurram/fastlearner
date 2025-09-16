package com.vinncorp.fast_learner.response.topic;

import com.vinncorp.fast_learner.dtos.docs.DocumentDto;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnswer;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicDetailForUpdateResponse {

    private Long sectionId;
    private Long topicId;
    private String topicName;
    private Long topicTypeId;
    private String topicType;
    private int topicDuration;
    private int sequenceNumber;

    private Long videoId;
    private String filename;
    private String videoUrl;
    private String summary;
    private String transcript;
    private String vttContent;

    private Long articleId;
    private String article;

    private Long quizId;
    private String quizTitle;
    private Integer randomQuestion;
    private Integer durationInMinutes;
    private Double passingCriteria;
    private QuizQuestionAnswer quizQuestionAnswer;
    private List<DocumentDto> docs;
    private Date creationDate;

    public static List<DocumentDto> convertToDocumentList(ArrayList<LinkedHashMap<String, Object>> list) {
        List<DocumentDto> documentList = new ArrayList<>();
        for (LinkedHashMap<String, Object> map : list) {
            DocumentDto document = DocumentDto.builder()
                    .id((Integer) map.get("id"))
                    .name((String) map.get("name"))
                    .url((String) map.get("url"))
                    .summary((String) map.get("summary"))
                    .build();

            documentList.add(document);
        }
        return documentList;
    }

    public static List<TopicDetailForUpdateResponse> from(List<Tuple> tuples) {
        return tuples.stream()
                .map(e -> {
                    ArrayList<LinkedHashMap<String, Object>> documentObjects = (ArrayList<LinkedHashMap<String, Object>>) e.get("document_object");
                    List<DocumentDto> documents = convertToDocumentList(documentObjects);
                    if(documents.get(0).getId() == null)
                        documents = null;

                    Long topicId = (Long) e.get("id");
//                    QuizQuestionAnswer quizQuestionAnswer = quizQuestionAnswers.stream().filter(q -> q.getTopicId().equals(topicId)).findAny().orElse(null);

                    TopicDetailForUpdateResponse data = TopicDetailForUpdateResponse.builder()
                            .topicId((Long) e.get("id"))
                            .docs(documents)
                            .sectionId((Long) e.get("section_id"))
                            .topicName((String) e.get("name"))
                            .topicTypeId((Long) e.get("topic_type_id"))
                            .topicType((String) e.get("type_name"))
                            .creationDate((Date) e.get("creation_date"))
                            .sequenceNumber(e.get("sequence_number") != null ? Integer.parseInt("" +  e.get("sequence_number")) : 0)
                            .topicDuration(Objects.nonNull(e.get("duration_in_sec")) ? Integer.parseInt("" + e.get("duration_in_sec")) : 0)
                            .videoId((Long) e.get("video_id"))
                            .filename((String) e.get("filename"))
                            .videoUrl((String) e.get("videourl"))
                            .summary((String) e.get("video_summary"))
                            .transcript((String) e.get("transcribe"))
                            .quizId((Long) e.get("quiz_id"))
                            .quizTitle((String) e.get("title"))
                            .randomQuestion((Integer) e.get("random_question"))
                            .durationInMinutes((Integer) e.get("duration_in_minutes"))
                            .passingCriteria((Double) e.get("passing_criteria"))
                            .articleId((Long) e.get("article_id"))
                            .article(e.get("content") != null ? HtmlUtils.htmlUnescape((String) e.get("content")) : null)
                            .vttContent((String) e.get("vtt_content"))
                            .quizQuestionAnswer(null)
                            .build();
                    return data;
                })
                .toList();
    }

}

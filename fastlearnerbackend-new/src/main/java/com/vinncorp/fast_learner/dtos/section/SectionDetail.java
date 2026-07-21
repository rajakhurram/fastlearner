package com.vinncorp.fast_learner.dtos.section;

import com.vinncorp.fast_learner.models.section.Section;
import jakarta.persistence.Tuple;
import lombok.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionDetail {

    private Long sectionId;
    private String sectionName;
    private Integer sectionLevel;
    private int sectionDuration;
    private Boolean isFree;
    private double review;
    private int noOfReviewers;

    private boolean isPanelOpen = false;
    private Boolean delete;
    private List<TopicDetail> topicDetails;

    public static List<SectionDetail> from(List<Tuple> sectionDetails) {
        Set<SectionDetail> sections = new LinkedHashSet<>();
        List<TopicDetail> topics = new ArrayList<>();
        sectionDetails.forEach(e -> {
            sections.add(SectionDetail.builder()
                    .sectionId((Long) e.get("section_id"))
                    .sectionName((String) e.get("section_name"))
                    .sectionLevel(e.get("section_level") != null ? Integer.parseInt("" + e.get("section_level")) : null)
                    .isFree(e.get("is_free") != null && Boolean.parseBoolean("" + e.get("is_free")))
                    .review(e.get("sec_reviews") == null ? 0.0 : Double.parseDouble(""+e.get("sec_reviews")))
                    .noOfReviewers(e.get("total_sec_reviews") != null ? Integer.parseInt("" + e.get("total_sec_reviews")) : 0)
                    .build());

            topics.add(TopicDetail.builder()
                    .sectionId((Long) e.get("section_id"))
                    .topicId((Long) e.get("topic_id"))
                    .topicName((String) e.get("topic_name"))
                    .topicType((String) e.get("topic_type_name"))
                    .topicLevel(e.get("topic_level") != null ? Integer.parseInt("" +  e.get("topic_level")) : 0)
                    .topicDuration(e.get("duration") != null ? Integer.parseInt("" + e.get("duration")) : 0)
                    .videoId((Long) e.get("video_id"))
                    .videoFileName((String) e.get("video_filename"))
                    .videoUrl((String) e.get("video_url"))
                    .quizId((Long) e.get("quiz_id"))
                    .quizTitle((String) e.get("quiz_title"))
                    .article(Objects.nonNull(e.get("content")) ? HtmlUtils.htmlUnescape((String) e.get("content")) : null)
                    .build());
        });

        for (SectionDetail s : sections) {
            List<TopicDetail> topicsList = new ArrayList<>();
            s.setTopicDetails(topics.stream()
                    .filter(e -> e.getSectionId().equals(s.getSectionId())).collect(Collectors.toList()));
            s.setSectionDuration(
                    topics.stream()
                            .filter(e -> e.getSectionId().equals(s.getSectionId()))
                            .mapToInt(TopicDetail::getTopicDuration)
                            .sum());

        }

        return sections.stream().toList();
    }

    public static SectionDetail toSectionDetail(Section section){
        return SectionDetail
                .builder()
                .sectionId(section.getId())
                .sectionName(section.getName())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionDetail that = (SectionDetail) o;
        return Objects.equals(sectionId, that.sectionId) &&
                Objects.equals(sectionName, that.sectionName) &&
                Objects.equals(sectionLevel, that.sectionLevel) &&
                Objects.equals(isFree, that.isFree) &&
                Objects.equals(review, that.review) &&
                Objects.equals(noOfReviewers, that.noOfReviewers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectionId, sectionName, sectionLevel, isFree, review, noOfReviewers);
    }
}

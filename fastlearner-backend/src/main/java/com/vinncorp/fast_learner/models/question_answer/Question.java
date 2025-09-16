package com.vinncorp.fast_learner.models.question_answer;

import com.vinncorp.fast_learner.models.audit.Auditable;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.topic.Topic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question")
public class Question extends Auditable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @OneToOne(mappedBy = "question", cascade = CascadeType.REMOVE)
    private Answer answer;
}

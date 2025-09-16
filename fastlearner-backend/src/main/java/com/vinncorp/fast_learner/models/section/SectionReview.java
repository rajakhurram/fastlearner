package com.vinncorp.fast_learner.models.section;

import com.vinncorp.fast_learner.models.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "section_review")
public class SectionReview extends Auditable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    private String comment;
    private double rating;

    @Column(name = "likes", columnDefinition = "INT default 0")
    private int likes;

    @Column(name = "dislikes", columnDefinition = "INT default 0")
    private int dislikes;
}
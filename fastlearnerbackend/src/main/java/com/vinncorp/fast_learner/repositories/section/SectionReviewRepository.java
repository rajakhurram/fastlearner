package com.vinncorp.fast_learner.repositories.section;

import com.vinncorp.fast_learner.models.section.SectionReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SectionReviewRepository extends JpaRepository<SectionReview, Long> {

    @Transactional(readOnly = true)
    long countBySection_Id(Long id);

    SectionReview findBySectionIdAndCreatedBy(Long sectionId, Long userId);
}

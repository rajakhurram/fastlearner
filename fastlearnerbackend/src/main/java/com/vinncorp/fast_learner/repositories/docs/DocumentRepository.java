package com.vinncorp.fast_learner.repositories.docs;

import com.vinncorp.fast_learner.models.docs.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    boolean existsByUrl(String url);
}

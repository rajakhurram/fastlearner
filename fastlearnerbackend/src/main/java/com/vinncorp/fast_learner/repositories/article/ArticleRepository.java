package com.vinncorp.fast_learner.repositories.article;

import com.vinncorp.fast_learner.models.article.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Article findByTopicId(Long topicId);
}

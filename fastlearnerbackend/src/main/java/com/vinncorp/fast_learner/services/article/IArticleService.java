package com.vinncorp.fast_learner.services.article;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.article.Article;

public interface IArticleService {
    Article save(Article article) throws InternalServerException;
    Article findByTopicId(Long topicId);
}

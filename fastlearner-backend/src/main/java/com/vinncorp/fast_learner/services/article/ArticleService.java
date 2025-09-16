package com.vinncorp.fast_learner.services.article;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.article.ArticleRepository;
import com.vinncorp.fast_learner.models.article.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService implements IArticleService{

    private final ArticleRepository repo;

    @Override
    public Article save(Article article) throws InternalServerException {
        log.info("Creating article.");
        try {
            if (Objects.nonNull(article.getId()) && Objects.nonNull(article.getDelete()) && article.getDelete()) {
                repo.deleteById(article.getId());
                return null;
            }else {
                return repo.save(article);
            }
        } catch (Exception e) {
            log.error("ERROR: "+e.getLocalizedMessage());
            throw new InternalServerException("Article "+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    public Article findByTopicId(Long topicId){
        return this.repo.findByTopicId(topicId);
    }
}

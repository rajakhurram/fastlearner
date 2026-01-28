package com.vinncorp.fast_learner.mock.article;

import com.vinncorp.fast_learner.mock.topic.TopicTestData;
import com.vinncorp.fast_learner.models.article.Article;

import java.io.IOException;
import java.util.Arrays;

public class ArticleTestData {

    public static Article article() throws IOException {
        return Article.builder()
                .id(1L)
                .topic(TopicTestData.topicData())
                .content("Article contents")
                .documents(Arrays.asList())
                .delete(false)
                .build();
    }
}

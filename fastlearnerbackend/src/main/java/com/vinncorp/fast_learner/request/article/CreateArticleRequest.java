package com.vinncorp.fast_learner.request.article;

import com.vinncorp.fast_learner.request.docs.CreateDocumentsRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateArticleRequest {
    private Long id;
    private Boolean delete;
    private String article;
    private List<CreateDocumentsRequest> documents;
}

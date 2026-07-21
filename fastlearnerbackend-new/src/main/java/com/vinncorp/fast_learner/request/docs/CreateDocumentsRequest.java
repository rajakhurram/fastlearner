package com.vinncorp.fast_learner.request.docs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDocumentsRequest {

    private Long id;
    private Boolean delete;
    private String docName;
    private String docUrl;
    private String summary;
}

package com.vinncorp.fast_learner.request.video;

import com.vinncorp.fast_learner.request.docs.CreateDocumentsRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateVideoRequest {
    private Long id;
    private Boolean delete;
    private String filename;
    private String videoURL;
    private String summary;
    private String transcribe;
    private String vttContent;
    private List<CreateDocumentsRequest> documents;
}

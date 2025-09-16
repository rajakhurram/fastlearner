package com.vinncorp.fast_learner.request.uploader;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDeleteRequest {

    private Long id;
    private String url;
    private String fileType;
    private Long topicId;
}

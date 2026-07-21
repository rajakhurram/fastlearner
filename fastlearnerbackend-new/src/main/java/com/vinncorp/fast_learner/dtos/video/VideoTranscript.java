package com.vinncorp.fast_learner.dtos.video;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoTranscript {

    private String vttContent;
    private String transcript;
    private String summary;
    private Long duration;
}

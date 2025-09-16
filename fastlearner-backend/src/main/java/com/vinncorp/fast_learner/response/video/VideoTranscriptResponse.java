package com.vinncorp.fast_learner.response.video;

import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoTranscriptResponse {

    private String code;
    private String message;
    private VideoTranscript data;
    private int status;
}

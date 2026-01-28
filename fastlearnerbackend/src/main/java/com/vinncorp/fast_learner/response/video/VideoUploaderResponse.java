package com.vinncorp.fast_learner.response.video;

import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploaderResponse {

    private String url;
    private String fileName;
    private VideoTranscript transcriptData;
}

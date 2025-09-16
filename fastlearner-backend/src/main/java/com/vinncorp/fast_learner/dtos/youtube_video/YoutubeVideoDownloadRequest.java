package com.vinncorp.fast_learner.dtos.youtube_video;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeVideoDownloadRequest {
    private String url;
}

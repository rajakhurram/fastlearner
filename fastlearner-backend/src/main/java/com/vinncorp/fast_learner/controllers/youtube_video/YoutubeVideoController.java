package com.vinncorp.fast_learner.controllers.youtube_video;

import com.vinncorp.fast_learner.dtos.youtube_video.YoutubeVideoDownloadRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.response.video.VideoTranscriptResponse;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.YOUTUBE_VIDEO_DOWNLOADER)
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final IVideoService videoService;

    @PostMapping(APIUrls.YOUTUBE_VIDEO_DOWNLOAD)
    public ResponseEntity<VideoTranscriptResponse> download(@RequestBody YoutubeVideoDownloadRequest url, Principal principal) throws BadRequestException {
        var m = this.videoService.transcriptGeneratorViaYoutubeUrl(url, principal.getName());
        return ResponseEntity.ok(m);
    }

    @GetMapping(APIUrls.YOUTUBE_VIDEO_DURATION)
    public ResponseEntity<Message<Integer>> fetchDuration(@RequestParam String videoId, Principal principal) {
        var m = this.videoService.fetchDuration(videoId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}

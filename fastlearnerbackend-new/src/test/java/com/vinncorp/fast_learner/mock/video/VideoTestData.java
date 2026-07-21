package com.vinncorp.fast_learner.mock.video;

import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.mock.topic.TopicTestData;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.response.video.VideoTranscriptResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class VideoTestData {

    public static Video video() throws IOException {
        return Video.builder()
                .id(1L)
                .transcribe("Test transcribe")
                .summary("Test summary")
                .videoURL("testing url")
                .filename("Testing Video")
                .documents(Arrays.asList())
                .uploadedDate(new Date())
                .topic(TopicTestData.topicData())
                .build();
    }

    public static VideoTranscript videoTranscript() {
        return VideoTranscript.builder()
                .transcript("Test transcript")
                .summary("Test summary")
                .duration(100L)
                .build();
    }

    public static VideoTranscriptResponse videoTranscriptResponse() {
        return VideoTranscriptResponse.builder()
                .code(HttpStatus.OK.name())
                .status(HttpStatus.OK.value())
                .message("Successfully created transcript")
                .data(videoTranscript())
                .build();
    }
}

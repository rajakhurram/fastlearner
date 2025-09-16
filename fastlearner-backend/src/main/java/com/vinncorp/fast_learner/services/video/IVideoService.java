package com.vinncorp.fast_learner.services.video;

import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.youtube_video.YoutubeVideoDownloadRequest;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.response.video.VideoTranscriptResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.web.multipart.MultipartFile;

public interface IVideoService {
    Video save(Video video) throws InternalServerException;

    VideoTranscript generateTranscript(MultipartFile file) throws BadRequestException;

    Video getVideoByTopicId(Long id) throws EntityNotFoundException;

    boolean existsByUrl(String url);

    void deleteById(Long id);

    VideoTranscriptResponse transcriptGeneratorViaYoutubeUrl(YoutubeVideoDownloadRequest url, String email) throws BadRequestException;

    Message<Integer> fetchDuration(String videoId, String email);
}

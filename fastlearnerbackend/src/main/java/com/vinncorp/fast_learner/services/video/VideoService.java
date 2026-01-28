package com.vinncorp.fast_learner.services.video;

import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.dtos.youtube_video.YoutubeVideoDownloadRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.repositories.video.VideoRepository;
import com.vinncorp.fast_learner.response.video.VideoTranscriptResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService implements IVideoService{

    @Value("${transcript.generation.url}")
    private String TRANSCRIPT_GENERATION_URL;

    @Value("${youtube.video-downloader}")
    private String YOUTUBE_VIDEO_DOWNLOADER;

    @Value("${transcript.generation.auth-key}")
    private String TRANSCRIPT_GENERATION_AUTH_KEY;

    @Value("${youtube.api.key}")
    private String YOUTUBE_API_KEY;

    @Value("${youtube.video.duration.url}")
    private String YOUTUBE_DURATION_URL;

    private final VideoRepository repo;
    private final RestTemplate restTemplate;

    @Override
    public Video getVideoByTopicId(Long topicId) throws EntityNotFoundException {
        log.info("Fetching video by topic id: "+ topicId);
        return repo.findByTopicId(topicId)
                .orElseThrow(() -> new EntityNotFoundException("No video found for provided topic."));
    }

    @Override
    public Video save(Video video) throws InternalServerException {
        log.info("Creating video.");
        try {
            if (Objects.nonNull(video.getId()) && Objects.nonNull(video.getDelete()) && video.getDelete()) {
                repo.deleteById(video.getId());
                return null;
            }else {
                return repo.save(video);
            }
        } catch (Exception e) {
            log.error("ERROR: "+e.getLocalizedMessage());
            throw new InternalServerException("Video"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    // TODO NOTE I have deleted a RestTemplate object creation in below method and used above global
    //  RestTemplate object because the test for this method doesn't worked
    @Override
    public VideoTranscript generateTranscript(MultipartFile file) throws BadRequestException {
        log.info("Generating transcript for a video.");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", TRANSCRIPT_GENERATION_AUTH_KEY);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file_name", file.getName());
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // Perform the file upload
            log.info("Calling transcript generation service.");
            ResponseEntity<VideoTranscriptResponse> response = restTemplate.postForEntity(TRANSCRIPT_GENERATION_URL, request, VideoTranscriptResponse.class);
            log.info("Successfully called transcript generation service.");
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Transcript generated successfully.");
                return response.getBody().getData();
            } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.info("Audio stream not found in the video.");
                throw new BadRequestException(response.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new BadRequestException("Error: "+e.getLocalizedMessage());
        }
        throw new BadRequestException("Transcript generation is failed.");
    }

    @Override
    public boolean existsByUrl(String url) {
        return repo.existsByVideoURL(url);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public VideoTranscriptResponse transcriptGeneratorViaYoutubeUrl(YoutubeVideoDownloadRequest url, String email) throws BadRequestException {
        log.info("Fetching the video using the youtube url.");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", TRANSCRIPT_GENERATION_AUTH_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<YoutubeVideoDownloadRequest> request = new HttpEntity<>(url, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // Perform the file upload
            log.info("Calling transcript generation service.");
            ResponseEntity<VideoTranscriptResponse> response = restTemplate.postForEntity(YOUTUBE_VIDEO_DOWNLOADER, request, VideoTranscriptResponse.class);
            log.info("Successfully called transcript generation service.");
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Transcript generated successfully.");
                return response.getBody();
            }
        } catch (Exception e) {
            throw new BadRequestException("Error: "+e.getLocalizedMessage());
        }
        throw new BadRequestException("Transcript generation is failed.");
    }

    // TODO NOTE I have deleted a RestTemplate object creation in below method and used above global
    //  RestTemplate object because the test for this method doesn't worked
    @Override
    public Message<Integer> fetchDuration(String videoId, String email) {
        log.info("Fetching the duration of the youtube.");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(YOUTUBE_DURATION_URL + videoId + "&part=contentDetails&key=" + YOUTUBE_API_KEY, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return new Message<Integer>()
                    .setStatus(HttpStatus.BAD_REQUEST.value())
                    .setCode(HttpStatus.BAD_REQUEST.name())
                    .setMessage("Maybe the video id is not valid please provide valid id.");
        }
        Map<String, Object> responseBody = response.getBody();
        int durationInSeconds = 0;
        // Extract the duration from the response
        List<Map<String, Object>> items = (List<Map<String, Object>>) responseBody.get("items");
        if (!items.isEmpty()) {
            // Access the first item
            Map<String, Object> firstItem = items.get(0);
            Map<String, Object> contentDetails = (Map<String, Object>) firstItem.get("contentDetails");
            String duration = (String) contentDetails.get("duration");

            // Convert the duration to seconds
            durationInSeconds = TimeUtil.convertDurationToSeconds(duration);
        } else {
            return new Message<Integer>()
                    .setStatus(HttpStatus.BAD_REQUEST.value())
                    .setCode(HttpStatus.BAD_REQUEST.name())
                    .setMessage("No items found in the response.");
        }

        return new Message<Integer>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched the duration successfully.")
                .setData(durationInSeconds);
    }
}

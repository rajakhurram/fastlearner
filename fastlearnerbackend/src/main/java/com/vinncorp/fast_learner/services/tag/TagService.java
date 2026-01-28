package com.vinncorp.fast_learner.services.tag;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.repositories.tag.TagRepository;
import com.vinncorp.fast_learner.services.course.ICourseTagService;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseTag;
import com.vinncorp.fast_learner.request.tag.CreateTagRequest;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService implements ITagService{

    private final TagRepository repo;
    private final ICourseTagService courseTagService;

    @Override
    public Message<List<Tag>> fetchTageByName(String name) throws EntityNotFoundException, BadRequestException {
        log.info("Fetching all tags like provided name: "+name);
        if (Objects.nonNull(name)) {
            name = "%"+name+"%";
        }else{
            throw new BadRequestException("name cannot be null.");
        }

        List<Tag> tags = repo.findByIsActiveAndNameLike(true, name);
        if (CollectionUtils.isEmpty(tags)) {
            log.error("No tags found.");
            throw new EntityNotFoundException("No tags found.");
        }

        log.info("Successfully fetched all tags.");
        return new Message<List<Tag>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Successfully fetched all tags.")
                .setData(tags);
    }

    private List<Long> deletedTags(List<CreateTagRequest> tags){
        return tags.stream()
                .filter(e -> Objects.nonNull(e.getId()) && !e.getActive())
                .map(CreateTagRequest::getId)
                .toList();
    }

    @Override
    @Transactional
    public void createAllNewAndAlreadyExistsTags(List<CreateTagRequest> tags, Course course) throws BadRequestException {
        log.info("Creating tags provided.");
        if (!CollectionUtils.isEmpty(tags)) {
            try {
                List<Long> deletedTags = deletedTags(tags);
                // Always delete all courses' tags first
                courseTagService.deleteAllCourseTagByTagIds(course.getId());

                if (!CollectionUtils.isEmpty(deletedTags)) {
                    this.deleteTagsByIds(deletedTags);
                }

                List<Tag> existingTags = existingTags(tags);
                List<Tag> newTags = CreateTagRequest.toTag(tags);
                List<Tag> savedTags = new ArrayList<>(repo.saveAll(newTags));

                savedTags.addAll(existingTags);

                List<CourseTag> courseTags = savedTags.stream().map(e -> CourseTag.builder()
                        .tag(e)
                        .course(course)
                        .build()).collect(Collectors.toList());

                courseTagService.createAllCourseTags(courseTags);
                log.info("Created tags for the course.");
            } catch (Exception e) {
                log.error("Some tags can not be saved.");
                throw new BadRequestException("Some tags can not be saved.");
            }
        }
    }

    void deleteTagsByIds(List<Long> deletedTags){
        repo.deleteAllById(deletedTags);
    }

    private List<Tag> existingTags(List<CreateTagRequest> tags){
        return tags.stream()
                .filter(e -> Objects.nonNull(e.getId()) && e.getActive())
                .map(e -> Tag.builder().id(e.getId()).name(e.getName()).isActive(e.getActive()).build())
                .toList();
    }

    @Override
    public List<Tag> findByCourseId(Long courseId) {
        log.info("Fetching all tags for a course.");
        return repo.findByCourseId(courseId);
    }

    @Override
    public Message<List<Tag>> fetchTagsByCourse(Long courseId, String email) throws EntityNotFoundException {
        log.info("Fetching all tags by course id: " + courseId);
        List<Tag> allTags = findByCourseId(courseId);
        if(CollectionUtils.isEmpty(allTags))
            throw new EntityNotFoundException("No tags are found for this course.");
        return new Message<List<Tag>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully fetched all tags.")
                .setData(allTags);
    }
}

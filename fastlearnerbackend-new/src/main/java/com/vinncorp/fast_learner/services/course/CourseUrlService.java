package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.course.CourseUrlRepository;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseUrlService implements ICourseUrlService {
    private final CourseUrlRepository repo;


    @Override
    public CourseUrl findByUrlAndCourseStatuses(String courseUrl, List<CourseStatus> courseStatuses) throws EntityNotFoundException {
       return this.repo.findByUrlAndCourseStatuses(courseUrl, courseStatuses).orElse(null);
    }

    @Override
    public CourseUrl findActiveUrlByCourseIdAndStatus(Long id, GenericStatus status) throws EntityNotFoundException {
        return this.repo.findByCourseIdAndStatus(id, status).orElse(null);
    }

    @Override
    public CourseUrl save(CourseUrl courseUrl) throws InternalServerException {
        try {
            List<CourseUrl> courseUrls = this.repo.findAllByCourseId(courseUrl.getCourse().getId());
            if(courseUrls.size() > 0 && courseUrls.contains(courseUrl)){
                for(CourseUrl cu: courseUrls){
                    if(cu.getUrl().equalsIgnoreCase(courseUrl.getUrl())){
                        cu.setStatus(GenericStatus.ACTIVE);
                    }else {
                        cu.setStatus(GenericStatus.INACTIVE);
                    }
                }
                this.repo.saveAll(courseUrls);
                return courseUrl;
            }else if(courseUrls.size() > 0 && !courseUrls.contains(courseUrl)){
                for(CourseUrl cu: courseUrls){
                    cu.setStatus(GenericStatus.INACTIVE);
                }
                courseUrls.add(courseUrl);
                this.repo.saveAll(courseUrls);
                return courseUrl;
            }else {
                return this.repo.save(courseUrl);
            }
        }catch(Exception e){
            throw new InternalServerException(InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteCourseUrlByCourseId(Long courseId){
        this.repo.deleteAllByCourseId(courseId);
    }
}

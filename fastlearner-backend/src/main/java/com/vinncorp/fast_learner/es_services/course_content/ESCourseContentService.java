package com.vinncorp.fast_learner.es_services.course_content;

import com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto;
import com.vinncorp.fast_learner.es_dto.SearchCourses;
import com.vinncorp.fast_learner.es_models.CourseContent;
import com.vinncorp.fast_learner.es_repository.CustomESCourseContentRepository;
import com.vinncorp.fast_learner.es_repository.ESCourseContentRepository;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.erhlc.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder.Field;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ESCourseContentService implements IESCourseContentService{

    private final CustomESCourseContentRepository customESCourseContentRepository;
    private final ESCourseContentRepository courseContentRepository;

    public ESCourseContentService(CustomESCourseContentRepository customESCourseContentRepository, ESCourseContentRepository courseContentRepository) {
        this.customESCourseContentRepository = customESCourseContentRepository;
        this.courseContentRepository = courseContentRepository;
    }


//    @Override
//    public CourseContent save(CourseContent courseContent) throws IOException {
//        return this.courseContentRepository.save(courseContent);
//    }

    @Override
    public List<CourseContent> save(List<CourseContent> courseContents) throws IOException {
        return (List<CourseContent>) this.courseContentRepository.saveAll(courseContents);
    }

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    public CourseContent getCourseContentById(String id) {
        return elasticsearchTemplate.get(id, CourseContent.class);
    }

    public List<SearchCourses> getCourseTitles(String query, int pageNo, int pageSize) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("status", CourseStatus.PUBLISHED.name()))
                .should(QueryBuilders.multiMatchQuery(query, "title")
                        .fuzziness(Fuzziness.fromEdits(1)))
                .should(QueryBuilders.prefixQuery("title", query));

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withSort(Sort.by(Sort.Order.desc("_score")))
                .withPageable(PageRequest.of(pageNo, pageSize))
                .build();

        SearchHits<CourseContent> searchHits = elasticsearchTemplate.search(searchQuery, CourseContent.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> SearchCourses.builder()
                        .courseId(Long.valueOf(hit.getContent().getId()))
                        .title(hit.getContent().getTitle())
                        .courseUrl(hit.getContent().getCourseUrl())
                        .thumbnail(hit.getContent().getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());
    }



    public List<InstructorProfileSearchDto> getInstructorProfiles(String query, int pageNo, int pageSize) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("status", CourseStatus.PUBLISHED.name()))
                .should(QueryBuilders.multiMatchQuery(query, "creatorName")
                        .fuzziness(Fuzziness.fromEdits(1)));

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(pageNo, pageSize))
                .build();

        SearchHits<CourseContent> searchHits = elasticsearchTemplate.search(searchQuery, CourseContent.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> InstructorProfileSearchDto.builder()
                        .fullName(hit.getContent().getCreatorName())
                        .profilePicture(hit.getContent().getUserPictureUrl())
                        .profileUrl("user/profile?url=" + hit.getContent().getUserProfileUrl())
                        .build())
                .filter(distinctByKey(InstructorProfileSearchDto::getProfileUrl))
                .collect(Collectors.toList());

    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public List<CourseContent> getCoursesByCreatedBy(Long createdById) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("createdBy", createdById))
                .build();

        SearchHits<CourseContent> searchHits = elasticsearchTemplate.search(searchQuery, CourseContent.class);

        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    public SearchPage<CourseContent> searchCourses(String searchTerm, int pageNo, int pageSize) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("status", CourseStatus.PUBLISHED.name()))
                .minimumShouldMatch(1)
                .should(QueryBuilders.multiMatchQuery(searchTerm,
                                "title",
                                "outcome",
                                "description",
                                "tags",
                                "creatorName")
                        .fuzziness(Fuzziness.fromEdits(1)))
                .should(QueryBuilders.nestedQuery("sections",
                                QueryBuilders.boolQuery()
//                                        .must(QueryBuilders.termQuery("sections.status", true))
                                        .should(QueryBuilders.matchQuery("sections.name", searchTerm).fuzziness(Fuzziness.fromEdits(1))),
                                ScoreMode.Max)
                        .innerHit(new InnerHitBuilder()
                                .setName("highlightedSections")
                                .setSize(5)
                                .setHighlightBuilder(new HighlightBuilder()
                                        .field("sections.name")
                                        .preTags("<b>").postTags("</b>")
                                )

                        )
                )
                .should(QueryBuilders.nestedQuery("sections.topics",
                                QueryBuilders.boolQuery()
//                                        .must(QueryBuilders.termQuery("sections.status", true))
//                                        .must(QueryBuilders.termQuery("sections.topics.status", true))
                                        .should(QueryBuilders.matchQuery("sections.topics.name", searchTerm).fuzziness(Fuzziness.fromEdits(0))),
                                ScoreMode.Max)
                        .innerHit(new InnerHitBuilder()
                                .setName("highlightedTopics")
                                .setSize(5)
                                .setHighlightBuilder(new HighlightBuilder()
                                        .field("sections.topics.name")
                                        .preTags("<b>").postTags("</b>")
                                )

                        )
                );

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")
                .field("outcome")
                .field("description")
                .field("tags")
                .field("creatorName")
                .preTags("<b>")
                .postTags("</b>");

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withHighlightBuilder(highlightBuilder)
                .withPageable(PageRequest.of(pageNo, pageSize))
                .withTrackTotalHits(true)
                .build();

        SearchHits<CourseContent> searchHits = elasticsearchTemplate.search(searchQuery, CourseContent.class);
        return SearchHitSupport.searchPageFor(searchHits, PageRequest.of(pageNo, pageSize));
    }
}

package com.vinncorp.fast_learner.mock.topic;

import com.vinncorp.fast_learner.mock.section.SectionTestData;
import com.vinncorp.fast_learner.models.topic.Topic;

import java.io.IOException;

public class TopicTestData {

    public static Topic topicData() throws IOException {
        var section = SectionTestData.sectionData();
        return Topic.builder()
                .id(1L)
                .name("Sample Topic")
                .durationInSec(200)
                .section(section)
                .build();
    }
}

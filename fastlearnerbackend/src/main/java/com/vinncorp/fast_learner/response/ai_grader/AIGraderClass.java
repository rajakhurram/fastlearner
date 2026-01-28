package com.vinncorp.fast_learner.response.ai_grader;

import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
@ToString

public class AIGraderClass {

    private Long id;
    private String name;
    private String description;
    private Long instructorId;
}

package com.vinncorp.fast_learner.request.tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.models.tag.Tag;
import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTagRequest {
    private Long id;
    private String name;
    private Boolean active;

    public static List<Tag> toTag(List<CreateTagRequest> tagRequests) {
        return tagRequests.stream().filter(e -> Objects.isNull(e.getId())).map(e -> Tag.builder()
                .name(e.getName())
                .isActive(!Objects.isNull(e.getActive()) && e.getActive())
                .build()).collect(Collectors.toList());
    }

    public static List<CreateTagRequest> toCreateTagRequest(List<Tag> tags) {
        return tags.stream()
                .map(tag ->
                        CreateTagRequest
                                .builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .active(tag.isActive())
                                .build()).toList();
    }
}

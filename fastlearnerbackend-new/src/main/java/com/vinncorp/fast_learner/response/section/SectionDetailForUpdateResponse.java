package com.vinncorp.fast_learner.response.section;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionDetailForUpdateResponse {

    private Long sectionId;
    private String sectionName;
    private int sequence;
    private boolean isFree;


    public static List<SectionDetailForUpdateResponse> from(List<Tuple> data) {
        return data.stream().map(e -> SectionDetailForUpdateResponse.builder()
                .sectionId(Long.parseLong("" + e.get("id")))
                .sectionName((String) e.get("name"))
                .sequence(Integer.parseInt("" + e.get("sequence")))
                .isFree(Boolean.parseBoolean("" + e.get("isFree")))
                .build()).toList();
    }
}

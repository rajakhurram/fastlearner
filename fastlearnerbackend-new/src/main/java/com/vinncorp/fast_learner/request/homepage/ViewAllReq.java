package com.vinncorp.fast_learner.request.homepage;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ViewAllReq {

    private List<Long> categoriesId;
    private String courseType;
    private String feature;
    private Long rating;
    private String contentType;
    private String search;
    private int pageNo;
    private int pageSize;


    @Override
    public String toString() {
        return "ViewAllReq{" +
                "categoriesId=" + categoriesId +
                ", courseType='" + courseType + '\'' +
                ", feature='" + feature + '\'' +
                ", rating=" + rating +
                '}';
    }
}

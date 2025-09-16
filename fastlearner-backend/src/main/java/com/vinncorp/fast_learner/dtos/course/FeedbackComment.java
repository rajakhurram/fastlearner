package com.vinncorp.fast_learner.dtos.course;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackComment {

    private Long reviewId;
    private String comment;
    private String userName;
    private double rating;
    private String createdAt;
    private int likes;
    private int dislikes;
    private String profileImage;
    private String userProfileUrl;


    public static List<FeedbackComment> from(List<Tuple> listOfReview) {
        return listOfReview.stream().map(e -> {
            FeedbackComment.FeedbackCommentBuilder builder = FeedbackComment.builder()
                    .comment((String) e.get("comment"))
                    .rating((Double) e.get("rating"))
                    .userName((String) e.get("full_name"))
                    .likes(e.get("likes") != null ? Integer.parseInt("" + e.get("likes")) : 0)
                    .dislikes(e.get("dislikes") != null ? Integer.parseInt("" + e.get("dislikes")) : 0)
                    .reviewId((Long) e.get("review_id"))
                    .createdAt(e.get("created_date") != null ? "" + e.get("created_date") : null)
                    .profileImage((String) e.get("profile_picture"));
            if (e.getElements().stream().anyMatch(column -> "profile_url".equals(column.getAlias()))) {
                builder.userProfileUrl((String) e.get("profile_url"));
            }

            return builder.build();
        }).toList();
    }


    public static FeedbackComment from(Tuple review) {
        return FeedbackComment
                .builder()
                .comment((String) review.get("comment"))
                .rating((Double) review.get("rating"))
                .userName((String) review.get("full_name"))
                .likes(review.get("likes") != null ? Integer.parseInt("" + review.get("likes")) : 0)
                .dislikes(review.get("dislikes") != null ? Integer.parseInt("" + review.get("dislikes")) : 0)
                .reviewId((Long) review.get("review_id"))
                .createdAt(review.get("created_date") != null ? "" + review.get("created_date") : null )
                .profileImage((String) review.get("profile_picture"))
                .build();
    }

}

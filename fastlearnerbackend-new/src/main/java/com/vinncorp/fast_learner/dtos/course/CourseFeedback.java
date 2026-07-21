package com.vinncorp.fast_learner.dtos.course;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseFeedback {

    private double rating1;
    private double rating2;
    private double rating3;
    private double rating4;
    private double rating5;
    private List<FeedbackComment> feedbackComments;
    private double totalUsers;
    private double totalReview;

    public static CourseFeedback toOnlyRating(List<Tuple> tuples, long totalReviewers) {
        CourseFeedback d = new CourseFeedback();
        double reviews = 0;
        double users = 0;
        List<Integer> roundedPercentages = new ArrayList<>();
        int totalPercentage = 0;

        // Step 1: Calculate and round percentages
        for (Tuple tuple : tuples) {
            double ratingNo = Double.parseDouble("" + tuple.get("rating"));
            double noOfUser = Double.parseDouble("" + tuple.get("users"));
            reviews += ratingNo * noOfUser;
            users += noOfUser;

            double ratingPercentage = noOfUser <= 0 ? 0 : (100 * (noOfUser / totalReviewers));
            int roundedPercentage = (int) Math.round(ratingPercentage);
            totalPercentage += roundedPercentage;
            roundedPercentages.add(roundedPercentage);

            switch ((int) ratingNo) {
                case 1:
                    d.setRating1(roundedPercentage);
                    break;
                case 2:
                    d.setRating2(roundedPercentage);
                    break;
                case 3:
                    d.setRating3(roundedPercentage);
                    break;
                case 4:
                    d.setRating4(roundedPercentage);
                    break;
                case 5:
                    d.setRating5(roundedPercentage);
                    break;
            }
        }

        // Step 2: Adjust to ensure total equals 100%
        int difference = 100 - totalPercentage;

        if (difference != 0) {
            for (int i = 0; i < roundedPercentages.size(); i++) {
                // Adjust one of the percentages to account for the difference
                if (difference > 0) {
                    roundedPercentages.set(i, roundedPercentages.get(i) + 1);
                    difference--;
                } else if (difference < 0) {
                    if (roundedPercentages.get(i) > 0) { // Avoid negative percentages
                        roundedPercentages.set(i, roundedPercentages.get(i) - 1);
                        difference++;
                    }
                }
                if (difference == 0) break;
            }
        }

        // Step 3: Reassign adjusted percentages
        for (int i = 0; i < tuples.size(); i++) {
            Tuple tuple = tuples.get(i);
            int adjustedPercentage = roundedPercentages.get(i);

            switch ((int) Double.parseDouble("" + tuple.get("rating"))) {
                case 1:
                    d.setRating1(adjustedPercentage);
                    break;
                case 2:
                    d.setRating2(adjustedPercentage);
                    break;
                case 3:
                    d.setRating3(adjustedPercentage);
                    break;
                case 4:
                    d.setRating4(adjustedPercentage);
                    break;
                case 5:
                    d.setRating5(adjustedPercentage);
                    break;
            }
        }
        d.setTotalReview(reviews/users);
        d.setTotalUsers(users);
        return d;
    }
}

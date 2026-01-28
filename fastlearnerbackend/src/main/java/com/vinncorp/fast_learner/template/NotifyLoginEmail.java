package com.vinncorp.fast_learner.template;

import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotifyLoginEmail {

    @Value("${login.url}")
    private static String LOGIN_URL;

    public static String loginEmailTemplate(String fullName, Date lastLoginDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String formattedDate = dateFormat.format(lastLoginDate);
        String body = "<html>" +
                "<body>" +
                "<p>Hi <strong>" + fullName + "</strong>,</p>" +
                "<p>We’ve noticed you haven’t logged into FastLearner since <strong>" + formattedDate + "</strong>, and we’d love to see you back! " +
                "Whether you’re looking to pick up where you left off or start something new, your learning progress is our top priority.</p>" +
                "<p>FastLearner is packed with fresh and exciting content, and we’re constantly adding more to keep your learning experience dynamic and rewarding. " +
                "This is the perfect opportunity to jump back in and explore!</p>" +
                "<p><strong>Why now?</strong></p>" +
                "<ul>" +
                "<li><strong>Stay ahead of the curve:</strong> New skills mean new opportunities.</li>" +
                "<li><strong>Flexible learning:</strong> Continue at your own pace, whenever it works for you.</li>" +
                "<li><strong>Achieve your goals:</strong> Every lesson gets you closer to mastering something amazing.</li>" +
                "</ul>" +
                "<p>Let’s get you back on track! Log in today and reignite your learning journey. We can’t wait to see what you’ll accomplish next!</p>" +
                "<p><a href='" + LOGIN_URL + "' style='padding: 10px 15px; background-color: #007bff; color: #fff; text-decoration: none; border-radius: 5px;'>Log in</a></p>" +
                "<p>Best,<br/>The FastLearner Team</p>" +
                "</body>" +
                "</html>";
       return body;
    }


}

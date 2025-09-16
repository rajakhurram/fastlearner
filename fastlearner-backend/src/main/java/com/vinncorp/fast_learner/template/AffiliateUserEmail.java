package com.vinncorp.fast_learner.template;

public class AffiliateUserEmail {

    public static final String AFFILIATE_WELCOME_TEMPLATE = "" +
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Welcome to FastLearner Affiliate Program</title>\n" +
            "    <style>\n" +
            "        @media screen and (max-width: 768px) {\n" +
            "            .mobile-responsive {\n" +
            "                width: 80% !important;\n" +
            "            }\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div style=\"background-color: #F4F6FB; padding: 20px; border: 1px solid #ddd;\">\n" +
            "        <div style=\"text-align: center; margin-bottom: 20px;\">\n" +
            "            <img style=\"height: 32px;\" src=\"https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/o1Ua722u_fast-learner-logo.png\" alt=\"FastLearner Logo\">\n" +
            "        </div>\n" +
            "        <div class=\"mobile-responsive\" style=\"background-color: #fff; padding: 20px; margin: 0 auto; max-width: 600px; border-radius: 8px;\">\n" +
            "            <h1 style=\"font-size: 24px; color: #333; font-family: sans-serif;\">Hello [Affiliate Name],</h1>\n" +
            "            <p style=\"font-size: 14px; color: #555; font-family: sans-serif;\">Welcome to the FastLearner Affiliate Program!</p>\n" +
            "            <p style=\"font-size: 14px; color: #555; font-family: sans-serif;\">We’re excited to have you join us as an affiliate, where you'll be helping learners connect with premium courses and earning rewards as you do. [Instructor Name] has personally invited you to become part of this journey. Here’s what you can expect:</p>\n" +
            "            <h3 style=\"font-size: 18px; color: #333; font-family: sans-serif;\">How to Get Started:</h3>\n" +
            "            <ol style=\"font-size: 14px; color: #555; font-family: sans-serif;\">\n" +
            "                <li>Create Your Stripe Account: To start earning rewards, please create your Stripe account by following this link: <a href=\"[Stripe Onboarding Link]\" style=\"color: #1A73E8;\">Stripe Onboarding Link</a>. This will ensure you receive earnings from every successful referral.</li>\n" +
            "                <li>Track Your Unique Link: After setting up your account, we’ll send you a unique course link that you can use for referrals and marketing. Every successful purchase made through this link will contribute to your rewards.</li>\n" +
            "            </ol>\n" +
            "            <h3 style=\"font-size: 18px; color: #333; font-family: sans-serif;\">What You’ll Gain:</h3>\n" +
            "            <p style=\"font-size: 14px; color: #555; font-family: sans-serif;\">For every premium course purchased through your unique link, you’ll earn a percentage-based reward that’s tracked and disbursed to you monthly.</p>\n" +
            "            <p style=\"font-size: 14px; color: #555; font-family: sans-serif;\">If you have any questions or need help getting set up, please feel free to reach out to us at <a href=\"mailto:[Support Email]\" style=\"color: #1A73E8;\">[Support Email]</a>.</p>\n" +
            "            <p style=\"font-size: 14px; color: #555; font-family: sans-serif;\">Thank you for joining us, [Affiliate Name]. We look forward to seeing your success!</p>\n" +
            "            <p style=\"font-size: 14px; color: #555; font-family: sans-serif;\">Warm regards,<br>The FastLearner Team</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";



    public static String createAffiliateWithStripeUrl(String userName,String stripeAccountUrl) {
        String body = "<html>" +
                "<body>" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td align=\"center\">" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border: 1px solid #dddddd; padding: 20px; background-color: #ffffff;\">" +
                "<tr>" +
                "<td>" +
                "<p>Hi <strong>" + userName + "</strong>,</p>" +
                "<p>We’re excited to let you know that you can now become an affiliate with FastLearner! As an affiliate, you’ll earn rewards for every successful referral.</p>" +
                "<p>To get started, please set up your Stripe account using the link below:</p>" +
                "<p><a href=\"" + stripeAccountUrl + "\" style=\"color: #007bff; text-decoration: none;\">Set Up Stripe Account</a></p>" +
                "<p>Once your account is set up, you’ll be able to manage and track your affiliate rewards seamlessly.</p>" +
                "<p>We’re thrilled to have you onboard and can’t wait to see the impact you’ll make as an affiliate!</p>" +
                "<p>Best regards,<br>" +
                "The FastLearner Team</p>" +
                "<p>Need help? Contact us at <a href=\"mailto:support@fastlearner.ai\" style=\"color: #007bff; text-decoration: none;\">support@fastlearner.ai</a>.</p>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</body>" +
                "</html>";
        return body;

    }

    public static String createAffiliateWithoutStripeUrl(String userName) {

        String body = "<html>" +
                "<body>" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td align=\"center\">" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border: 1px solid #dddddd; padding: 20px; background-color: #ffffff;\">" +
                "<tr>" +
                "<td>" +
                "<p>Hi <strong>" + userName + "</strong>,</p>" +
                "<p>We’re thrilled to inform you that you’ve been successfully added as an affiliate with FastLearner! As an affiliate, you have the opportunity to earn rewards for every successful referral you make.</p>" +
                "<p>You can now start sharing your affiliate links and help others discover the amazing learning opportunities on FastLearner while earning rewards for yourself.</p>" +
                "<p>Here are some tips to make the most out of your affiliate journey:</p>" +
                "<ul>" +
                "<li>Share your unique affiliate link with friends, family, and on social media.</li>" +
                "<li>Encourage others to join FastLearner and explore our premium courses.</li>" +
                "<li>Track your rewards and referrals in your affiliate dashboard.</li>" +
                "</ul>" +
                "<p>We’re excited to have you as part of the FastLearner Affiliate Program, and we can’t wait to see the impact you’ll make!</p>" +
                "<p>If you have any questions or need assistance, feel free to reach out to us at <a href=\"mailto:support@fastlearner.ai\" style=\"color: #007bff; text-decoration: none;\">support@fastlearner.ai</a>.</p>" +
                "<p>Best regards,<br>" +
                "The FastLearner Team</p>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</body>" +
                "</html>";
        return body;
    }
}

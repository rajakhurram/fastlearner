package com.vinncorp.fast_learner.util.Constants;

public class EmailTemplate {

    public static final String FIRST_PART_RESET_PASSWORD_TEMPLATE = "" +
            "<!DOCTYPE html>\n" +
            "            <html lang=\"en\">\n" +
            "            <head>\n" +
            "                <meta charset=\"UTF-8\">\n" +
            "                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "                <title>Registration OTP</title>\n" +
            "<style>\n" +
            "        @media screen and (max-width: 768px) {\n" +
            "            .mobile-responsive {\n" +
            "                width: 80% !important;\n" +
            "            }\n" +
            "        }\n" +
            "    </style>"+
            "            </head>\n" +
            "            <body>\n" +
            "                <div style=\"background-color: #F4F6FB !important; border: 1px solid #F4F6FB !important;\">\n" +
            "                    <div style=\"margin-top: 3%; text-align: center;\">\n" +
            "                        <img style=\"height: 32px;\" src=\"https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/o1Ua722u_fast-learner-logo.png\">\n" +
            "                    </div>\n" +
            "                    <div style=\"margin-top: 2%; text-align: center;\">\n" +
            "                        <div class=\"mobile-responsive\" style=\"width: 30%; background-color: white; margin: 0 auto;\">\n" +
            "                            <div style=\"color: black; font-size: 20px; font-weight: 600; margin-top: 10%; font-family: sans-serif; padding-top: 25px;\">\n" +
            "                                Reset Password\n" +
            "                            </div>\n" +
            "                            <div style=\"color: black; font-size: 12px; margin-left: 5%; margin-right: 5%; margin-top: 5%; font-family: sans-serif; text-align: start\">\n" +
            "                                <p>You have just requested a password reset for the FastLearner</p>\n" +
            "                                <p>account associated with this email address.</p>\n" +
            "                            </div>\n" +
            "                            <div style=\"margin-left: 22%; margin-right: 22%; background-color: #FE4A55;\n" +
            "                            border-radius: 4px;; height: 45px; border-radius: 4px; font-weight: 600; margin-top: 4%; font-family: sans-serif;\">\n" +
            "                                <a href=\"";
    public static final String LAST_PART_RESET_PASSWORD_TEMPLATE = "\" style=\"text-decoration: none\"><div style=\"line-height: 45px; color: white;\">Reset Password</div></a>\n" +
            "                            </div>\n" +
            "                            <hr style=\"margin-left: 5%; margin-right: 5%; margin-top: 5%;\">\n" +
            "                            <div style=\"margin-left: 5%; margin-right: 5%; font-size: 12px; font-family: sans-serif;  padding-bottom: 25px; text-align: start\">\n" +
            "                                <p>If you continue to have issues signing in,</p>\n" +
            "                                <p>please <a href=\"https://fastlearner.ai/contact-us\">contact support</a>. Thank you for using FastLearner!</p>\n" +
            "                            </div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                    <div style=\"text-align: center; margin: 2%;\">\n" +
            "                        <a href=\"https://www.facebook.com/fastlearnerGPT/\" target=\"_blank\">\n" +
            "                            <img src=\"https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/YVU7hjK7_Fb_Icon.svg\" style=\"height: 20px;\">\n" +
            "                        </a>\n" +
            "                        <a href=\"https://www.linkedin.com/company/fastlearner/\" target=\"_blank\" style=\"margin-left: 30px;\">\n" +
            "                            <img src=\"https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/SqEq8Wyi_LInk_icon.svg\" style=\"height: 20px;\">\n" +
            "                        </a>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </body>\n" +
            "            </html>";

    public static final String FIRST_PART_OTP_EMAIL_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Registration OTP</title>\n" +
            "<style>\n" +
            "        @media screen and (max-width: 768px) {\n" +
            "            .mobile-responsive {\n" +
            "                width: 80% !important;\n" +
            "            }\n" +
            "        }\n" +
            "    </style>"+
            "</head>\n" +
            "<body>\n" +
            "    <div style=\"background-color: #f4f6fb !important; border: 1px solid #f4f6fb !important;\">\n" +
            "\n" +
            "        <div style=\"margin-top: 3%; text-align: center;\">\n" +
            "            <img style=\"height: 32px;\" src=\"https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/o1Ua722u_fast-learner-logo.png\">\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"margin-top: 2%; text-align: center;\"> \n" +
            "            <div class=\"mobile-responsive\" style=\"width: 30%; background-color: white; margin: 0 auto;\">\n" +
            "                <div style=\"color: black; font-size: 20px; font-weight: 600; margin-top: 10%; font-family: sans-serif; padding-top: 25px;\">\n" +
            "                    Email Verification\n" +
            "                </div>\n" +
            "                <div style=\"color: black; font-size: 12px; margin-left: 5%; margin-right: 5%; margin-top: 5%; font-family: sans-serif; text-align: justify\">\n" +
            "                    <p>Here is the verification code. Please copy it and verify your email.</p>\n" +
            "                    <p>OTP (one-time password) will expire in 1 minute.</p>\n" +
            "                </div>\n" +
            "                <div style=\"margin-left: 5%; margin-right: 5%; background-color: #d2e0ff; height: 45px; border-radius: 4px; font-weight: 600; margin-top: 4%; font-family: sans-serif;\">\n" +
            "                    <div style=\"line-height: 45px;\">Code: ";
    public static final String SECOND_PART_OTP_EMAIL_TEMPLATE = "\n" +
            "</div>\n" +
            "                </div>\n" +
            "                <hr style=\"margin-left: 5%; margin-right: 5%; margin-top: 5%;\">\n" +
            "                <div style=\"margin-left: 5%; margin-right: 5%; font-size: 12px; font-family: sans-serif;  padding-bottom: 25px; text-align: justify\">\n" +
            "                    <p>If you continue to have issues signing in,</p>\n" +
            "                    <p>please <a href=\"https://fastlearner.ai/contact-us\">contact support</a>. Thank you for using FastLearner!</p>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"text-align: center; margin: 2%;\">\n" +
            "            <a href=\"https://www.facebook.com/fastlearnerGPT/\" target=\"_blank\">\n" +
            "                <img src=\"https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/CACyWf0U_fb.png\" style=\"height: 20px;\">\n" +
            "            </a>\n" +
            "            <a href=\"https://www.linkedin.com/company/fastlearner/\" target=\"_blank\" style=\"margin-left: 30px;\">\n" +
            "                <img src=\"https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/hO6lgLEA_linkedin.png\" style=\"height: 20px;\">\n" +
            "            </a>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";


    public static final String FIRST_PART_OTP_RESET_EMAIL_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Registration OTP</title>\n" +
            "<style>\n" +
            "        @media screen and (max-width: 768px) {\n" +
            "            .mobile-responsive {\n" +
            "                width: 80% !important;\n" +
            "            }\n" +
            "        }\n" +
            "    </style>"+
            "</head>\n" +
            "<body>\n" +
            "    <div style=\"background-color: #f4f6fb !important; border: 1px solid #f4f6fb !important;\">\n" +
            "\n" +
            "        <div style=\"margin-top: 3%; text-align: center;\">\n" +
            "            <img style=\"height: 32px;\" src=\"https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/o1Ua722u_fast-learner-logo.png\">\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"margin-top: 2%; text-align: center;\"> \n" +
            "            <div class=\"mobile-responsive\" style=\"width: 30%; background-color: white; margin: 0 auto;\">\n" +
            "                <div style=\"color: black; font-size: 20px; font-weight: 600; margin-top: 10%; font-family: sans-serif; padding-top: 25px;\">\n" +
            "                    Reset Password\n" +
            "                </div>\n" +
            "                <div style=\"color: black; font-size: 12px; margin-left: 5%; margin-right: 5%; margin-top: 5%; font-family: sans-serif; text-align: justify\">\n" +
            "                    <p>Here is the verification code. Please copy it and verify your email.</p>\n" +
            "                    <p>OTP (one-time password) will expire in 1 minute.</p>\n" +
            "                </div>\n" +
            "                <div style=\"margin-left: 5%; margin-right: 5%; background-color: #d2e0ff; height: 45px; border-radius: 4px; font-weight: 600; margin-top: 4%; font-family: sans-serif;\">\n" +
            "                    <div style=\"line-height: 45px;\">Code: ";



}



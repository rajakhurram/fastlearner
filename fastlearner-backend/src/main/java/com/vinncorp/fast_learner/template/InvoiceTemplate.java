package com.vinncorp.fast_learner.template;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InvoiceTemplate {

    public static String generateInvoiceTemplate(String courseName,String email, String fullName,
                                                 double subscriptionAmount,
                                                 Double discount,
                                                 Double finalAmount,
                                                 Date creationDate,
                                                 String paymentId,
                                                 boolean isPlan) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        String formattedDate = dateFormat.format(creationDate);

        return  "<html>" +
                "<body>" +
                "<div style=\"width: 600px; margin: auto; font-family: Arial, sans-serif; border: 1px solid #ddd; border-radius: 10px; padding: 20px;\">" +
                "  <div style=\"text-align: left; display: inline-block; vertical-align: middle;\">" +
                "  <img style=\"height: 82px; vertical-align: middle; margin-right: 40px; \n\" src=\"https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/AaxopLD7_image_(1).png\" alt=\"FastLearner Logo\" />\n" +
                " " +
                "    <p style=\"color: #C1C1C1; text-align: right;   line-height: 26px;display: inline-block; vertical-align: middle;\n\">\n" +
                "    FastLearner, Inc.                <br/>10100 Robert Watkins Way<br/>Elk Grove, California 95757\n" +
                "</p>\n" +
                "  </div>" +
                "  <hr style=\"border: 0; height: 1px; background: #ddd; margin: 20px 0;\" />" +
                "  <div style=\"text-align: left; margin-bottom: 20px;\">" +
                "    <h3 style=\"color: #324EB5; font-size: 20px; margin: 0;\">Receipt From FastLearner</h3>" +
                "    <p style=\"color: #292929; font-size: 14px;  line-height: 26px;\n\">Your transaction is completed and processed securely.<br/>Please retain this copy for your records.</p>" +
                "  </div>" +
                "  <hr style=\"border: 0; height: 1px; background: #ddd; margin: 20px 0;\" />" +
                "  <div style=\"margin-bottom: 20px;\">" +
                "    <h4 style=\"color: #324EB5; font-size: 18px; margin-bottom: 10px;\">Transaction</h4>" +
                "    <table style=\"width: 100%; border-collapse: collapse;\">" +
                "<tr><td style=\"color: #707070; font-size: 14px; padding: 5px 0;\">" +
                (isPlan ? "Plan Type" : "Course name") + "</td>" +
                "<td style=\"text-align: right; font-size: 14px; font-weight: bold;\">" + courseName + "</td></tr>" +
                "      <tr><td style=\"color: #707070; font-size: 14px; padding: 5px 0;\">Price</td><td style=\"text-align: right; font-size: 14px; font-weight: bold;\">$" + String.format("%.2f", subscriptionAmount) + "</td></tr>" +
                "      <tr><td style=\"color: #707070; font-size: 14px; padding: 5px 0;\">Discount</td><td style=\"text-align: right; font-size: 14px; font-weight: bold;\">$" + String.format("%.2f", discount) + "</td></tr>" +
                "      <tr><td style=\"color: #707070; font-size: 14px; padding: 5px 0;\">Total</td><td style=\"text-align: right; font-size: 14px; font-weight: bold;\">$" + String.format("%.2f", finalAmount) + "</td></tr>" +
                "      <tr><td style=\"color: #707070; font-size: 14px; padding: 5px 0;\">Purchased date</td><td style=\"text-align: right; font-size: 14px; font-weight: bold;\">" + formattedDate + "</td></tr>" +
                "    </table>" +
                "  </div>" +
                "  <div>" +
                "    <h4 style=\"color: #324EB5; font-size: 18px; margin-bottom: 10px;\">Customer</h4>" +
                "    <table style=\"width: 100%; border-collapse: collapse;\">" +
                "      <tr><td style=\"color: #707070; font-size: 14px; padding: 5px 0;\">Name</td><td style=\"text-align: right; font-size: 14px; font-weight: bold;\">" + fullName + "</td></tr>" +
                "      <tr><td style=\"color: #707070; font-size: 14px; padding: 5px 0;\">Email</td><td style=\"text-align: right; font-size: 14px; font-weight: bold;\">" + email + "</td></tr>" +
                "    </table>" +
                "  </div>" +
                "  <div style=\"text-align: center; color: #6c757d; font-size: 12px;\">" +
                "  </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}

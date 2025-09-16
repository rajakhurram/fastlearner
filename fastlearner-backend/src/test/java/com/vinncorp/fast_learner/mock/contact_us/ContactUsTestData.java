package com.vinncorp.fast_learner.mock.contact_us;

import com.vinncorp.fast_learner.request.contact_us.ContactUsRequest;

public class ContactUsTestData {

    public static ContactUsRequest getContactUsRequest() {
        return ContactUsRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .description("Need assistance with my account.")
                .build();
    }
}

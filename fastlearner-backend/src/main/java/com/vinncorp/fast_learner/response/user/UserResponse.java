package com.vinncorp.fast_learner.response.user;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class UserResponse {
    private String email;
    private String name;
}

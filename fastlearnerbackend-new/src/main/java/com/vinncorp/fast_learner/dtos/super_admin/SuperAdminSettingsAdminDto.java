package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminSettingsAdminDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;       // Active / Inactive
    private Date lastLogin;
}

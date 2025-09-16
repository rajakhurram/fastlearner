package com.vinncorp.fast_learner.models.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String stripeAccountId;
    private Integer age;

    @JsonIgnore
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private AuthProvider provider;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date creationDate;

    @Column(name = "login_timestamp",columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date loginTimestamp;

    private boolean isSubscribed = false;

    @Column(name = "sales_raise", precision = 2, scale = 1, nullable = false, columnDefinition = "NUMERIC(2,1) DEFAULT 1")
    private Double salesRaise;

    private boolean isActive = false;
    @Past(message = "Date of birth must be in the past")

    private LocalDate dob;
}

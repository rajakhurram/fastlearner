package com.vinncorp.fast_learner.models.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinncorp.fast_learner.models.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails extends Auditable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;
    private boolean welcomeInstructorDashboard;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date creationDate;

    @Column(name = "login_timestamp",columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date loginTimestamp;

}

package com.vinncorp.fast_learner.models.affiliate;

import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "instructor_affiliate")
public class InstructorAffiliate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @Column(name = "username")
    private String username;

    @ManyToOne
    @JoinColumn(name = "affiliate_user")
    private Affiliate affiliateUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "last_mod_date")
    @Temporal(TIMESTAMP)
    protected Date lastModifiedDate;

    @Column(name = "defaultReward",nullable = false)
    private Double defaultReward;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "affiliate_uuid", unique = true, nullable = false)
    private String affiliateUuid;
    @Enumerated(value = EnumType.STRING)
    private GenericStatus status ;
}

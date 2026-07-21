package com.vinncorp.fast_learner.models.subscription;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscribed_user_profile")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SubscribedUserProfile {

    @Id
    @Column(name = "id",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_payment_profile_id",unique = true)
    private String customerPaymentProfileId;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "customer_payment_id")
    private String customerPaymentId;

    @ManyToOne
    @JoinColumn(name = "subscribed_user_id",referencedColumnName = "id", nullable = false)
    private SubscribedUser subscribedUser;

}

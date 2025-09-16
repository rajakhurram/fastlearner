package com.vinncorp.fast_learner.models.token;

import com.vinncorp.fast_learner.models.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "token")
@Entity
public class Token extends Auditable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "status")
    private boolean status = true;

}

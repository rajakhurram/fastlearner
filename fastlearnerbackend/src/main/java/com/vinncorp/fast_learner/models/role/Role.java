package com.vinncorp.fast_learner.models.role;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role")
@Getter
@Setter
@Data
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id", unique=true, nullable=false)
    private Long id;

    private String type;

}
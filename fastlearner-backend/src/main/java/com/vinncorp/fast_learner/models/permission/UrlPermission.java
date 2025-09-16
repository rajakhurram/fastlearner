package com.vinncorp.fast_learner.models.permission;

import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"url", "method"}))
public class UrlPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private String method;
    @Enumerated(value = EnumType.STRING)
    private GenericStatus status;
}

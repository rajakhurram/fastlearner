package com.vinncorp.fast_learner.models.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.Date;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Data
@EqualsAndHashCode
@ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<T> {

    @Column(name = "created_date", updatable = false)
    @Temporal(TIMESTAMP)
    @CreatedDate
    protected Date creationDate;


    @Column(name = "last_mod_date")
    @LastModifiedDate
    @Temporal(TIMESTAMP)
    protected Date lastModifiedDate;

    @CreatedBy
    @Column(name="created_by")
    protected T createdBy;

    @LastModifiedBy
    @Column(name="modified_by")
    protected T modifiedBy;

}

package com.vinncorp.fast_learner.repositories.certificate;

import com.vinncorp.fast_learner.models.certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByUuid(String uuid);
    Certificate findByCourseIdAndStudentId(Long courseId, Long id);
}

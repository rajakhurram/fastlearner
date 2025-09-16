package com.vinncorp.fast_learner.repositories.role;

import com.vinncorp.fast_learner.models.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByType(String roleType);
}

package com.vinncorp.fast_learner.repositories.api_client;

import com.vinncorp.fast_learner.models.api_client.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {
    ApiClient findByClientId(String clientId);
}

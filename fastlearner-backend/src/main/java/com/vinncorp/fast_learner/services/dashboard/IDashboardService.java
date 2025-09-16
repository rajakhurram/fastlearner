package com.vinncorp.fast_learner.services.dashboard;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.dashboard.DashboardStatsResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IDashboardService {
    Message<DashboardStatsResponse> fetchStats(String filterBy, String email) throws EntityNotFoundException, BadRequestException;
}

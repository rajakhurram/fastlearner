package com.vinncorp.fast_learner.controllers.dashboard;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.dashboard.DashboardStatsResponse;
import com.vinncorp.fast_learner.services.dashboard.IDashboardService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.DASHBOARD)
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService service;

    @GetMapping(APIUrls.FETCH_DASHBOARD_STATS)
    public ResponseEntity<Message<DashboardStatsResponse>> fetchStats(@RequestParam String filterBy, Principal principal)
            throws EntityNotFoundException, BadRequestException {
        var m = service.fetchStats(filterBy, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}

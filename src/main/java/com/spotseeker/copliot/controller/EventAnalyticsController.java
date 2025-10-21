package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.service.EventAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events/{id}")
@RequiredArgsConstructor
public class EventAnalyticsController {

    private final EventAnalyticsService eventAnalyticsService;

    @GetMapping("/overview")
    public ResponseEntity<EventOverviewDto> getEventOverview(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getName());
        EventOverviewDto overview = eventAnalyticsService.getEventOverview(userId, id);
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/live-stats")
    public ResponseEntity<LiveStatsDto> getLiveStats(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getName());
        LiveStatsDto stats = eventAnalyticsService.getLiveStats(userId, id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/finance/sales")
    public ResponseEntity<FinanceSalesDto> getFinanceSales(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "all") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = Long.parseLong(authentication.getName());
        FinanceSalesDto sales = eventAnalyticsService.getFinanceSales(userId, id, period);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/finance/breakdown")
    public ResponseEntity<FinanceBreakdownDto> getFinanceBreakdown(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getName());
        FinanceBreakdownDto breakdown = eventAnalyticsService.getFinanceBreakdown(userId, id);
        return ResponseEntity.ok(breakdown);
    }
}


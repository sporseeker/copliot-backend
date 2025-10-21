package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceBreakdownDto {
    private BigDecimal totalRevenue;
    private BigDecimal availableFunds;
    private BigDecimal totalWithdrawals;
    private List<RevenueTimeline> revenueTimeline;
    private List<WithdrawalDto> withdrawals;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueTimeline {
        private String period;
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WithdrawalDto {
        private String id;
        private String purpose;
        private BigDecimal amount;
        private String date;
        private String status;
    }
}


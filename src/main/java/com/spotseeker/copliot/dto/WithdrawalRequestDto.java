package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDto {

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private BankDetails bankDetails;

    private String note;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankDetails {
        private String bankName;
        private String accountNumber;
        private String accountName;
    }
}


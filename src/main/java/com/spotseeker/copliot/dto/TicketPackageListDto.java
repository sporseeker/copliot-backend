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
public class TicketPackageListDto {
    private List<TicketPackageDto> ticketPackages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketPackageDto {
        private String id;
        private String name;
        private BigDecimal price;
        private Integer releaseCount;
        private Integer soldCount;
        private Integer availableCount;
        private String startDateTime;
        private String endDateTime;
        private String status;
    }
}


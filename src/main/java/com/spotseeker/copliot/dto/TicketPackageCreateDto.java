package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketPackageCreateDto {

    @NotBlank(message = "Package name is required")
    private String name;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "Release count is required")
    private Integer releaseCount;

    @NotBlank(message = "Start date time is required")
    private String startDateTime; // ISO 8601 format

    @NotBlank(message = "End date time is required")
    private String endDateTime; // ISO 8601 format

    private String status = "active";
}


package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketValidationResponseDto {
    private Boolean valid;
    private String ticketId;
    private String packageName;
    private String attendeeName;
    private Boolean alreadyUsed;
    private String usedAt;
    private String message;
}


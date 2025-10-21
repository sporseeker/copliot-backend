package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.TicketValidationRequestDto;
import com.spotseeker.copliot.dto.TicketValidationResponseDto;
import com.spotseeker.copliot.dto.WithdrawalRequestDto;
import com.spotseeker.copliot.dto.WithdrawalResponseDto;
import com.spotseeker.copliot.service.TicketService;
import com.spotseeker.copliot.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/{id}")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final WithdrawalService withdrawalService;

    @PostMapping("/generate-ticket-qr")
    public ResponseEntity<Map<String, Object>> generateTicketQr(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long userId = Long.parseLong(authentication.getName());
        Long ticketPackageId = Long.parseLong(request.get("ticket_package_id").toString());
        Integer quantity = Integer.parseInt(request.get("quantity").toString());

        List<TicketService.TicketResponseDto> tickets =
                ticketService.generateTickets(userId, id, ticketPackageId, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("qr_codes", tickets);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-ticket")
    public ResponseEntity<TicketValidationResponseDto> validateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketValidationRequestDto request) {
        TicketValidationResponseDto response = ticketService.validateTicket(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finance/withdraw")
    public ResponseEntity<WithdrawalResponseDto> requestWithdrawal(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody WithdrawalRequestDto request) {
        Long userId = Long.parseLong(authentication.getName());
        WithdrawalResponseDto response = withdrawalService.requestWithdrawal(userId, id, request);
        return ResponseEntity.ok(response);
    }
}


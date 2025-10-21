package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.service.TicketPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/ticket-packages")
@RequiredArgsConstructor
public class TicketPackageController {

    private final TicketPackageService ticketPackageService;

    @PostMapping
    public ResponseEntity<TicketPackageListDto.TicketPackageDto> createTicketPackage(
            Authentication authentication,
            @PathVariable Long eventId,
            @Valid @RequestBody TicketPackageCreateDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        TicketPackageListDto.TicketPackageDto ticketPackage =
                ticketPackageService.createTicketPackage(userId, eventId, dto);
        return ResponseEntity.ok(ticketPackage);
    }

    @GetMapping
    public ResponseEntity<TicketPackageListDto> getTicketPackages(
            Authentication authentication,
            @PathVariable Long eventId) {
        Long userId = Long.parseLong(authentication.getName());
        TicketPackageListDto ticketPackages = ticketPackageService.getTicketPackages(userId, eventId);
        return ResponseEntity.ok(ticketPackages);
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<TicketPackageListDto.TicketPackageDto> updateTicketPackage(
            Authentication authentication,
            @PathVariable Long eventId,
            @PathVariable Long packageId,
            @Valid @RequestBody TicketPackageUpdateDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        TicketPackageListDto.TicketPackageDto ticketPackage =
                ticketPackageService.updateTicketPackage(userId, eventId, packageId, dto);
        return ResponseEntity.ok(ticketPackage);
    }

    @DeleteMapping("/{packageId}")
    public ResponseEntity<Map<String, Object>> deleteTicketPackage(
            Authentication authentication,
            @PathVariable Long eventId,
            @PathVariable Long packageId) {
        Long userId = Long.parseLong(authentication.getName());
        ticketPackageService.deleteTicketPackage(userId, eventId, packageId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ticket package deleted");
        return ResponseEntity.ok(response);
    }
}


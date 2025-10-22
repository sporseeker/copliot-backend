package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.PartnerApprovalDto;
import com.spotseeker.copliot.dto.PartnerRegistrationResponseDto;
import com.spotseeker.copliot.model.PartnerRegistrationRequest;
import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.repository.PartnerRepository;
import com.spotseeker.copliot.repository.UserRepository;
import com.spotseeker.copliot.repository.EventRepository;
import com.spotseeker.copliot.service.PartnerRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final EventRepository eventRepository;
    private final PartnerRegistrationService partnerRegistrationService;

    @GetMapping("/partners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPartners(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit);
        Page<User> users;

        if (status != null) {
            User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
            users = userRepository.findByStatus(userStatus, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("partners", users.getContent());
        response.put("total", users.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/partners/{partnerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updatePartnerStatus(
            @PathVariable Long partnerId,
            @RequestBody Map<String, String> request) {

        User user = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        User.UserStatus status = User.UserStatus.valueOf(request.get("status").toUpperCase());
        user.setStatus(status);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Partner status updated successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/events/{eventId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateEventStatus(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> request) {

        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        var status = com.spotseeker.copliot.model.Event.EventStatus.valueOf(
                request.get("status").toUpperCase()
        );
        event.setStatus(status);
        eventRepository.save(event);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Event status updated successfully");
        if (request.containsKey("reason")) {
            response.put("reason", request.get("reason"));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/partner-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPartnerRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit);
        Page<PartnerRegistrationRequest> requests;

        if (status != null && status.equals("PENDING_APPROVAL")) {
            requests = partnerRegistrationService.getPendingRequests(pageable);
        } else {
            requests = partnerRegistrationService.getAllRequests(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("requests", requests.getContent());
        response.put("total", requests.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner-requests/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerRegistrationResponseDto> approvePartnerRequest(
            @Valid @RequestBody PartnerApprovalDto request,
            Authentication authentication) {

        // Get admin user ID from authentication (subject contains user ID)
        Long adminId = Long.parseLong(authentication.getName());
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        PartnerRegistrationResponseDto response;

        if ("approve".equalsIgnoreCase(request.getAction())) {
            response = partnerRegistrationService.approveRequest(
                    request.getRequestId(),
                    admin.getId(),
                    request.getNotes()
            );
        } else if ("reject".equalsIgnoreCase(request.getAction())) {
            response = partnerRegistrationService.rejectRequest(
                    request.getRequestId(),
                    admin.getId(),
                    request.getRejectionReason(),
                    request.getNotes()
            );
        } else {
            throw new RuntimeException("Invalid action. Must be 'approve' or 'reject'");
        }

        return ResponseEntity.ok(response);
    }
}

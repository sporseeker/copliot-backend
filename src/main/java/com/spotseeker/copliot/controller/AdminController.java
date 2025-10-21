package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.repository.PartnerRepository;
import com.spotseeker.copliot.repository.UserRepository;
import com.spotseeker.copliot.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
}


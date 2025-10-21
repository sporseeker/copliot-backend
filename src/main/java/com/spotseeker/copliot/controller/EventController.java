package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDetailDto> createEvent(
            Authentication authentication,
            @Valid @ModelAttribute EventCreateDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        EventDetailDto event = eventService.createEvent(userId, dto);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<EventListResponseDto> getEvents(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {
        Long userId = Long.parseLong(authentication.getName());
        EventListResponseDto events = eventService.getEvents(userId, status, page, limit);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDto> getEventById(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getName());
        EventDetailDto event = eventService.getEventById(userId, id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDetailDto> updateEvent(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @ModelAttribute EventUpdateDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        EventDetailDto event = eventService.updateEvent(userId, id, dto);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEvent(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getName());
        eventService.deleteEvent(userId, id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Event deleted successfully");
        return ResponseEntity.ok(response);
    }
}


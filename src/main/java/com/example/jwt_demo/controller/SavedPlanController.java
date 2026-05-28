package com.example.jwt_demo.controller;

import com.example.jwt_demo.dto.AIReply;
import com.example.jwt_demo.dto.ActivatePlanRequest;
import com.example.jwt_demo.dto.EditPlanRequest;
import com.example.jwt_demo.dto.SavePlanRequest;
import com.example.jwt_demo.exception.AIServiceException;
import com.example.jwt_demo.model.SavedPlan;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.SavedPlanRepository;
import com.example.jwt_demo.service.AIService;
import com.example.jwt_demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class SavedPlanController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SavedPlanRepository savedPlanRepository;
    private final UserService userService;
    private final AIService aiService;

    public SavedPlanController(SavedPlanRepository savedPlanRepository,
                               UserService userService,
                               AIService aiService) {
        this.savedPlanRepository = savedPlanRepository;
        this.userService = userService;
        this.aiService = aiService;
    }

    @PostMapping("/save")
    public ResponseEntity<SavedPlan> save(
            @AuthenticationPrincipal String email,
            @RequestBody SavePlanRequest request) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SavedPlan plan = new SavedPlan();
        plan.setUserId(user.getId());
        plan.setTitle(request.getTitle());
        plan.setData(request.getData());
        plan.setSavedAt(LocalDateTime.now());
        return ResponseEntity.ok(savedPlanRepository.save(plan));
    }

    @GetMapping
    public ResponseEntity<List<SavedPlan>> myPlans(@AuthenticationPrincipal String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(savedPlanRepository.findByUserId(user.getId()));
    }

    @PostMapping("/{id}/edit")
    public ResponseEntity<SavedPlan> edit(
            @AuthenticationPrincipal String email,
            @PathVariable String id,
            @RequestBody EditPlanRequest request) {

        if (request.getInstruction() == null || request.getInstruction().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instruction is required");
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SavedPlan plan = savedPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        if (!user.getId().equals(plan.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your plan");
        }

        String currentPlanJson;
        try {
            currentPlanJson = MAPPER.writeValueAsString(plan.getData());
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize plan");
        }

        List<Map<String, String>> history = List.of(Map.of(
                "role", "Soli",
                "content", currentPlanJson
        ));

        AIReply reply = aiService.getReply(request.getInstruction(), null, "plan", history);

        if (!"plan".equals(reply.getType()) || reply.getData() == null) {
            throw new AIServiceException("AI did not return an updated plan");
        }

        plan.setData(reply.getData());
        plan.setSavedAt(LocalDateTime.now());
        return ResponseEntity.ok(savedPlanRepository.save(plan));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(
            @AuthenticationPrincipal String email,
            @PathVariable String id,
            @RequestBody ActivatePlanRequest request) {

        if (request.getStartDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate is required");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate cannot be in the past");
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SavedPlan plan = savedPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        if (!user.getId().equals(plan.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your plan");
        }

        int dayCount = countDays(plan.getData());
        if (dayCount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan has no days");
        }

        LocalDate start = request.getStartDate();
        LocalDate end = start.plusDays(dayCount - 1);

        for (SavedPlan other : savedPlanRepository.findByUserIdAndStartDateNotNull(user.getId())) {
            if (other.getId().equals(plan.getId())) continue;
            if (other.getStartDate() == null || other.getEndDate() == null) continue;
            boolean overlap = !start.isAfter(other.getEndDate()) && !end.isBefore(other.getStartDate());
            if (overlap) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "error", "OVERLAP",
                        "message", "This plan overlaps with another active plan",
                        "conflictsWith", Map.of(
                                "id", other.getId(),
                                "title", other.getTitle(),
                                "startDate", other.getStartDate().toString(),
                                "endDate", other.getEndDate().toString()
                        )
                ));
            }
        }

        plan.setStartDate(start);
        plan.setEndDate(end);
        savedPlanRepository.save(plan);

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "startDate", start.toString(),
                "endDate", end.toString()
        ));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<SavedPlan> deactivate(
            @AuthenticationPrincipal String email,
            @PathVariable String id) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SavedPlan plan = savedPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        if (!user.getId().equals(plan.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your plan");
        }
        plan.setStartDate(null);
        plan.setEndDate(null);
        return ResponseEntity.ok(savedPlanRepository.save(plan));
    }

    @SuppressWarnings("unchecked")
    private int countDays(Object data) {
        if (!(data instanceof Map<?, ?> map)) return 0;
        Object days = ((Map<String, Object>) map).get("days");
        if (!(days instanceof List<?> list)) return 0;
        return list.size();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal String email,
            @PathVariable String id) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SavedPlan plan = savedPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        if (!user.getId().equals(plan.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your plan");
        }
        savedPlanRepository.delete(plan);
        return ResponseEntity.noContent().build();
    }
}

package com.example.jwt_demo.controller;

import com.example.jwt_demo.dto.SuggestedPlanCard;
import com.example.jwt_demo.model.Landmark;
import com.example.jwt_demo.model.SuggestedPlan;
import com.example.jwt_demo.repository.LandmarkRepository;
import com.example.jwt_demo.repository.SuggestedPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final LandmarkRepository landmarkRepository;
    private final SuggestedPlanRepository suggestedPlanRepository;

    public HomeController(LandmarkRepository landmarkRepository,
                          SuggestedPlanRepository suggestedPlanRepository) {
        this.landmarkRepository = landmarkRepository;
        this.suggestedPlanRepository = suggestedPlanRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        List<Landmark> trending = landmarkRepository.findAll();
        List<SuggestedPlanCard> suggested = suggestedPlanRepository.findAll().stream()
                .map(SuggestedPlanCard::from)
                .toList();
        return ResponseEntity.ok(Map.of(
                "trendingExpeditions", trending,
                "suggestedPlans", suggested
        ));
    }

    @GetMapping("/suggested-plans/{id}")
    public ResponseEntity<SuggestedPlan> getSuggestedPlan(@PathVariable String id) {
        SuggestedPlan plan = suggestedPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        return ResponseEntity.ok(plan);
    }
}

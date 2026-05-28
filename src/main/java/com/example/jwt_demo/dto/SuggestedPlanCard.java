package com.example.jwt_demo.dto;

import com.example.jwt_demo.model.SuggestedPlan;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuggestedPlanCard {
    private String id;
    private String name;
    private String subtitle;
    private String imageUrl;

    public static SuggestedPlanCard from(SuggestedPlan p) {
        return new SuggestedPlanCard(p.getId(), p.getName(), p.getSubtitle(), p.getImageUrl());
    }
}

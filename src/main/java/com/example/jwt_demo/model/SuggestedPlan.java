package com.example.jwt_demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "suggested_plans")
public class SuggestedPlan {

    @Id
    private String id;

    private String name;
    private String subtitle;
    private String imageUrl;
    private Object data;
}

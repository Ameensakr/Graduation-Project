package com.example.jwt_demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "saved_plans")
public class SavedPlan {

    @Id
    private String id;

    private String userId;
    private String title;
    private Object data;
    private LocalDateTime savedAt;

    private LocalDate startDate;
    private LocalDate endDate;
}

package com.example.jwt_demo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ActivatePlanRequest {
    private LocalDate startDate;
}

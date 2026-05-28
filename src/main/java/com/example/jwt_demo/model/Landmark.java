package com.example.jwt_demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "landmarks")
public class Landmark {

    @Id
    private String id;

    private String name;
    private String subtitle;
    private String imageUrl;
    private Double lat;
    private Double lng;
}
